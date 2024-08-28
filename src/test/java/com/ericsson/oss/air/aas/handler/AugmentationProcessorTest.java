/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 *
 *
 * The copyright to the computer program(s) herein is the property of
 *
 * Ericsson Inc. The programs may be used and/or copied only with written
 *
 * permission from Ericsson Inc. or in accordance with the terms and
 *
 * conditions stipulated in the agreement/contract under which the
 *
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.aas.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.ericsson.oss.air.aas.model.ardq.ArdqRequestDto;
import com.ericsson.oss.air.aas.model.ardq.ArdqResponseDto;
import com.ericsson.oss.air.aas.service.AugmentationProcessingService;
import com.ericsson.oss.air.aas.service.ardq.ArdqService;
import com.ericsson.oss.air.api.generated.model.ArdqAugmentationFieldDto;
import com.ericsson.oss.air.exception.AasValidationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.micrometer.core.instrument.Counter;
import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.apache.kafka.common.record.TimestampType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.UnknownHttpStatusCodeException;

@ExtendWith(OutputCaptureExtension.class)
@ExtendWith(MockitoExtension.class)
public class AugmentationProcessorTest {

    private static final String FIELD_1 = "field1";
    private static final String FIELD_2 = "field2";
    private static final String FIELD_3 = "field3";

    private static final String UNAUGMENTED_VALUE = "unaug_value_1";
    private static final List<String> AUGMENTED_VALUES = List.of("aug_value_1", "aug_value_2");

    private static final String ARDQ_TYPE = "CORE";

    private static final String INPUT_SCHEMA_NAME = "inputSchemaName";
    private static final String OUTPUT_SCHEMA_NAME = "Test";

    private static final Schema OUTPUT_SCHEMA = SchemaBuilder
            .record(OUTPUT_SCHEMA_NAME)
            .namespace("5G." + ARDQ_TYPE + ".PM_COUNTERS").fields()
            .name(FIELD_1).type().optional().stringType()
            .name(FIELD_2).type().optional().array().items().stringType()
            .name(FIELD_3).type().optional().stringType()
            .endRecord();

    private static final String INPUT_SCHEMA_STRING = SchemaBuilder
            .record(INPUT_SCHEMA_NAME)
            .namespace("5G." + ARDQ_TYPE + ".PM_COUNTERS").fields()
            .name(FIELD_1).type().optional().stringType()
            .endRecord().toString();

    private static final String HEADER_SCHEMA_SUBJECT_KEY = "schemaSubject";
    private static final String HEADER_SCHEMA_SUBJECT_VALUE = "5G.CORE.PM_COUNTERS." + OUTPUT_SCHEMA_NAME;
    private static final Integer SCHEMA_VERSION = 2;
    private static final String HEADER_SCHEMA_ID_KEY = "schemaID";
    private static final String HEADER_SCHEMA_ID_VALUE = SCHEMA_VERSION.toString();

    @Mock
    private AugmentationProcessingService augProcService;

    @Mock
    private ArdqService ardqService;

    @Mock
    private Counter augmentationErrorsCounter;

    private AugmentationProcessor augmentationProcessor;

    private ConsumerRecord<String, GenericRecord> sampleIncomingRecord;

    private final Headers headers = new RecordHeaders();

    @BeforeEach
    void setUp() {
        final ArdqAugmentationFieldDto augFieldDto = new ArdqAugmentationFieldDto().output(FIELD_2).input(List.of(FIELD_1));

        this.augmentationProcessor = AugmentationProcessor.builder()
                .ardqUrl("URL")
                .outputSchema(OUTPUT_SCHEMA)
                .fields(List.of(augFieldDto))
                .schemaVersion(SCHEMA_VERSION)
                .ardqService(this.ardqService)
                .augProcService(this.augProcService)
                .ardqType(ARDQ_TYPE)
                .augmentationErrorsCounter(this.augmentationErrorsCounter)
                .build();

        final var inputRecord = new GenericData.Record(new Schema.Parser().parse(INPUT_SCHEMA_STRING));

        headers.add(HEADER_SCHEMA_SUBJECT_KEY, "5G.CORE.PM_COUNTERS.OLD".getBytes(StandardCharsets.UTF_8));
        headers.add(HEADER_SCHEMA_ID_KEY, SCHEMA_VERSION.toString().getBytes(StandardCharsets.UTF_8));
        this.sampleIncomingRecord = new ConsumerRecord<>("topic", 0, 0L, -1L, TimestampType.NO_TIMESTAMP_TYPE, -1, -1, "key", inputRecord, headers,
                Optional.empty());
    }

    @Test
    void testApply_called() throws AasValidationException {
        //given
        final var inputRecord = new GenericData.Record(new Schema.Parser().parse(INPUT_SCHEMA_STRING));
        final ConsumerRecord<String, GenericRecord> incomingRecord = new ConsumerRecord<>("topic", 0, 0L, "key", inputRecord);

        final var augmentation = ArdqResponseDto.builder()
                .fields(List.of(
                        List.of(ArdqResponseDto.AugmentationField.builder().name(FIELD_2).value(AUGMENTED_VALUES.get(0)).build()),
                        List.of(ArdqResponseDto.AugmentationField.builder().name(FIELD_2).value(AUGMENTED_VALUES.get(1)).build())))
                .build();

        when(this.ardqService.getAugmentationData(anyString(), any())).thenReturn(ResponseEntity.of(Optional.of(augmentation)));

        this.augmentationProcessor.apply(incomingRecord);

        verify(this.ardqService, times(1)).getAugmentationData(anyString(), any(ArdqRequestDto.class));
        verify(this.augProcService, times(2)).sendRecord(any(GenericRecord.class), any(Headers.class), any(String.class));
        verify(this.augmentationErrorsCounter, never()).increment();
    }

    @Test
    void testApply_called_inputFieldNotInRecordExceptionCaught(final CapturedOutput output) throws AasValidationException {

        final ArdqAugmentationFieldDto augFieldDto = new ArdqAugmentationFieldDto().output(FIELD_2).input(List.of("input_1"));

        final var inputRecord = new GenericData.Record(new Schema.Parser().parse(INPUT_SCHEMA_STRING));
        final ConsumerRecord<String, GenericRecord> incomingRecord = new ConsumerRecord<>("topic", 0, 0L, "key", inputRecord);

        final AugmentationProcessor augmentationProcessor = AugmentationProcessor.builder()
                .ardqUrl("URL")
                .outputSchema(OUTPUT_SCHEMA)
                .fields(List.of(augFieldDto))
                .ardqService(this.ardqService)
                .ardqType(ARDQ_TYPE)
                .augProcService(this.augProcService)
                .augmentationErrorsCounter(this.augmentationErrorsCounter)
                .build();

        Assertions.assertDoesNotThrow(() -> augmentationProcessor.apply(incomingRecord));

        Assertions.assertTrue(
                output.getOut().contains("Failed to prepare ARDQ request: ")
                        && output.getOut().contains("com.ericsson.oss.air.exception.AasValidationException"));

        verify(this.augmentationErrorsCounter, times(1)).increment();

    }

    @Test
    void testApply_called_HttpServerErrorExceptionCaught(final CapturedOutput output) throws AasValidationException {

        when(this.ardqService.getAugmentationData(anyString(), any())).thenThrow(HttpServerErrorException.class);

        Assertions.assertDoesNotThrow(() -> this.augmentationProcessor.apply(this.sampleIncomingRecord));

        Assertions.assertTrue(output.getOut().contains("Failed to get augmentation data due to an HTTP Server Error"));

        verify(this.augmentationErrorsCounter, times(1)).increment();

    }

    @Test
    void testApply_called_CallNotPermittedExceptionCaught(final CapturedOutput output) throws AasValidationException {

        when(this.ardqService.getAugmentationData(anyString(), any())).thenThrow(CallNotPermittedException.class);

        Assertions.assertDoesNotThrow(() -> this.augmentationProcessor.apply(this.sampleIncomingRecord));

        Assertions.assertTrue(output.getOut().contains("Failed to get augmentation data"));

        verify(this.augmentationErrorsCounter, times(1)).increment();

    }

    @Test
    void testApply_called_ExceptionCaught(final CapturedOutput output) throws AasValidationException {

        when(this.ardqService.getAugmentationData(anyString(), any())).thenThrow(UnknownHttpStatusCodeException.class);

        Assertions.assertDoesNotThrow(() -> this.augmentationProcessor.apply(this.sampleIncomingRecord));

        Assertions.assertTrue(output.getOut().contains("org.springframework.web.client.UnknownHttpStatusCodeException"));

        verify(this.augmentationErrorsCounter, times(1)).increment();

    }

    @Test
    void testApply_called_AllExceptionCaught(final CapturedOutput output) throws AasValidationException {

        when(this.ardqService.getAugmentationData(anyString(), any())).thenThrow(AasValidationException.class);

        Assertions.assertDoesNotThrow(() -> this.augmentationProcessor.apply(this.sampleIncomingRecord));

        Assertions.assertTrue(output.getOut().contains("com.ericsson.oss.air.exception.AasValidationException"));

        verify(this.augmentationErrorsCounter, times(1)).increment();

    }

    @Test
    void testApply_called_validationFailed(final CapturedOutput output) throws AasValidationException {

        final ArdqAugmentationFieldDto augFieldDto = new ArdqAugmentationFieldDto().output(FIELD_2).input(List.of(FIELD_1));

        final AugmentationProcessor augmentationProcessor = AugmentationProcessor.builder()
                .ardqUrl("URL")
                .outputSchema(OUTPUT_SCHEMA)
                .fields(List.of(augFieldDto))
                .ardqService(this.ardqService)
                .augProcService(this.augProcService)
                .augmentationErrorsCounter(this.augmentationErrorsCounter)
                .build();

        Assertions.assertDoesNotThrow(() -> augmentationProcessor.apply(this.sampleIncomingRecord));

        Assertions.assertFalse(output.getOut().contains("com.ericsson.oss.air.exception.AasValidationException"));

        verify(this.augmentationErrorsCounter, times(1)).increment();

    }

    @Test
    void testGetPopulatedArdqRequest_populated() throws AasValidationException, JsonProcessingException {
        //given
        final ObjectMapper om = new ObjectMapper();

        final var inputRecord = new GenericData.Record(new Schema.Parser().parse(INPUT_SCHEMA_STRING));
        inputRecord.put(FIELD_1, UNAUGMENTED_VALUE);
        final ConsumerRecord<String, GenericRecord> incomingRecord = new ConsumerRecord<>("topic", 0, 0L, "key", inputRecord);

        //when
        final ArdqRequestDto ardqRequest = AugmentationProcessorHandler.getPopulatedArdqRequest(incomingRecord.value()
                , this.augmentationProcessor.getFields(), "");

        //then
        assertThat(ardqRequest.getInputFields()).hasSize(1)
                .contains(ArdqRequestDto.QueryField.of(FIELD_1, UNAUGMENTED_VALUE));
        assertThat(ardqRequest.getAugmentationFields()).hasSize(1)
                .containsExactlyInAnyOrder(
                        ArdqRequestDto.AugmentationFieldRequest.of(FIELD_2));

        // Make sure augmentation request can be serialized to json correctly
        final List<ArdqRequestDto.QueryField> queryFieldList = new ArrayList<>(ardqRequest.getInputFields());
        final String inputFieldString = om.writer().writeValueAsString(queryFieldList.get(0));
        assertEquals("{\"name\":\"field1\",\"value\":\"unaug_value_1\"}", inputFieldString);

        final String augmentationRequestString = om.writer().writeValueAsString(ardqRequest);
        assertEquals("{\"inputFields\":[{\"name\":\"field1\",\"value\":\"unaug_value_1\"}],\"augmentationFields\":[{\"name\":\"field2\"}]}",
                augmentationRequestString);
    }

    @Test
    void testGetPopulatedArdqRequest_inputFieldNotInRecord_exception() {
        //given
        final ArdqAugmentationFieldDto augFieldDto = new ArdqAugmentationFieldDto().output(FIELD_2).input(List.of("input_1"));

        final var inputRecord = new GenericData.Record(new Schema.Parser().parse(INPUT_SCHEMA_STRING));
        final ConsumerRecord<String, GenericRecord> incomingRecord = new ConsumerRecord<>("topic", 0, 0L, "key", inputRecord);

        final AugmentationProcessor augmentationProcessor =
                AugmentationProcessor.builder().
                        ardqUrl("URL")
                        .outputSchema(OUTPUT_SCHEMA)
                        .fields(List.of(augFieldDto))
                        .ardqService(this.ardqService)
                        .augProcService(this.augProcService)
                        .augmentationErrorsCounter(this.augmentationErrorsCounter)
                        .build();

        //when
        final AasValidationException thrown = assertThrows(AasValidationException.class,
                () -> AugmentationProcessorHandler.getPopulatedArdqRequest(incomingRecord.value(), augmentationProcessor.getFields(), ""));

        //then
        assertTrue(thrown.getMessage().contentEquals(
                "The deserialized avro record provided does not contain the field: " + "input_1"));
    }

    @Test
    void testGetPopulatedOutputRecord_outputRecordContainsInputField_success() throws AasValidationException {
        // Given
        final var inputRecord = new GenericData.Record(new Schema.Parser().parse(INPUT_SCHEMA_STRING));
        inputRecord.put(FIELD_1, UNAUGMENTED_VALUE);
        final ConsumerRecord<String, GenericRecord> incomingRecord = new ConsumerRecord<>("topic", 0, 0L, "key", inputRecord);

        final AugmentationProcessor augmentationProcessor =
                AugmentationProcessor.builder()
                        .ardqUrl("URL")
                        .outputSchema(OUTPUT_SCHEMA)
                        .fields(List.of())
                        .ardqService(this.ardqService)
                        .augProcService(this.augProcService)
                        .augmentationErrorsCounter(this.augmentationErrorsCounter)
                        .build();

        final var augmentation = ArdqResponseDto.builder()
                .fields(List.of(
                        List.of(ArdqResponseDto.AugmentationField.builder().name(FIELD_2).value(AUGMENTED_VALUES.get(0)).build()),
                        List.of(ArdqResponseDto.AugmentationField.builder().name(FIELD_2).value(AUGMENTED_VALUES.get(1)).build())))
                .build();

        // When
        for (int listIndex = 0; listIndex < augmentation.getFields().size(); listIndex++) {
            final List<ArdqResponseDto.AugmentationField> augmentationFields = augmentation.getFields().get(listIndex);
            final var actualRecord = AugmentationProcessorHandler.getPopulatedOutputRecord(incomingRecord, augmentationFields,
                    augmentationProcessor.getOutputSchema());
            // Then
            assertEquals(UNAUGMENTED_VALUE, actualRecord.get(FIELD_1));
            assertEquals(AUGMENTED_VALUES.get(listIndex), actualRecord.get(FIELD_2));
            assertNull(actualRecord.get(FIELD_3));

            verify(this.augmentationErrorsCounter, never()).increment();
        }

    }

    @Test
    void testGetPopulatedOutputRecord_missingInputFieldInOutput_exception() {
        // Given
        final var MISSING_FIELD = "missing_field";
        final String inputSchema = SchemaBuilder
                .record("Test")
                .namespace("Namespace").fields()
                .name(MISSING_FIELD).type().optional().stringType()
                .endRecord().toString();

        final var inputRecord = new GenericData.Record(new Schema.Parser().parse(inputSchema));
        inputRecord.put(MISSING_FIELD, "value1");
        final ConsumerRecord<String, GenericRecord> incomingRecord = new ConsumerRecord<>("topic", 0, 0L, "key", inputRecord);

        final var ardqResponse = ArdqResponseDto.builder()
                .fields(List.of(
                        List.of(ArdqResponseDto.AugmentationField.builder().name(FIELD_2).value(AUGMENTED_VALUES.get(0)).build()),
                        List.of(ArdqResponseDto.AugmentationField.builder().name(FIELD_2).value(AUGMENTED_VALUES.get(1)).build())))
                .build();

        final AugmentationProcessor augmentationProcessor =
                AugmentationProcessor.builder()
                        .ardqUrl("URL")
                        .outputSchema(OUTPUT_SCHEMA)
                        .fields(List.of())
                        .ardqService(this.ardqService)
                        .augProcService(this.augProcService)
                        .augmentationErrorsCounter(this.augmentationErrorsCounter)
                        .build();

        //when
        final AasValidationException firstThrown = assertThrows(AasValidationException.class,
                () -> AugmentationProcessorHandler.getPopulatedOutputRecord(incomingRecord,
                        ardqResponse.getFields().get(0),
                        augmentationProcessor.getOutputSchema()));

        final AasValidationException secondThrown = assertThrows(AasValidationException.class,
                () -> AugmentationProcessorHandler.getPopulatedOutputRecord(incomingRecord,
                        ardqResponse.getFields().get(1),
                        augmentationProcessor.getOutputSchema()));

        //then
        assertEquals(String.format("The output schema does not contain the field '%s' present in the input schema", MISSING_FIELD),
                firstThrown.getMessage());
        assertEquals(String.format("The output schema does not contain the field '%s' present in the input schema", MISSING_FIELD),
                secondThrown.getMessage());

    }

    @Test
    void testGetPopulatedOutputRecord_missingAugmentedFieldInOutput_exception() {
        // Given
        final var inputRecord = new GenericData.Record(new Schema.Parser().parse(INPUT_SCHEMA_STRING));
        inputRecord.put(FIELD_1, UNAUGMENTED_VALUE);
        final ConsumerRecord<String, GenericRecord> incomingRecord = new ConsumerRecord<>("topic", 0, 0L, "key", inputRecord);

        final var ardqResponse = ArdqResponseDto.builder()
                .fields(List.of(
                        List.of(ArdqResponseDto.AugmentationField.builder().name("MISSING_FIELD").value(AUGMENTED_VALUES.get(0)).build()),
                        List.of(ArdqResponseDto.AugmentationField.builder().name("MISSING_FIELD").value(AUGMENTED_VALUES.get(1)).build())))
                .build();

        final AugmentationProcessor augmentationProcessor =
                AugmentationProcessor.builder()
                        .ardqUrl("URL")
                        .outputSchema(OUTPUT_SCHEMA)
                        .fields(List.of())
                        .ardqService(this.ardqService)
                        .augProcService(this.augProcService)
                        .augmentationErrorsCounter(this.augmentationErrorsCounter)
                        .build();

        //when
        final AasValidationException firstThrown = assertThrows(AasValidationException.class,
                () -> AugmentationProcessorHandler.getPopulatedOutputRecord(incomingRecord,
                        ardqResponse.getFields().get(0),
                        augmentationProcessor.getOutputSchema()));

        final AasValidationException secondThrown = assertThrows(AasValidationException.class,
                () -> AugmentationProcessorHandler.getPopulatedOutputRecord(incomingRecord,
                        ardqResponse.getFields().get(1),
                        augmentationProcessor.getOutputSchema()));

        //then
        assertEquals(String.format("The output schema does not contain the field '%s' present in the ARDQ response", "MISSING_FIELD"),
                firstThrown.getMessage());
        assertEquals(String.format("The output schema does not contain the field '%s' present in the ARDQ response", "MISSING_FIELD"),
                secondThrown.getMessage());
    }

    @Test
    void testGenerateOutputHeaders() {
        final Headers outgoingHeaders = this.augmentationProcessor.generateOutputHeaders(this.sampleIncomingRecord, OUTPUT_SCHEMA);
        final Header[] headerArray = outgoingHeaders.toArray();

        assertEquals(2, headerArray.length);
        assertEquals(HEADER_SCHEMA_SUBJECT_KEY, headerArray[0].key());
        assertEquals(HEADER_SCHEMA_SUBJECT_VALUE, new String(headerArray[0].value(), StandardCharsets.UTF_8));
        assertEquals(HEADER_SCHEMA_ID_KEY, headerArray[1].key());
        assertEquals(HEADER_SCHEMA_ID_VALUE, new String(headerArray[1].value(), StandardCharsets.UTF_8));
    }

}



/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.aas.handler.registration;

import static com.ericsson.oss.air.aas.handler.RegistrationTestUtil.ARDQ_ID;
import static com.ericsson.oss.air.aas.handler.RegistrationTestUtil.ARDQ_URL;
import static com.ericsson.oss.air.aas.handler.RegistrationTestUtil.DATA_TYPE_RESPONSE_DTO;
import static com.ericsson.oss.air.aas.handler.RegistrationTestUtil.FIELDS;
import static com.ericsson.oss.air.aas.handler.RegistrationTestUtil.FIELD_DTO1;
import static com.ericsson.oss.air.aas.handler.RegistrationTestUtil.FIELD_DTO2;
import static com.ericsson.oss.air.aas.handler.RegistrationTestUtil.INPUT_FIELD1;
import static com.ericsson.oss.air.aas.handler.RegistrationTestUtil.INPUT_FIELD2;
import static com.ericsson.oss.air.aas.handler.RegistrationTestUtil.INPUT_SCHEMA;
import static com.ericsson.oss.air.aas.handler.RegistrationTestUtil.INPUT_SCHEMA_NAME;
import static com.ericsson.oss.air.aas.handler.RegistrationTestUtil.INPUT_SCHEMA_REFERENCE;
import static com.ericsson.oss.air.aas.handler.RegistrationTestUtil.INPUT_SCHEMA_REFERENCE_OBJ;
import static com.ericsson.oss.air.aas.handler.RegistrationTestUtil.ONE_RULE_REGISTRATION_DTO;
import static com.ericsson.oss.air.aas.handler.RegistrationTestUtil.OUTPUT_FIELD1;
import static com.ericsson.oss.air.aas.handler.RegistrationTestUtil.OUTPUT_SCHEMA;
import static com.ericsson.oss.air.aas.handler.RegistrationTestUtil.OUTPUT_SCHEMA_NAME;
import static com.ericsson.oss.air.aas.handler.RegistrationTestUtil.OUTPUT_SCHEMA_REFERENCE;
import static com.ericsson.oss.air.aas.handler.RegistrationTestUtil.OUTPUT_SCHEMA_REFERENCE_OBJ;
import static com.ericsson.oss.air.aas.handler.RegistrationTestUtil.SCHEMA_NAMESPACE;
import static com.ericsson.oss.air.aas.handler.RegistrationTestUtil.SUBJECT_NAME;
import static com.ericsson.oss.air.aas.handler.RegistrationTestUtil.TWO_RULES_REGISTRATION_DTO;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.ericsson.oss.air.aas.handler.registration.provider.MessageSchemaRequestDtoProvider;
import com.ericsson.oss.air.aas.handler.registration.provider.OutputSchemaNameProvider;
import com.ericsson.oss.air.aas.model.IoSchema;
import com.ericsson.oss.air.aas.model.SpecificationReference;
import com.ericsson.oss.air.aas.model.datacatalog.response.DataTypeResponseDto;
import com.ericsson.oss.air.aas.model.datacatalog.response.MessageSchemaResponseDto;
import com.ericsson.oss.air.aas.repository.ArdqRegistrationDao;
import com.ericsson.oss.air.aas.repository.SchemaDao;
import com.ericsson.oss.air.aas.service.schema.DataCatalogService;
import com.ericsson.oss.air.aas.service.schema.SchemaRegistryService;
import com.ericsson.oss.air.api.generated.model.ArdqAugmentationFieldDto;
import com.ericsson.oss.air.api.generated.model.ArdqAugmentationRuleDto;
import com.ericsson.oss.air.api.generated.model.ArdqRegistrationDto;
import com.ericsson.oss.air.exception.AasValidationException;
import com.ericsson.oss.air.exception.http.problem.exception.HttpBadRequestProblemException;
import com.ericsson.oss.air.exception.http.problem.exception.HttpConflictRequestProblemException;
import com.ericsson.oss.air.exception.http.problem.exception.HttpInternalServerErrorException;
import com.ericsson.oss.air.exception.http.problem.exception.HttpNotFoundRequestProblemException;
import com.ericsson.oss.air.exception.http.problem.exception.HttpServiceUnavailableException;
import com.ericsson.oss.air.util.logging.FaultHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.confluent.kafka.schemaregistry.avro.AvroSchema;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

@ExtendWith(MockitoExtension.class)
class AugmentSchemaHandlerTest {

    public static final Integer TEST_SCHEMA_VERSION = 1;

    @InjectMocks
    private AugmentSchemaHandler handler;

    @Mock
    private ArdqRegistrationDao ardqRegistrationDao;

    @Mock
    private SchemaDao schemaDao;

    @Mock
    private SchemaRegistryService schemaRegistryService;

    @Mock
    private DataCatalogService dataCatalogService;

    @Mock
    private DataTypeResponseDto dataTypeResponseDto;

    @Mock
    private MessageSchemaRequestDtoProvider messageSchemaRequestDtoProvider;

    @Mock
    private OutputSchemaNameProvider outputSchemaNameProvider;

    @Mock
    private ObjectMapper mapper = new ObjectMapper();

    @Mock
    private FaultHandler faultHandler;

    private ListAppender<ILoggingEvent> listAppender;

    @BeforeEach
    void setup() {
        final Logger logger = (Logger) LoggerFactory.getLogger(AugmentSchemaHandler.class);
        logger.setLevel(Level.INFO);
        this.listAppender = new ListAppender<>();
        this.listAppender.start();
        logger.addAppender(this.listAppender);
    }

    @Test
    void test_createAugmentSchema() throws RestClientException, IOException {
        final AvroSchema avroSchema = this.getDummyAvroSchema();
        when(this.dataCatalogService.retrieveSchemaMetadata(INPUT_SCHEMA_REFERENCE_OBJ)).thenReturn(DATA_TYPE_RESPONSE_DTO);
        when(this.outputSchemaNameProvider.generate(ARDQ_ID, INPUT_SCHEMA_NAME)).thenReturn(OUTPUT_SCHEMA_NAME);
        when(this.schemaRegistryService.getLatestSchema(SUBJECT_NAME)).thenReturn(Optional.of(avroSchema));
        when(this.schemaRegistryService.register(any(), any())).thenReturn(1);
        when(this.schemaRegistryService.getVersion(any(), any())).thenReturn(1);

        this.handler.create(TWO_RULES_REGISTRATION_DTO);

        verify(this.dataCatalogService, times(1)).retrieveSchemaMetadata(INPUT_SCHEMA_REFERENCE_OBJ);
        verify(this.outputSchemaNameProvider, times(1)).generate(ARDQ_ID, INPUT_SCHEMA_NAME);
        verify(this.schemaRegistryService, times(1)).getLatestSchema(SUBJECT_NAME);
        verify(this.schemaDao, times(1)).saveSchema(eq(INPUT_SCHEMA_REFERENCE), any());
        verify(this.schemaDao, times(1)).saveSchema(eq(OUTPUT_SCHEMA_REFERENCE), any());
    }

    @Test
    void test_createAugmentSchema_checkInputAndOutputSchema() throws RestClientException, IOException {
        final AvroSchema avroSchema = this.getDummyAvroSchema();

        when(this.dataCatalogService.retrieveSchemaMetadata(INPUT_SCHEMA_REFERENCE_OBJ)).thenReturn(DATA_TYPE_RESPONSE_DTO);
        when(this.outputSchemaNameProvider.generate(ARDQ_ID, INPUT_SCHEMA_NAME)).thenReturn(OUTPUT_SCHEMA_NAME);
        when(this.schemaRegistryService.getLatestSchema(SUBJECT_NAME)).thenReturn(Optional.of(avroSchema));

        this.handler.create(TWO_RULES_REGISTRATION_DTO);

        verify(this.dataCatalogService, times(1)).retrieveSchemaMetadata(INPUT_SCHEMA_REFERENCE_OBJ);
        verify(this.outputSchemaNameProvider, times(1)).generate(ARDQ_ID, INPUT_SCHEMA_NAME);
        verify(this.schemaRegistryService, times(1)).getLatestSchema(SUBJECT_NAME);
        verify(this.schemaDao, times(1)).saveSchema(eq(INPUT_SCHEMA_REFERENCE), any());
        verify(this.schemaDao, times(1)).saveSchema(eq(OUTPUT_SCHEMA_REFERENCE), any());
    }

    @Test
    void test_createAugmentSchema_outputSchema_existed() throws RestClientException, IOException {
        final AvroSchema avroSchema = this.getDummyAvroSchema();

        when(this.dataCatalogService.retrieveSchemaMetadata(INPUT_SCHEMA_REFERENCE_OBJ)).thenReturn(DATA_TYPE_RESPONSE_DTO);
        when(this.outputSchemaNameProvider.generate(ARDQ_ID, INPUT_SCHEMA_NAME)).thenReturn(OUTPUT_SCHEMA_NAME);
        when(this.schemaRegistryService.getLatestSchema(SUBJECT_NAME)).thenReturn(Optional.of(avroSchema));

        when(this.dataCatalogService.retrieveSchemaMetadata(OUTPUT_SCHEMA_REFERENCE_OBJ)).thenReturn(DATA_TYPE_RESPONSE_DTO);
        when(this.schemaDao.getSchema(OUTPUT_SCHEMA_REFERENCE)).thenReturn(Optional.of(OUTPUT_SCHEMA));

        this.handler.create(TWO_RULES_REGISTRATION_DTO);

        verify(this.dataCatalogService, times(1)).retrieveSchemaMetadata(INPUT_SCHEMA_REFERENCE_OBJ);
        verify(this.outputSchemaNameProvider, times(1)).generate(ARDQ_ID, INPUT_SCHEMA_NAME);
        verify(this.schemaRegistryService, times(1)).getLatestSchema(SUBJECT_NAME);
        verify(this.schemaDao, times(1)).saveSchema(eq(INPUT_SCHEMA_REFERENCE), any());
        verify(this.schemaDao, times(1)).saveSchema(eq(OUTPUT_SCHEMA_REFERENCE), any());

        verify(this.dataCatalogService, times(0)).register(any());
        verify(this.dataCatalogService, times(1)).update(any());
    }

    @Test
    void test_createAugmentSchema_outputSchema_matched() throws RestClientException, IOException {
        // Test create workflow when output schema version matches the DC schema version
        final AvroSchema avroSchema = this.getDummyAvroSchema();

        when(this.dataCatalogService.retrieveSchemaMetadata(INPUT_SCHEMA_REFERENCE_OBJ)).thenReturn(DATA_TYPE_RESPONSE_DTO);
        when(this.outputSchemaNameProvider.generate(ARDQ_ID, INPUT_SCHEMA_NAME)).thenReturn(OUTPUT_SCHEMA_NAME);
        when(this.schemaRegistryService.getLatestSchema(SUBJECT_NAME)).thenReturn(Optional.of(avroSchema));

        when(this.dataCatalogService.retrieveSchemaMetadata(OUTPUT_SCHEMA_REFERENCE_OBJ)).thenReturn(DATA_TYPE_RESPONSE_DTO);
        when(this.schemaDao.getSchema(OUTPUT_SCHEMA_REFERENCE)).thenReturn(Optional.of(OUTPUT_SCHEMA));

        when(this.schemaRegistryService.getVersion(any(), any())).thenReturn(TEST_SCHEMA_VERSION);

        this.handler.create(TWO_RULES_REGISTRATION_DTO);

        verify(this.dataCatalogService, times(0)).register(any());
        verify(this.dataCatalogService, times(0)).update(any());
        verify(this.outputSchemaNameProvider, times(1)).generate(ARDQ_ID, INPUT_SCHEMA_NAME);
    }

    @Test
    void test_createAugmentSchema_rest_client_exception() throws RestClientException, IOException {
        final AvroSchema avroSchema = this.getDummyAvroSchema();
        when(this.dataCatalogService.retrieveSchemaMetadata(INPUT_SCHEMA_REFERENCE_OBJ)).thenReturn(DATA_TYPE_RESPONSE_DTO);
        when(this.outputSchemaNameProvider.generate(ARDQ_ID, INPUT_SCHEMA_NAME)).thenReturn(OUTPUT_SCHEMA_NAME);
        when(this.schemaRegistryService.getLatestSchema(SUBJECT_NAME)).thenReturn(Optional.of(avroSchema));
        when(this.schemaRegistryService.register(any(), any())).thenThrow(RestClientException.class);

        assertThrows(HttpInternalServerErrorException.class, () -> this.handler.create(ONE_RULE_REGISTRATION_DTO));
    }

    @Test
    void test_createAugmentSchema_io_exception() throws RestClientException, IOException {
        final AvroSchema avroSchema = this.getDummyAvroSchema();
        when(this.dataCatalogService.retrieveSchemaMetadata(INPUT_SCHEMA_REFERENCE_OBJ)).thenReturn(DATA_TYPE_RESPONSE_DTO);
        when(this.outputSchemaNameProvider.generate(ARDQ_ID, INPUT_SCHEMA_NAME)).thenReturn(OUTPUT_SCHEMA_NAME);
        when(this.schemaRegistryService.getLatestSchema(SUBJECT_NAME)).thenReturn(Optional.of(avroSchema));
        when(this.schemaRegistryService.register(any(), any())).thenThrow(IOException.class);

        assertThrows(HttpInternalServerErrorException.class, () -> this.handler.create(ONE_RULE_REGISTRATION_DTO));
    }

    @Test
    void test_augmentSchema_OutputFieldExist_flowEnds_BadRequest() throws RestClientException, IOException {
        final AvroSchema avroSchema = this.getDummyAvroSchema();

        when(this.dataCatalogService.retrieveSchemaMetadata(INPUT_SCHEMA_REFERENCE_OBJ)).thenReturn(DATA_TYPE_RESPONSE_DTO);
        when(this.schemaRegistryService.getLatestSchema(SUBJECT_NAME)).thenReturn(Optional.of(avroSchema));

        final ArdqAugmentationFieldDto fieldDto = new ArdqAugmentationFieldDto().input(List.of(INPUT_FIELD1, INPUT_FIELD2)).output(INPUT_FIELD1);

        assertThrows(HttpBadRequestProblemException.class, () -> this.handler.create(new ArdqRegistrationDto().ardqId(ARDQ_ID).ardqUrl("url")
                .rules(List.of(new ArdqAugmentationRuleDto().inputSchema(
                                INPUT_SCHEMA_REFERENCE)
                        .fields(List.of(fieldDto))))));
    }

    @Test
    void test_augmentSchema_InputFieldDoesNotExist_flowEnds_BadRequest() throws RestClientException, IOException, AasValidationException {
        final AvroSchema avroSchema = this.getDummyAvroSchema();

        when(this.dataCatalogService.retrieveSchemaMetadata(INPUT_SCHEMA_REFERENCE_OBJ)).thenReturn(DATA_TYPE_RESPONSE_DTO);
        when(this.schemaRegistryService.getLatestSchema(SUBJECT_NAME)).thenReturn(Optional.of(avroSchema));

        assertThrows(HttpBadRequestProblemException.class, () -> this.handler.create(new ArdqRegistrationDto().ardqId(ARDQ_ID).ardqUrl("url")
                .rules(List.of(new ArdqAugmentationRuleDto().inputSchema(
                                INPUT_SCHEMA_REFERENCE)
                        .fields(List.of(new ArdqAugmentationFieldDto().input(
                                        List.of("field1",
                                                "field2"))
                                .output("field3")))))));
    }

    @Test
    void test_updateAugmentSchema() throws RestClientException, IOException, AasValidationException {
        final AvroSchema avroSchema = this.getDummyAvroSchema();

        when(this.dataCatalogService.retrieveSchemaMetadata(INPUT_SCHEMA_REFERENCE_OBJ)).thenReturn(DATA_TYPE_RESPONSE_DTO);
        when(this.dataCatalogService.retrieveSchemaMetadata(OUTPUT_SCHEMA_REFERENCE_OBJ)).thenReturn(DATA_TYPE_RESPONSE_DTO);
        when(this.outputSchemaNameProvider.generate(ARDQ_ID, INPUT_SCHEMA_NAME)).thenReturn(OUTPUT_SCHEMA_NAME);
        when(this.schemaRegistryService.getLatestSchema(SUBJECT_NAME)).thenReturn(Optional.of(avroSchema));
        when(this.ardqRegistrationDao.findByArdqId(anyString())).thenReturn(Optional.ofNullable(ONE_RULE_REGISTRATION_DTO));

        final Schema outputSchemaDb =
                SchemaBuilder.record("cardq_schema_from_DMM").namespace(SCHEMA_NAMESPACE).fields()
                        .name(INPUT_FIELD1).type().stringType().noDefault()
                        .name(INPUT_FIELD2).type().stringType().noDefault()
                        .name(OUTPUT_FIELD1).type().stringType().noDefault()
                        .endRecord();
        when(this.schemaDao.getSchema(OUTPUT_SCHEMA_REFERENCE))
                .thenReturn(Optional.of(outputSchemaDb));

        this.handler.update(TWO_RULES_REGISTRATION_DTO);

        verify(this.dataCatalogService, times(1)).retrieveSchemaMetadata(INPUT_SCHEMA_REFERENCE_OBJ);
        verify(this.dataCatalogService, times(1)).retrieveSchemaMetadata(OUTPUT_SCHEMA_REFERENCE_OBJ);
        verify(this.schemaRegistryService, times(1)).getLatestSchema(SUBJECT_NAME);
        verify(this.schemaDao, times(1)).getSchema(OUTPUT_SCHEMA_REFERENCE);
        verify(this.dataCatalogService, times(0)).register(any());
        verify(this.dataCatalogService, times(1)).update(any());
        verify(this.schemaDao, times(1)).saveSchema(eq(INPUT_SCHEMA_REFERENCE), any());
        verify(this.schemaDao, times(1))
                .saveSchema(
                        eq(OUTPUT_SCHEMA_REFERENCE),
                        eq(new Schema.Parser().parse(
                                "{\"type\":\"record\",\"name\":\"cardq_schema_from_DMM\",\"namespace\":\"5G.CORE.PM_COUNTERS\","
                                        + "\"fields\":[{\"name\":\"inputField1\",\"type\":\"string\"},{\"name\":\"inputField2\","
                                        + "\"type\":\"string\"},{\"name\":\"outputField1\",\"type\":\"string\"},{\"name\":\"outputField2\","
                                        + "\"type\":[\"null\",\"string\"],\"default\":null}]}"))
                );
    }

    @Test
    void test_updateAugmentSchema_outputFieldAlreadyExists_inputFieldsConflict() throws RestClientException, IOException {
        final AvroSchema avroSchema = this.getDummyAvroSchema();

        when(this.dataCatalogService.retrieveSchemaMetadata(INPUT_SCHEMA_REFERENCE_OBJ)).thenReturn(DATA_TYPE_RESPONSE_DTO);
        when(this.schemaRegistryService.getLatestSchema(SUBJECT_NAME)).thenReturn(Optional.of(avroSchema));
        when(this.ardqRegistrationDao.findByArdqId(anyString())).thenReturn(Optional.ofNullable(
                new ArdqRegistrationDto().ardqId(ARDQ_ID).ardqUrl(ARDQ_URL).addRulesItem(
                        new ArdqAugmentationRuleDto().inputSchema(INPUT_SCHEMA_REFERENCE)
                                .fields(List.of(new ArdqAugmentationFieldDto().input(List.of(INPUT_FIELD1, "inputField5")).output(OUTPUT_FIELD1))))));

        assertThrows(HttpConflictRequestProblemException.class, () -> this.handler.update(TWO_RULES_REGISTRATION_DTO));

        verify(this.dataCatalogService, times(1)).retrieveSchemaMetadata(INPUT_SCHEMA_REFERENCE_OBJ);
        verify(this.schemaRegistryService, times(1)).getLatestSchema(SUBJECT_NAME);
        verify(this.schemaDao, times(0)).getSchema(any());
        verify(this.schemaDao, times(0)).saveSchema(any(), any());
    }

    @Test
    void test_updateSchema_targetSchemaNotExist() throws RestClientException, IOException {
        final AvroSchema avroSchema = this.getDummyAvroSchema();

        when(this.dataCatalogService.retrieveSchemaMetadata(INPUT_SCHEMA_REFERENCE_OBJ)).thenReturn(DATA_TYPE_RESPONSE_DTO);
        when(this.outputSchemaNameProvider.generate(ARDQ_ID, INPUT_SCHEMA_NAME)).thenReturn(OUTPUT_SCHEMA_NAME);
        when(this.schemaRegistryService.getLatestSchema(SUBJECT_NAME)).thenReturn(Optional.of(avroSchema));
        when(this.ardqRegistrationDao.findByArdqId(anyString()))
                .thenReturn(Optional.of(new ArdqRegistrationDto().ardqId(ARDQ_ID).ardqUrl(ARDQ_URL)
                        .addRulesItem(new ArdqAugmentationRuleDto().inputSchema("G5|PM_COUNTERS|oldSchema")
                                .addFieldsItem(FIELD_DTO1))));

        this.handler.update(ONE_RULE_REGISTRATION_DTO);

        verify(this.dataCatalogService, times(1)).retrieveSchemaMetadata(INPUT_SCHEMA_REFERENCE_OBJ);
        verify(this.dataCatalogService, times(1)).retrieveSchemaMetadata(OUTPUT_SCHEMA_REFERENCE_OBJ);
        verify(this.schemaRegistryService, times(1)).getLatestSchema(SUBJECT_NAME);
        verify(this.schemaDao, times(1)).getSchema(OUTPUT_SCHEMA_REFERENCE);
        verify(this.dataCatalogService, times(1)).register(any());
        verify(this.schemaDao, times(1)).saveSchema(eq(INPUT_SCHEMA_REFERENCE), any());
        verify(this.schemaDao, times(1))
                .saveSchema(eq(OUTPUT_SCHEMA_REFERENCE),
                        eq(new Schema.Parser().parse(
                                "{\"type\":\"record\",\"name\":\"cardq_AMF_Mobility_NetworkSlice_1\",\"namespace\":\"5G.CORE.PM_COUNTERS\","
                                        + "\"fields\":[{\"name\":\"inputField1\",\"type\":\"string\"},{\"name\":\"inputField2\","
                                        + "\"type\":\"string\"},{\"name\":\"inputField3\",\"type\":\"string\"},{\"name\":\"inputField4\","
                                        + "\"type\":\"string\"},{\"name\":\"outputField1\",\"type\":[\"null\",\"string\"],\"default\":null}]}"))
                );
    }

    @Test
    void test_createOutputSchema() {
        final List<ArdqAugmentationFieldDto> fieldDtoList = new ArrayList<>();
        final Schema inputSchema = SchemaBuilder.record("sampleSchema").fields().name("input").type("string").noDefault().endRecord();
        final Schema outputSchema = AugmentSchemaHandler.createOutputSchema(inputSchema, OUTPUT_SCHEMA_NAME, fieldDtoList);
        assertEquals(OUTPUT_SCHEMA_NAME, outputSchema.getName());
        assertEquals(inputSchema.getFields(), outputSchema.getFields());
        assertEquals(1, outputSchema.getFields().size());
        assertEquals(Schema.Type.STRING, outputSchema.getFields().get(0).schema().getType());
    }

    @Test
    void test_null_schema_metadata() {
        when(this.dataCatalogService.retrieveSchemaMetadata(INPUT_SCHEMA_REFERENCE_OBJ)).thenReturn(null);
        assertThrows(HttpBadRequestProblemException.class, () -> this.handler.create(ONE_RULE_REGISTRATION_DTO));
    }

    @Test
    void test_invalid_schema_specification_reference() {
        final DataTypeResponseDto metaData = DataTypeResponseDto.builder().build();
        final MessageSchemaResponseDto messageSchema = MessageSchemaResponseDto.builder().build();
        messageSchema.setSpecificationReference("specification_reference");
        metaData.setMessageSchema(messageSchema);

        when(this.dataCatalogService.retrieveSchemaMetadata(INPUT_SCHEMA_REFERENCE_OBJ)).thenReturn(metaData);
        assertThrows(HttpInternalServerErrorException.class, () -> this.handler.create(ONE_RULE_REGISTRATION_DTO));
    }

    @Test
    void test_retrieveInputSchema_io_exception() throws RestClientException, IOException {
        when(this.dataCatalogService.retrieveSchemaMetadata(INPUT_SCHEMA_REFERENCE_OBJ)).thenReturn(DATA_TYPE_RESPONSE_DTO);
        when(this.schemaRegistryService.getLatestSchema(SUBJECT_NAME)).thenThrow(IOException.class);
        assertThrows(HttpServiceUnavailableException.class, () -> this.handler.create(ONE_RULE_REGISTRATION_DTO));
    }

    @Test
    void test_retrieveInputSchema_rest_client_exception() throws RestClientException, IOException {
        when(this.dataCatalogService.retrieveSchemaMetadata(INPUT_SCHEMA_REFERENCE_OBJ)).thenReturn(DATA_TYPE_RESPONSE_DTO);
        when(this.schemaRegistryService.getLatestSchema(SUBJECT_NAME)).thenThrow(RestClientException.class);
        assertThrows(HttpInternalServerErrorException.class, () -> this.handler.create(ONE_RULE_REGISTRATION_DTO));
    }

    @Test
    void test_retrieveInputSchema_jsonProcessingException() throws IOException {
        when(this.dataCatalogService.retrieveSchemaMetadata(INPUT_SCHEMA_REFERENCE_OBJ)).thenReturn(DATA_TYPE_RESPONSE_DTO);
        when(this.mapper.writeValueAsString(DATA_TYPE_RESPONSE_DTO)).thenThrow(JsonProcessingException.class);
        assertThrows(HttpInternalServerErrorException.class, () -> this.handler.create(ONE_RULE_REGISTRATION_DTO));
    }

    @Test
    void test_retrieveInputSchema_fail_to_return_schema() {
        when(this.dataCatalogService.retrieveSchemaMetadata(INPUT_SCHEMA_REFERENCE_OBJ)).thenReturn(DATA_TYPE_RESPONSE_DTO);
        assertThrows(HttpInternalServerErrorException.class, () -> this.handler.create(ONE_RULE_REGISTRATION_DTO));
    }

    @Test
    void test_addOutputFieldsToTargetSchema() {
        final Schema inputSchema =
                SchemaBuilder.record("input_schema").namespace("aas").fields()
                        .name("existing_field_1").type().stringType().noDefault()
                        .name("existing_field_2").type().stringType().noDefault()
                        .endRecord();
        final ArdqAugmentationFieldDto fieldDto1 = new ArdqAugmentationFieldDto().output("sampleAugmentedField_1");
        final ArdqAugmentationFieldDto fieldDto2 = new ArdqAugmentationFieldDto().output("sampleAugmentedField_2");
        final List<ArdqAugmentationFieldDto> ardqAugmentationFieldDtoList = List.of(fieldDto1, fieldDto2);

        final Schema outputSchema = AugmentSchemaHandler.createOutputSchema(inputSchema, OUTPUT_SCHEMA_NAME, ardqAugmentationFieldDtoList);

        assertEquals(4, outputSchema.getFields().size());
        assertEquals("sampleAugmentedField_1", outputSchema.getFields().get(2).name());
        assertEquals(Schema.Type.UNION, outputSchema.getFields().get(2).schema().getType());
        assertEquals("sampleAugmentedField_2", outputSchema.getFields().get(3).name());
        assertEquals(Schema.Type.UNION, outputSchema.getFields().get(3).schema().getType());
    }

    @Test
    void test_addOutputFieldsToTargetSchema_alreadyHavingSameField() {
        final Schema inputSchema =
                SchemaBuilder.record("input_schema").namespace("aas").fields()
                        .name("existing_field_1").type().stringType().noDefault()
                        .name("existing_field_2").type().stringType().noDefault()
                        .endRecord();
        final ArdqAugmentationFieldDto fieldDto1 = new ArdqAugmentationFieldDto().output("sampleAugmentedField_1");
        final ArdqAugmentationFieldDto fieldDto2 = new ArdqAugmentationFieldDto().output("existing_field_2");
        final List<ArdqAugmentationFieldDto> ardqAugmentationFieldDtoList = List.of(fieldDto1, fieldDto2);

        final Schema outputSchema = AugmentSchemaHandler.createOutputSchema(inputSchema, OUTPUT_SCHEMA_NAME, ardqAugmentationFieldDtoList);

        assertEquals(3, outputSchema.getFields().size());
        assertEquals("sampleAugmentedField_1", outputSchema.getFields().get(2).name());
        assertEquals(Schema.Type.UNION, outputSchema.getFields().get(2).schema().getType());
    }

    @Test
    void test_addInputFieldsToTargetSchema_AddingNewField() {
        final Schema inputSchema =
                SchemaBuilder.record("input_schema").namespace("aas").fields()
                        .name("existing_field_1").type().stringType().noDefault()
                        .endRecord();
        final ArdqAugmentationFieldDto fieldDto1 = new ArdqAugmentationFieldDto().output("sampleAugmentedField_1");
        final ArdqAugmentationFieldDto fieldDto2 = new ArdqAugmentationFieldDto().output("existing_field_2");
        final List<ArdqAugmentationFieldDto> ardqAugmentationFieldDtoList = List.of(fieldDto1, fieldDto2);

        AugmentSchemaHandler.createOutputSchema(inputSchema, OUTPUT_SCHEMA_NAME, ardqAugmentationFieldDtoList);

        final Schema updatedInputSchema =
                SchemaBuilder.record("input_schema").namespace("aas").fields()
                        .name("existing_field_1").type().stringType().noDefault()
                        .name("added_field").type().stringType().noDefault()
                        .endRecord();

        final Schema outputSchema = AugmentSchemaHandler.createOutputSchema(updatedInputSchema, OUTPUT_SCHEMA_NAME,
                ardqAugmentationFieldDtoList);

        assertEquals(4, outputSchema.getFields().size());
        assertEquals("sampleAugmentedField_1", outputSchema.getFields().get(2).name());
        assertEquals(Schema.Type.UNION, outputSchema.getFields().get(2).schema().getType());
        assertEquals("existing_field_2", outputSchema.getFields().get(3).name());
        assertEquals(Schema.Type.UNION, outputSchema.getFields().get(3).schema().getType());
    }

    @Test
    void test_getFieldsPerInputSchemaReference() {
        final String inputSchemaReference2 = "inputSchemaReference2";

        final Map<String, List<ArdqAugmentationFieldDto>> expectedResult = new HashMap<>();
        expectedResult.put(INPUT_SCHEMA_REFERENCE, FIELDS);
        expectedResult.put(inputSchemaReference2, FIELDS);

        final ArdqRegistrationDto ardqRegistrationDtoA = new ArdqRegistrationDto().ardqId(ARDQ_ID).ardqUrl(ARDQ_URL)
                .addRulesItem(new ArdqAugmentationRuleDto().inputSchema(INPUT_SCHEMA_REFERENCE).fields(FIELDS))
                .addRulesItem(new ArdqAugmentationRuleDto().inputSchema(inputSchemaReference2).fields(FIELDS));
        final ArdqRegistrationDto ardqRegistrationDtoB = new ArdqRegistrationDto().ardqId(ARDQ_ID).ardqUrl(ARDQ_URL)
                .addRulesItem(new ArdqAugmentationRuleDto().inputSchema(INPUT_SCHEMA_REFERENCE).addFieldsItem(FIELD_DTO1))
                .addRulesItem(new ArdqAugmentationRuleDto().inputSchema(INPUT_SCHEMA_REFERENCE).addFieldsItem(FIELD_DTO2))
                .addRulesItem(new ArdqAugmentationRuleDto().inputSchema(inputSchemaReference2).fields(FIELDS));

        assertEquals(expectedResult, AugmentSchemaHandler.getFieldsPerInputSchemaReferenceMap(ardqRegistrationDtoA));
        assertEquals(expectedResult, AugmentSchemaHandler.getFieldsPerInputSchemaReferenceMap(ardqRegistrationDtoB));
    }

    @Test
    void test_retrieveInputSchema_NotFound_throwBadRequest() {
        when(this.dataCatalogService.retrieveSchemaMetadata(INPUT_SCHEMA_REFERENCE_OBJ)).thenThrow(HttpNotFoundRequestProblemException.class);

        assertThrows(HttpBadRequestProblemException.class, () -> this.handler.create(ONE_RULE_REGISTRATION_DTO));
        verify(this.dataCatalogService, times(1)).retrieveSchemaMetadata(INPUT_SCHEMA_REFERENCE_OBJ);
    }

    private AvroSchema getDummyAvroSchema() {
        return new AvroSchema(INPUT_SCHEMA);
    }

    @Test
    void getSchemaReferenceObject_invalidInput_throwException() {
        assertThrows(HttpBadRequestProblemException.class, () -> {
            AugmentSchemaHandler.parseSchemaReference("invalidInputSchemaRef");
        });
    }

    @Test
    void getSchemaReferenceObject_validInput_noException() {
        assertDoesNotThrow(() -> AugmentSchemaHandler.parseSchemaReference(INPUT_SCHEMA_REFERENCE));
    }

    @Test
    void getSchemaSubject_invalidInput_throwException() {
        assertThrows(HttpBadRequestProblemException.class, () -> {
            final Schema invalidSchema = SchemaBuilder.record("schemaName").namespace("BAD namespace").fields().endRecord();
            AugmentSchemaHandler.parseSchemaSubject(invalidSchema);
        });
    }

    @Test
    void getSchemaSubjectObject_validInput_noException() {
        assertDoesNotThrow(() -> AugmentSchemaHandler.parseSchemaSubject(OUTPUT_SCHEMA));
        assertDoesNotThrow(() -> AugmentSchemaHandler.parseSchemaSubject(INPUT_SCHEMA));
    }

    @Test
    void createSpecificationReference() {

        final SpecificationReference specificationReference = AugmentSchemaHandler.createSpecificationReference(DATA_TYPE_RESPONSE_DTO,
                OUTPUT_SCHEMA_REFERENCE_OBJ);

        assertEquals("PM_COUNTERS", specificationReference.getDataCategory());
        assertEquals("CORE", specificationReference.getDataProvider());
        assertEquals("5G", specificationReference.getDataSpace());
        assertEquals("cardq_AMF_Mobility_NetworkSlice_1", specificationReference.getSchemaName());
        assertEquals("5G.CORE.PM_COUNTERS.cardq_AMF_Mobility_NetworkSlice_1", specificationReference.getSchemaSubject());
    }

    @Test
    void test_resyncAugmentSchema_noChangeRequired() throws RestClientException, IOException {

        when(this.schemaDao.getIOSchemas(ARDQ_ID)).thenReturn(List.of(new IoSchema(ARDQ_ID, INPUT_SCHEMA_REFERENCE, OUTPUT_SCHEMA_REFERENCE)));
        when(this.schemaDao.getSchema(OUTPUT_SCHEMA_REFERENCE)).thenReturn(Optional.of(OUTPUT_SCHEMA));
        when(this.schemaRegistryService.isSchemaFound(any(), any())).thenReturn(true);

        this.handler.resync(TWO_RULES_REGISTRATION_DTO);

        verify(this.dataCatalogService, times(0)).retrieveSchemaMetadata(INPUT_SCHEMA_REFERENCE_OBJ);
        verify(this.schemaRegistryService, times(0)).getVersion(any(), any());
        verify(this.schemaRegistryService, times(1)).isSchemaFound(any(), any());
    }

    @Test
    void test_resyncAugmentSchema_updateDMM() throws RestClientException, IOException {

        when(this.schemaDao.getIOSchemas(ARDQ_ID)).thenReturn(List.of(new IoSchema(ARDQ_ID, INPUT_SCHEMA_REFERENCE, OUTPUT_SCHEMA_REFERENCE)));
        when(this.schemaDao.getSchema(OUTPUT_SCHEMA_REFERENCE)).thenReturn(Optional.of(OUTPUT_SCHEMA));
        when(this.schemaRegistryService.isSchemaFound(any(), any())).thenReturn(false);

        final AvroSchema avroSchema = this.getDummyAvroSchema();
        when(this.dataCatalogService.retrieveSchemaMetadata(INPUT_SCHEMA_REFERENCE_OBJ)).thenReturn(DATA_TYPE_RESPONSE_DTO);
        when(this.schemaDao.getIOSchemas(ARDQ_ID)).thenReturn(List.of(new IoSchema(ARDQ_ID, INPUT_SCHEMA_REFERENCE, OUTPUT_SCHEMA_REFERENCE)));
        when(this.schemaDao.getSchema(OUTPUT_SCHEMA_REFERENCE)).thenReturn(Optional.of(OUTPUT_SCHEMA));
        when(this.schemaRegistryService.getVersion(any(), any())).thenReturn(1);

        this.handler.resync(TWO_RULES_REGISTRATION_DTO);

        verify(this.dataCatalogService, times(1)).retrieveSchemaMetadata(INPUT_SCHEMA_REFERENCE_OBJ);
        verify(this.dataCatalogService, times(1)).retrieveSchemaMetadata(OUTPUT_SCHEMA_REFERENCE_OBJ);

        verify(this.schemaRegistryService, times(1)).register(any(), any());
        verify(this.dataCatalogService, times(1)).register(any());
    }

    @Test
    void test_resyncAugmentSchema_internalError() throws RestClientException, IOException {

        when(this.schemaDao.getIOSchemas(ARDQ_ID)).thenReturn(List.of(new IoSchema(ARDQ_ID, INPUT_SCHEMA_REFERENCE, OUTPUT_SCHEMA_REFERENCE)));
        when(this.schemaDao.getSchema(OUTPUT_SCHEMA_REFERENCE)).thenReturn(Optional.empty());

        this.handler.resync(TWO_RULES_REGISTRATION_DTO);

        assertEquals(1, this.listAppender.list.size());
        assertEquals(Level.ERROR, this.listAppender.list.get(0).getLevel());
    }

    @Test
    void test_resyncAugmentSchema_internalErrorDueToSchemaRegistryException() throws RestClientException, IOException {

        when(this.schemaDao.getIOSchemas(ARDQ_ID)).thenReturn(List.of(new IoSchema(ARDQ_ID, INPUT_SCHEMA_REFERENCE, OUTPUT_SCHEMA_REFERENCE)));
        when(this.schemaDao.getSchema(OUTPUT_SCHEMA_REFERENCE)).thenReturn(Optional.of(OUTPUT_SCHEMA));
        when(this.schemaRegistryService.isSchemaFound(any(), any())).thenThrow(RestClientException.class);

        this.handler.resync(TWO_RULES_REGISTRATION_DTO);

        verify(this.faultHandler, times(1)).fatal(any(), any());
    }
}

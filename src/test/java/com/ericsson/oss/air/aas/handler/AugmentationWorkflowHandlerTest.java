/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.aas.handler;

import static com.ericsson.oss.air.aas.handler.RegistrationTestUtil.ARDQ_ID;
import static com.ericsson.oss.air.aas.handler.RegistrationTestUtil.DATA_SERVICE_RESPONSE_DTO;
import static com.ericsson.oss.air.aas.handler.RegistrationTestUtil.DATA_TYPE_RESPONSE_DTO;
import static com.ericsson.oss.air.aas.handler.RegistrationTestUtil.DEPRECATED_SCHEMA_REFERENCE;
import static com.ericsson.oss.air.aas.handler.RegistrationTestUtil.FIELDS;
import static com.ericsson.oss.air.aas.handler.RegistrationTestUtil.INPUT_SCHEMA_REFERENCE;
import static com.ericsson.oss.air.aas.handler.RegistrationTestUtil.INPUT_SCHEMA_REFERENCE_OBJ;
import static com.ericsson.oss.air.aas.handler.RegistrationTestUtil.MESSAGE_DATA_TOPIC_RESPONSE_DTO;
import static com.ericsson.oss.air.aas.handler.RegistrationTestUtil.OUTPUT_SCHEMA;
import static com.ericsson.oss.air.aas.handler.RegistrationTestUtil.OUTPUT_SCHEMA_REFERENCE;
import static com.ericsson.oss.air.aas.handler.RegistrationTestUtil.SCHEMA_SUBJECT_OBJ;
import static com.ericsson.oss.air.aas.handler.RegistrationTestUtil.VALID_REGISTRATION_DTO;
import static com.ericsson.oss.air.aas.handler.RegistrationTestUtil.VALID_REGISTRATION_WITH_ARDQTYPE_DTO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.ericsson.oss.air.aas.config.AugmentationProcessorFactory;
import com.ericsson.oss.air.aas.config.kafka.dynamic.APKafkaConsumerRegistrar;
import com.ericsson.oss.air.aas.config.kafka.dynamic.APMessageListener;
import com.ericsson.oss.air.aas.config.kafka.dynamic.APMessageListenerEndpointFactory;
import com.ericsson.oss.air.aas.model.datacatalog.response.DataTypeResponseDto;
import com.ericsson.oss.air.aas.model.datacatalog.response.MessageDataTopicResponseDto;
import com.ericsson.oss.air.aas.model.datacatalog.response.MessageSchemaResponseDto;
import com.ericsson.oss.air.aas.model.record.SchemaSubject;
import com.ericsson.oss.air.aas.repository.ArdqRegistrationDao;
import com.ericsson.oss.air.aas.repository.SchemaDao;
import com.ericsson.oss.air.aas.service.schema.DataCatalogService;
import com.ericsson.oss.air.aas.service.schema.SchemaRegistryService;
import com.ericsson.oss.air.exception.http.problem.exception.HttpProblemException;
import com.ericsson.oss.air.util.logging.FaultHandler;
import io.confluent.kafka.schemaregistry.avro.AvroSchema;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import io.micrometer.core.instrument.Counter;
import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class AugmentationWorkflowHandlerTest {

    @Mock
    private ArdqRegistrationDao ardqRegistrationDao;

    @Mock
    private APMessageListenerEndpointFactory apMessageListenerEndpointFactory;

    @Mock
    private APKafkaConsumerRegistrar apKafkaConsumerRegistrar;

    @Mock
    private AugmentationProcessorFactory augmentationProcessorFactory;

    @Mock
    private SchemaDao schemaDao;

    @Mock
    private DataCatalogService dataCatalogService;

    @Mock
    private Counter augmentedInputRecordsCounter;

    @Mock
    private Counter nonAugmentedInputRecordsCounter;

    @Mock
    private AugmentationProcessor augmentationProcessor;

    @Mock
    private SchemaRegistryService schemaRegistryService;

    @Mock
    private FaultHandler faultHandler;

    @InjectMocks
    private AugmentationWorkflowHandler augmentationWorkflowHandler;

    @Test
    void mapConsumerId_general_pass() {
        assertEquals("A-B-C-D", AugmentationWorkflowHandler.mapConsumerId("/A/B/C/D"));
        assertEquals("A-B-C-D", AugmentationWorkflowHandler.mapConsumerId("A/B/C/D"));
        assertEquals("A-B-C-DRandomMessage", AugmentationWorkflowHandler.mapConsumerId("A/B/C/D Random Message !"));
        assertEquals("DATA-DATA2-TTT-1-2-TEST", AugmentationWorkflowHandler.mapConsumerId("DATA|DATA2|TTT_1_2-TEST"));
    }

    @Test
    void stop_general_pass() {

        final String consumerId = "5G-DEPRECATED-AMF-Mobility-NetworkSlice-1";
        final APMessageListener messageListener = new APMessageListener(List.of(this.augmentationProcessor), SCHEMA_SUBJECT_OBJ,
                this.augmentedInputRecordsCounter, this.nonAugmentedInputRecordsCounter);

        when(this.apMessageListenerEndpointFactory.getMessageListener(consumerId)).thenReturn(messageListener);
        when(this.apKafkaConsumerRegistrar.isRunning(consumerId)).thenReturn(true);

        assertEquals(1, messageListener.getAugmentationProcessorList().size());
        this.augmentationWorkflowHandler.stop(DEPRECATED_SCHEMA_REFERENCE);

        verify(this.apKafkaConsumerRegistrar, times(1)).pauseConsumer(consumerId);
        assertEquals(0, messageListener.getAugmentationProcessorList().size());
    }

    @Test
    void stop_noContainerRunning_pass() {

        final String consumerId = "5G-DEPRECATED-AMF-Mobility-NetworkSlice-1";

        when(this.apMessageListenerEndpointFactory.getMessageListener(consumerId)).thenReturn(
                new APMessageListener(new ArrayList<>(), SCHEMA_SUBJECT_OBJ, this.augmentedInputRecordsCounter,
                        this.nonAugmentedInputRecordsCounter));

        when(this.apKafkaConsumerRegistrar.isRunning(consumerId)).thenReturn(false);
        this.augmentationWorkflowHandler.stop(DEPRECATED_SCHEMA_REFERENCE);

        verify(this.apKafkaConsumerRegistrar, never()).pauseConsumer(consumerId);
    }

    @Test
    void create_general_pass() {

        when(this.ardqRegistrationDao.retrieveRegistrationsByInputSchemaRef(INPUT_SCHEMA_REFERENCE)).thenReturn(
                Optional.of(List.of(VALID_REGISTRATION_DTO)));
        when(this.schemaDao.getOutputSchemaReference(ARDQ_ID, INPUT_SCHEMA_REFERENCE)).thenReturn(Optional.of(OUTPUT_SCHEMA_REFERENCE));
        when(this.schemaDao.getSchema(OUTPUT_SCHEMA_REFERENCE)).thenReturn(Optional.of(OUTPUT_SCHEMA));
        when(this.ardqRegistrationDao.getAugmentationFields(ARDQ_ID, INPUT_SCHEMA_REFERENCE)).thenReturn(Optional.of(FIELDS));
        when(this.dataCatalogService.retrieveSchemaMetadata(INPUT_SCHEMA_REFERENCE_OBJ)).thenReturn(DATA_TYPE_RESPONSE_DTO);

        this.augmentationWorkflowHandler.create(INPUT_SCHEMA_REFERENCE);

        verify(this.apKafkaConsumerRegistrar, times(1)).createConsumer(anyString(), anyString(), any(SchemaSubject.class), anyList());
    }

    @Test
    void create_invalidInputSchemaReference_noException() {

        this.augmentationWorkflowHandler.create("RANDOM");
        verify(this.apKafkaConsumerRegistrar, never()).createConsumer(anyString(), anyString(), any(SchemaSubject.class), anyList());
    }

    @Test
    void create_topicNameDoNotExist_noException() {

        //Mock new DataTypeResponseDto with topic name null
        final MessageDataTopicResponseDto topicDto = MESSAGE_DATA_TOPIC_RESPONSE_DTO.toBuilder()
                .name(null)
                .build();

        final MessageSchemaResponseDto schemaDto = MessageSchemaResponseDto.builder()
                .messageDataTopic(topicDto)
                .dataService(DATA_SERVICE_RESPONSE_DTO)
                .build();

        final DataTypeResponseDto invalidDataTypeResponseDto = DATA_TYPE_RESPONSE_DTO.toBuilder()
                .messageSchema(schemaDto)
                .build();

        when(this.dataCatalogService.retrieveSchemaMetadata(INPUT_SCHEMA_REFERENCE_OBJ)).thenReturn(invalidDataTypeResponseDto);

        this.augmentationWorkflowHandler.create(INPUT_SCHEMA_REFERENCE);

        verify(this.apKafkaConsumerRegistrar, never()).createConsumer(anyString(), anyString(), any(SchemaSubject.class), anyList());
    }

    @Test
    void create_general_schemaDoNotExist_noException() {

        when(this.ardqRegistrationDao.retrieveRegistrationsByInputSchemaRef(INPUT_SCHEMA_REFERENCE)).thenReturn(
                Optional.of(List.of(VALID_REGISTRATION_DTO)));
        when(this.schemaDao.getOutputSchemaReference(ARDQ_ID, INPUT_SCHEMA_REFERENCE)).thenReturn(Optional.of(OUTPUT_SCHEMA_REFERENCE));
        when(this.schemaDao.getSchema(OUTPUT_SCHEMA_REFERENCE)).thenReturn(Optional.empty());
        when(this.dataCatalogService.retrieveSchemaMetadata(INPUT_SCHEMA_REFERENCE_OBJ)).thenReturn(DATA_TYPE_RESPONSE_DTO);

        this.augmentationWorkflowHandler.create(INPUT_SCHEMA_REFERENCE);

        verify(this.apKafkaConsumerRegistrar, never()).createConsumer(anyString(), anyString(), any(SchemaSubject.class), anyList());
    }

    @Test
    void create_general_fieldsDoNotExist_noException() {

        when(this.ardqRegistrationDao.retrieveRegistrationsByInputSchemaRef(INPUT_SCHEMA_REFERENCE)).thenReturn(
                Optional.of(List.of(VALID_REGISTRATION_DTO)));
        when(this.schemaDao.getOutputSchemaReference(ARDQ_ID, INPUT_SCHEMA_REFERENCE)).thenReturn(Optional.of(OUTPUT_SCHEMA_REFERENCE));
        when(this.schemaDao.getSchema(OUTPUT_SCHEMA_REFERENCE)).thenReturn(Optional.of(OUTPUT_SCHEMA));
        when(this.ardqRegistrationDao.getAugmentationFields(ARDQ_ID, INPUT_SCHEMA_REFERENCE)).thenReturn(Optional.empty());
        when(this.dataCatalogService.retrieveSchemaMetadata(INPUT_SCHEMA_REFERENCE_OBJ)).thenReturn(DATA_TYPE_RESPONSE_DTO);

        this.augmentationWorkflowHandler.create(INPUT_SCHEMA_REFERENCE);

        verify(this.apKafkaConsumerRegistrar, times(1)).createConsumer(anyString(), anyString(), any(SchemaSubject.class), anyList());
    }

    @Test
    void update_general_pass() {

        final APMessageListener apMessageListener = new APMessageListener(new ArrayList<>(), SCHEMA_SUBJECT_OBJ,
                this.augmentedInputRecordsCounter, this.nonAugmentedInputRecordsCounter);
        final String consumerId = "5G-PM-COUNTERS-AMF-Mobility-NetworkSlice-1";

        when(this.ardqRegistrationDao.retrieveRegistrationsByInputSchemaRef(INPUT_SCHEMA_REFERENCE)).thenReturn(
                Optional.of(List.of(VALID_REGISTRATION_DTO)));
        when(this.schemaDao.getOutputSchemaReference(ARDQ_ID, INPUT_SCHEMA_REFERENCE)).thenReturn(Optional.of(OUTPUT_SCHEMA_REFERENCE));
        when(this.schemaDao.getSchema(OUTPUT_SCHEMA_REFERENCE)).thenReturn(Optional.of(OUTPUT_SCHEMA));
        when(this.ardqRegistrationDao.getAugmentationFields(ARDQ_ID, INPUT_SCHEMA_REFERENCE)).thenReturn(Optional.of(FIELDS));
        when(this.apMessageListenerEndpointFactory.getMessageListener(anyString())).thenReturn(apMessageListener);
        when(this.apKafkaConsumerRegistrar.isRunning(consumerId)).thenReturn(true);

        assertEquals(0, apMessageListener.getAugmentationProcessorList().size());

        this.augmentationWorkflowHandler.update(INPUT_SCHEMA_REFERENCE);

        assertEquals(1, apMessageListener.getAugmentationProcessorList().size());

        verify(this.apKafkaConsumerRegistrar, never()).createConsumer(anyString(), anyString(), any(SchemaSubject.class), anyList());
        verify(this.apKafkaConsumerRegistrar, times(1)).pauseConsumer(anyString());
        verify(this.apKafkaConsumerRegistrar, times(1)).resumeConsumer(anyString());
    }

    @Test
    void update_pausedContainerWithNoAugmentationProcessor_pass() {

        final APMessageListener apMessageListener = new APMessageListener(new ArrayList<>(), SCHEMA_SUBJECT_OBJ,
                this.augmentedInputRecordsCounter, this.nonAugmentedInputRecordsCounter);
        final String consumerId = "5G-PM-COUNTERS-AMF-Mobility-NetworkSlice-1";

        when(this.apMessageListenerEndpointFactory.getMessageListener(anyString())).thenReturn(apMessageListener);
        when(this.apKafkaConsumerRegistrar.isRunning(consumerId)).thenReturn(false);

        this.augmentationWorkflowHandler.update(INPUT_SCHEMA_REFERENCE);

        verify(this.apKafkaConsumerRegistrar, times(0)).createConsumer(anyString(), anyString(), any(SchemaSubject.class), anyList());
        verify(this.apKafkaConsumerRegistrar, times(0)).resumeConsumer(anyString());
    }

    @Test
    void update_pausedContainer_pass() {

        final APMessageListener apMessageListener = new APMessageListener(new ArrayList<>(), SCHEMA_SUBJECT_OBJ,
                this.augmentedInputRecordsCounter, this.nonAugmentedInputRecordsCounter);
        final String consumerId = "5G-PM-COUNTERS-AMF-Mobility-NetworkSlice-1";

        when(this.ardqRegistrationDao.retrieveRegistrationsByInputSchemaRef(INPUT_SCHEMA_REFERENCE)).thenReturn(
                Optional.of(List.of(VALID_REGISTRATION_DTO)));
        when(this.schemaDao.getOutputSchemaReference(ARDQ_ID, INPUT_SCHEMA_REFERENCE)).thenReturn(Optional.of(OUTPUT_SCHEMA_REFERENCE));
        when(this.schemaDao.getSchema(OUTPUT_SCHEMA_REFERENCE)).thenReturn(Optional.of(OUTPUT_SCHEMA));
        when(this.apMessageListenerEndpointFactory.getMessageListener(anyString())).thenReturn(apMessageListener);
        when(this.apKafkaConsumerRegistrar.isRunning(consumerId)).thenReturn(false);

        this.augmentationWorkflowHandler.update(INPUT_SCHEMA_REFERENCE);

        verify(this.apKafkaConsumerRegistrar, times(0)).createConsumer(anyString(), anyString(), any(SchemaSubject.class), anyList());
        verify(this.apKafkaConsumerRegistrar, times(1)).resumeConsumer(anyString());
    }

    @Test
    void isCreated_general_pass() {

        this.augmentationWorkflowHandler.isCreated(INPUT_SCHEMA_REFERENCE);
        verify(this.apKafkaConsumerRegistrar, times(1)).isCreated(anyString());
    }

    @Test
    void createAugmentationProcessorList_schemaRegistry_exception() throws RestClientException, IOException {

        when(this.ardqRegistrationDao.retrieveRegistrationsByInputSchemaRef(INPUT_SCHEMA_REFERENCE)).thenReturn(
                Optional.of(List.of(VALID_REGISTRATION_DTO)));
        when(this.schemaDao.getOutputSchemaReference(ARDQ_ID, INPUT_SCHEMA_REFERENCE)).thenReturn(Optional.of(OUTPUT_SCHEMA_REFERENCE));
        when(this.schemaDao.getSchema(OUTPUT_SCHEMA_REFERENCE)).thenReturn(Optional.of(OUTPUT_SCHEMA));
        when(this.ardqRegistrationDao.getAugmentationFields(ARDQ_ID, INPUT_SCHEMA_REFERENCE)).thenReturn(Optional.of(FIELDS));
        when(this.schemaRegistryService.getVersion(anyString(), any(AvroSchema.class))).thenThrow(IOException.class);

        final List<AugmentationProcessor> augmentationProcessorList = this.augmentationWorkflowHandler.createAugmentationProcessorList(
                INPUT_SCHEMA_REFERENCE);

        assertEquals(0, augmentationProcessorList.size());
    }

    @Test
    void createAugmentationProcessorList_schemaSubject_exception() {

        final Schema schema = SchemaBuilder.record("output_schema").namespace("test").fields()
                .name("existing_field_1").type().stringType().noDefault()
                .endRecord();

        when(this.ardqRegistrationDao.retrieveRegistrationsByInputSchemaRef(INPUT_SCHEMA_REFERENCE)).thenReturn(
                Optional.of(List.of(VALID_REGISTRATION_DTO)));
        when(this.schemaDao.getOutputSchemaReference(ARDQ_ID, INPUT_SCHEMA_REFERENCE)).thenReturn(Optional.of(OUTPUT_SCHEMA_REFERENCE));
        when(this.schemaDao.getSchema(OUTPUT_SCHEMA_REFERENCE)).thenReturn(Optional.of(schema));

        final List<AugmentationProcessor> augmentationProcessorList = this.augmentationWorkflowHandler.createAugmentationProcessorList(
                INPUT_SCHEMA_REFERENCE);

        assertEquals(0, augmentationProcessorList.size());
    }

    @Test
    void createAugmentationProcessorList_noRelatedArdqRegistration_emptyList() {

        when(this.ardqRegistrationDao.retrieveRegistrationsByInputSchemaRef(INPUT_SCHEMA_REFERENCE)).thenReturn(Optional.empty());
        final List<AugmentationProcessor> augmentationProcessorList = this.augmentationWorkflowHandler.createAugmentationProcessorList(
                INPUT_SCHEMA_REFERENCE);
        assertEquals(0, augmentationProcessorList.size());
    }

    @Test
    void createAugmentationProcessorList_emptySchema_emptyList() {

        final List<AugmentationProcessor> augmentationProcessorList = this.augmentationWorkflowHandler.createAugmentationProcessorList(
                INPUT_SCHEMA_REFERENCE);
        assertEquals(0, augmentationProcessorList.size());
    }

    @Test
    void createAugmentationProcessorList_badFormatInputSchemaReference_emptyList() {

        when(this.ardqRegistrationDao.retrieveRegistrationsByInputSchemaRef("bad format")).thenReturn(
                Optional.of(List.of(VALID_REGISTRATION_WITH_ARDQTYPE_DTO)));
        final List<AugmentationProcessor> augmentationProcessorList = this.augmentationWorkflowHandler.createAugmentationProcessorList(
                "bad format");
        assertEquals(0, augmentationProcessorList.size());
    }

    @Test
    void getDCDataType_HttpProblemException_Caught() {

        when(this.dataCatalogService.retrieveSchemaMetadata(INPUT_SCHEMA_REFERENCE_OBJ)).thenThrow(
                new HttpProblemException(HttpStatus.BAD_REQUEST, "Bad Request", "Test Instance", null));
        assertTrue(this.augmentationWorkflowHandler.getDCDataType(INPUT_SCHEMA_REFERENCE_OBJ).isEmpty());
    }
}
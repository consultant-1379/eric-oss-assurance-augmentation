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

package com.ericsson.oss.air.aas.model.datacatalog.request;

import static com.ericsson.oss.air.aas.model.datacatalog.DataCatalogTestUtil.*;
import static org.junit.jupiter.api.Assertions.*;

import com.ericsson.oss.air.aas.model.datacatalog.EncodingEnum;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;

class MessageSchemaRequestDtoTest {

    @Test
    void build_CreateMessageSchema() throws JsonProcessingException {

        // Example from API documentation for creating a MessageSchema
        final String expectedStringWithWhitespace = "{ \"dataSpace\": { \"name\": \"4G\" }, " +
                "\"dataCategory\": { \"dataCategoryName\": \"CM_EXPORTS1\" }, " +
                "\"dataProviderType\": { \"providerTypeId\": \"vv101\", \"providerVersion\": \"Vv101\" }, " +
                "\"messageStatusTopic\": { \"encoding\": \"JSON\",  \"messageBusId\": 1, \"name\": \"topic102\", \"specificationReference\": \"SpecRef101\" }, " +
                "\"messageDataTopic\": { \"encoding\": \"JSON\", \"messageBusId\": 1, \"name\": \"topic102\" }, " +
                "\"messageSchema\": { \"specificationReference\": \"SpecRef101\" }, " +
                "\"dataService\": { \"dataServiceName\": \"dataservicename102\" }, " +
                "\"dataServiceInstance\": { \"dataServiceInstanceName\": \"dsinst101\", \"controlEndPoint\": \"http://localhost:8082\", \"consumedDataSpace\": \"4G\", \"consumedDataCategory\": \"4G\", \"consumedDataProvider\": \"4G\", \"consumedSchemaName\": \"SCH2\", \"consumedSchemaVersion\": \"2\" }, " +
                "\"supportedPredicateParameter\": { \"parameterName\": \"pd101\", \"isPassedToConsumedService\": true }, " +
                "\"dataType\": { \"mediumType\": \"stream\", \"schemaName\": \"SCH2\", \"schemaVersion\": \"2\", \"isExternal\": true, \"consumedDataSpace\": \"4G\", \"consumedDataCategory\": \"4G\", \"consumedDataProvider\": \"4G\", \"consumedSchemaName\": \"4G\", \"consumedSchemaVersion\": \"2\" } }";
        final String expectedString = expectedStringWithWhitespace.replaceAll("\\s", "");

        final DataSpaceRequestDto dataSpaceRequestDto = DataSpaceRequestDto.builder()
                .name(DATA_SPACE_4G)
                .build();
        final DataCategoryRequestDto dataCategoryRequestDto = DataCategoryRequestDto.builder()
                .dataCategoryName(DATA_CATEGORY_NAME)
                .build();
        final DataProviderTypeRequestDto dataProviderTypeRequestDto = DataProviderTypeRequestDto.builder()
                .providerVersion(PROVIDER_VERSION)
                .providerTypeId(PROVIDER_TYPE_ID)
                .build();
        final MessageStatusTopicRequestDto messageStatusTopicRequestDto = MessageStatusTopicRequestDto.builder()
                .name(TOPIC_NAME)
                .messageBusId(BUS_ID)
                .specificationReference(SPECIFICATION_REFERENCE)
                .encoding(EncodingEnum.JSON)
                .build();
        final MessageDataTopicRequestDto messageDataTopicRequestDto = MessageDataTopicRequestDto.builder()
                .name(TOPIC_NAME)
                .messageBusId(BUS_ID)
                .encoding(EncodingEnum.JSON)
                .build();
        final InnerMessageSchemaRequestDto innerMessageSchemaRequestDto = InnerMessageSchemaRequestDto.builder()
                .specificationReference(SPECIFICATION_REFERENCE)
                .build();
        final DataServiceRequestDto dataServiceRequestDto = DataServiceRequestDto.builder()
                .dataServiceName(DATA_SERVICE_NAME)
                .build();
        final DataServiceInstanceRequestDto dataServiceInstanceRequestDto = DataServiceInstanceRequestDto.builder()
                .dataServiceInstanceName(DATA_SERVICE_INSTANCE_NAME)
                .controlEndPoint(CONTROL_ENDPOINT)
                .consumedDataSpace(DATA_SPACE_4G)
                .consumedDataCategory(DATA_SPACE_4G)
                .consumedDataProvider(DATA_SPACE_4G)
                .consumedSchemaName(SCHEMA_NAME)
                .consumedSchemaVersion(SCHEMA_VERSION)
                .build();
        final SupportedPredicateParameterRequestDto supportedPredicateParameterRequestDto = SupportedPredicateParameterRequestDto.builder()
                .parameterName(PARAMETER_NAME)
                .isPassedToConsumedService(Boolean.TRUE)
                .build();
        final DataTypeRequestDto dataTypeRequestDto = DataTypeRequestDto.builder()
                .mediumType(MEDIUM_TYPE)
                .schemaName(SCHEMA_NAME)
                .schemaVersion(SCHEMA_VERSION)
                .isExternal(Boolean.TRUE)
                .consumedDataSpace(DATA_SPACE_4G)
                .consumedDataCategory(DATA_SPACE_4G)
                .consumedDataProvider(DATA_SPACE_4G)
                .consumedSchemaName(DATA_SPACE_4G)
                .consumedSchemaVersion(SCHEMA_VERSION)
                .build();

        final MessageSchemaRequestDto messageSchemaRequestDto = MessageSchemaRequestDto.builder()
                .dataSpace(dataSpaceRequestDto)
                .dataCategory(dataCategoryRequestDto)
                .dataProviderType(dataProviderTypeRequestDto)
                .messageStatusTopic(messageStatusTopicRequestDto)
                .messageDataTopic(messageDataTopicRequestDto)
                .messageSchema(innerMessageSchemaRequestDto)
                .dataService(dataServiceRequestDto)
                .dataServiceInstance(dataServiceInstanceRequestDto)
                .supportedPredicateParameter(supportedPredicateParameterRequestDto)
                .dataType(dataTypeRequestDto)
                .build();

        final MessageSchemaRequestDto deserializedObject = mapper.readValue(expectedString, MessageSchemaRequestDto.class);
        final String builtDtoString = mapper.writeValueAsString(messageSchemaRequestDto);

        assertEquals(messageSchemaRequestDto, deserializedObject);
        assertEquals(expectedString, builtDtoString);

    }

    @Test
    void build_UpdateMessageSchema() throws JsonProcessingException {

        final String UPDATED_CONTROL_ENDPOINT = "http://localhost:8083";

        final String expectedStringWithWhitespace = "{ \"id\":2, " +
                "\"dataSpace\": { \"name\": \"4G\" }, " +
                "\"dataCategory\": { \"dataCategoryName\": \"CM_EXPORTS1\" }, " +
                "\"dataProviderType\": { \"providerTypeId\": \"vv101\", \"providerVersion\": \"Vv101\" }, " +
                "\"messageStatusTopic\": { \"encoding\": \"JSON\",  \"messageBusId\": 1, \"name\": \"topic102\", \"specificationReference\": \"SpecRef101\" }, " +
                "\"messageDataTopic\": { \"encoding\": \"JSON\", \"messageBusId\": 1, \"name\": \"topic102\" }, " +
                "\"messageSchema\": { \"specificationReference\": \"SpecRef101\" }, " +
                "\"dataService\": { \"dataServiceName\": \"dataservicename102\" }, " +
                "\"dataServiceInstance\": { \"dataServiceInstanceName\": \"dsinst101\", \"controlEndPoint\": \"http://localhost:8083\", \"consumedDataSpace\": \"4G\", \"consumedDataCategory\": \"4G\", \"consumedDataProvider\": \"4G\", \"consumedSchemaName\": \"SCH2\", \"consumedSchemaVersion\": \"2\" }, " +
                "\"supportedPredicateParameter\": { \"parameterName\": \"pd101\", \"isPassedToConsumedService\": true }, " +
                "\"dataType\": { \"mediumType\": \"stream\", \"schemaName\": \"SCH2\", \"schemaVersion\": \"2\", \"isExternal\": true, \"consumedDataSpace\": \"4G\", \"consumedDataCategory\": \"4G\", \"consumedDataProvider\": \"4G\", \"consumedSchemaName\": \"4G\", \"consumedSchemaVersion\": \"2\" } }";
        final String expectedString = expectedStringWithWhitespace.replaceAll("\\s", "");

        final DataSpaceRequestDto dataSpaceRequestDto = DataSpaceRequestDto.builder()
                .name(DATA_SPACE_4G)
                .build();
        final DataCategoryRequestDto dataCategoryRequestDto = DataCategoryRequestDto.builder()
                .dataCategoryName(DATA_CATEGORY_NAME)
                .build();
        final DataProviderTypeRequestDto dataProviderTypeRequestDto = DataProviderTypeRequestDto.builder()
                .providerVersion(PROVIDER_VERSION)
                .providerTypeId(PROVIDER_TYPE_ID)
                .build();
        final MessageStatusTopicRequestDto messageStatusTopicRequestDto = MessageStatusTopicRequestDto.builder()
                .name(TOPIC_NAME)
                .messageBusId(BUS_ID)
                .specificationReference(SPECIFICATION_REFERENCE)
                .encoding(EncodingEnum.JSON)
                .build();
        final MessageDataTopicRequestDto messageDataTopicRequestDto = MessageDataTopicRequestDto.builder()
                .name(TOPIC_NAME)
                .messageBusId(BUS_ID)
                .encoding(EncodingEnum.JSON)
                .build();
        final InnerMessageSchemaRequestDto innerMessageSchemaRequestDto = InnerMessageSchemaRequestDto.builder()
                .specificationReference(SPECIFICATION_REFERENCE)
                .build();
        final DataServiceRequestDto dataServiceRequestDto = DataServiceRequestDto.builder()
                .dataServiceName(DATA_SERVICE_NAME)
                .build();
        final DataServiceInstanceRequestDto dataServiceInstanceRequestDto = DataServiceInstanceRequestDto.builder()
                .dataServiceInstanceName(DATA_SERVICE_INSTANCE_NAME)
                .controlEndPoint(UPDATED_CONTROL_ENDPOINT)
                .consumedDataSpace(DATA_SPACE_4G)
                .consumedDataCategory(DATA_SPACE_4G)
                .consumedDataProvider(DATA_SPACE_4G)
                .consumedSchemaName(SCHEMA_NAME)
                .consumedSchemaVersion(SCHEMA_VERSION)
                .build();
        final SupportedPredicateParameterRequestDto supportedPredicateParameterRequestDto = SupportedPredicateParameterRequestDto.builder()
                .parameterName(PARAMETER_NAME)
                .isPassedToConsumedService(Boolean.TRUE)
                .build();
        final DataTypeRequestDto dataTypeRequestDto = DataTypeRequestDto.builder()
                .mediumType(MEDIUM_TYPE)
                .schemaName(SCHEMA_NAME)
                .schemaVersion(SCHEMA_VERSION)
                .isExternal(Boolean.TRUE)
                .consumedDataSpace(DATA_SPACE_4G)
                .consumedDataCategory(DATA_SPACE_4G)
                .consumedDataProvider(DATA_SPACE_4G)
                .consumedSchemaName(DATA_SPACE_4G)
                .consumedSchemaVersion(SCHEMA_VERSION)
                .build();

        final MessageSchemaRequestDto messageSchemaRequestDto = MessageSchemaRequestDto.builder()
                .id(2)
                .dataSpace(dataSpaceRequestDto)
                .dataCategory(dataCategoryRequestDto)
                .dataProviderType(dataProviderTypeRequestDto)
                .messageStatusTopic(messageStatusTopicRequestDto)
                .messageDataTopic(messageDataTopicRequestDto)
                .messageSchema(innerMessageSchemaRequestDto)
                .dataService(dataServiceRequestDto)
                .dataServiceInstance(dataServiceInstanceRequestDto)
                .supportedPredicateParameter(supportedPredicateParameterRequestDto)
                .dataType(dataTypeRequestDto)
                .build();

        final MessageSchemaRequestDto deserializedObject = mapper.readValue(expectedString, MessageSchemaRequestDto.class);
        final String builtDtoString = mapper.writeValueAsString(messageSchemaRequestDto);

        assertEquals(messageSchemaRequestDto, deserializedObject);
        assertEquals(expectedString, builtDtoString);
    }

}
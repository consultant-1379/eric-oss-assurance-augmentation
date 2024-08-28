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

package com.ericsson.oss.air.aas.model.datacatalog.response;

import static com.ericsson.oss.air.aas.model.datacatalog.DataCatalogTestUtil.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.List;

import com.ericsson.oss.air.aas.model.datacatalog.EncodingEnum;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;

class MessageSchemaResponseDtoTest {

    @Test
    void build_ResponseForCreateMessageSchema() throws JsonProcessingException {

        // Example from API documentation for the response for creating a MessageSchema
        final String expectedStringWithWhitespace = "{\n" +
                "  \"id\": 1,\n" +
                "  \"dataService\": {\n" +
                "    \"id\": 3,\n" +
                "    \"dataServiceName\": \"dataservicename102\",\n" +
                "    \"dataServiceInstance\": [{\n" +
                "      \"id\": 2,\n" +
                "      \"dataServiceId\": 3,\n" +
                "      \"dataServiceInstanceName\": \"dsinst101\",\n" +
                "      \"controlEndpoint\": \"http://localhost:8082\",\n" +
                "      \"consumedDataSpace\": \"4G\",\n" +
                "      \"consumedDataCategory\": \"4G\",\n" +
                "      \"consumedDataProvider\": \"4G\",\n" +
                "      \"consumedSchemaName\": \"SCH2\",\n" +
                "      \"consumedSchemaVersion\": \"2\"\n" +
                "    }],\n" +
                "    \"predicateParameter\": [{\n" +
                "      \"id\": 2,\n" +
                "      \"parameterName\": \"pd101\",\n" +
                "      \"isPassedToConsumedService\": true,\n" +
                "      \"dataService\": {\n" +
                "        \"id\": 3,\n" +
                "        \"dataServiceName\": \"dataservicename102\"\n" +
                "      }\n" +
                "    }]\n" +
                "  },\n" +
                "  \"messageDataTopic\": {\n" +
                "    \"encoding\": \"JSON\",\n" +
                "    \"id\": 1,\n" +
                "    \"dataProviderType\": {\n" +
                "      \"dataSpace\": {\n" +
                "        \"dataProviderTypeIds\": [],\n" +
                "        \"id\": 5,\n" +
                "        \"name\": \"4G\"\n" +
                "      },\n" +
                "      \"dataCategoryType\": {\n" +
                "        \"id\": 4,\n" +
                "        \"dataCategoryName\": \"CM_EXPORTS1\"\n" +
                "      },\n" +
                "      \"id\": 4,\n" +
                "      \"providerTypeId\": \"vv101\",\n" +
                "      \"providerVersion\": \"Vv101\",\n" +
                "      \"notificationTopicIds\": [],\n" +
                "      \"messageDataTopicIds\": []\n" +
                "    },\n" +
                "    \"messageBus\": {\n" +
                "      \"accessEndpoints\": [],\n" +
                "      \"clusterName\": \"c1\",\n" +
                "      \"id\": 1,\n" +
                "      \"messageDataTopicIds\": [],\n" +
                "      \"messageStatusTopicIds\": [\n" +
                "        1\n" +
                "      ],\n" +
                "      \"name\": \"mb\",\n" +
                "      \"nameSpace\": \"2g\",\n" +
                "      \"notificationTopicIds\": [\n" +
                "        1\n" +
                "      ]\n" +
                "    },\n" +
                "    \"messageSchemaIds\": [],\n" +
                "    \"messageStatusTopic\": {\n" +
                "      \"encoding\": \"JSON\",\n" +
                "      \"id\": 1,\n" +
                "      \"messageBus\": {\n" +
                "        \"accessEndpoints\": [],\n" +
                "        \"clusterName\": \"c1\",\n" +
                "        \"id\": 1,\n" +
                "        \"messageDataTopicIds\": [],\n" +
                "        \"messageStatusTopicIds\": [\n" +
                "          1\n" +
                "        ],\n" +
                "        \"name\": \"mb\",\n" +
                "        \"nameSpace\": \"2g\",\n" +
                "        \"notificationTopicIds\": [\n" +
                "          1\n" +
                "        ]\n" +
                "      },\n" +
                "      \"messageDataTopicIds\": [],\n" +
                "      \"name\": \"topic102\",\n" +
                "      \"specificationReference\": \"SpecRef101\"\n" +
                "    },\n" +
                "    \"name\": \"topic102\"\n" +
                "  },\n" +
                "  \"dataType\": {\n" +
                "    \"id\": 2,\n" +
                "    \"mediumId\": 1,\n" +
                "    \"mediumType\": \"stream\",\n" +
                "    \"schemaName\": \"SCH2\",\n" +
                "    \"schemaVersion\": \"2\",\n" +
                "    \"isExternal\": false,\n" +
                "    \"consumedDataSpace\": \"4G\",\n" +
                "    \"consumedDataCategory\": \"4G\",\n" +
                "    \"consumedDataProvider\": \"4G\",\n" +
                "    \"consumedSchemaName\": \"4G\",\n" +
                "    \"consumedSchemaVersion\": \"2\"\n" +
                "\n" +
                "  },\n" +
                "  \"specificationReference\": \"SpecRef101\"\n" +
                "}";
        final String expectedString = expectedStringWithWhitespace.replaceAll("\\s", "");

        final MessageBusResponseDto messageBusResponseDto = MessageBusResponseDto.builder()
                .id(1)
                .name(MESSAGE_BUS_NAME)
                .clusterName(CLUSTER_NAME)
                .nameSpace(NAMESPACE_NAME)
                .accessEndpoints(Collections.emptyList())
                .notificationTopicId(BUS_ID)
                .messageStatusTopicId(BUS_ID)
                .messageDataTopicIds(Collections.emptyList())
                .build();
        final DataProviderTypeResponseDto dataProviderTypeResponseDto = DataProviderTypeResponseDto.builder()
                .id(4)
                .dataSpace(DataSpaceResponseDto.builder()
                        .id(5)
                        .name(DATA_SPACE_4G)
                        .dataProviderTypeIds(Collections.emptyList())
                        .build())
                .dataCategoryType(DataCategoryResponseDto.builder()
                        .id(4)
                        .dataCategoryName(DATA_CATEGORY_NAME)
                        .build())
                .notificationTopicIds(Collections.emptyList())
                .messageDataTopicIds(Collections.emptyList())
                .providerVersion(PROVIDER_VERSION)
                .providerTypeId(PROVIDER_TYPE_ID)
                .build();
        final MessageStatusTopicResponseDto messageStatusTopicResponseDto = MessageStatusTopicResponseDto.builder()
                .id(1)
                .name(TOPIC_NAME)
                .specificationReference(SPECIFICATION_REFERENCE)
                .encoding(EncodingEnum.JSON)
                .messageDataTopicIds(Collections.emptyList())
                .messageBus(messageBusResponseDto)
                .build();
        final MessageDataTopicResponseDto messageDataTopicResponseDto = MessageDataTopicResponseDto.builder()
                .id(1)
                .name(TOPIC_NAME)
                .encoding(EncodingEnum.JSON)
                .messageSchemaIds(Collections.emptyList())
                .messageBus(messageBusResponseDto)
                .dataProviderType(dataProviderTypeResponseDto)
                .messageStatusTopic(messageStatusTopicResponseDto)
                .build();
        final DataServiceInstanceResponseDto dataServiceInstanceResponseDto = DataServiceInstanceResponseDto.builder()
                .id(2)
                .dataServiceId(3)
                .dataServiceInstanceName(DATA_SERVICE_INSTANCE_NAME)
                .controlEndpoint(CONTROL_ENDPOINT)
                .consumedDataSpace(DATA_SPACE_4G)
                .consumedDataCategory(DATA_SPACE_4G)
                .consumedDataProvider(DATA_SPACE_4G)
                .consumedSchemaVersion(SCHEMA_VERSION)
                .consumedSchemaName(SCHEMA_NAME)
                .build();
        final SupportedPredicateParameterResponseDto supportedPredicateParameterResponseDto = SupportedPredicateParameterResponseDto.builder()
                .id(2)
                .parameterName(PARAMETER_NAME)
                .isPassedToConsumedService(Boolean.TRUE)
                .dataService(DataServiceResponseDto.builder()
                        .id(3)
                        .dataServiceName(DATA_SERVICE_NAME)
                        .build())
                .build();
        final DataServiceResponseDto dataServiceResponseDto = DataServiceResponseDto.builder()
                .id(3)
                .dataServiceName(DATA_SERVICE_NAME)
                .dataServiceInstance(List.of(dataServiceInstanceResponseDto))
                .predicateParameter(List.of(supportedPredicateParameterResponseDto))
                .build();
        final DataTypeResponseDto dataTypeResponseDto = DataTypeResponseDto.builder()
                .id(2)
                .mediumType(MEDIUM_TYPE)
                .mediumId(1)
                .schemaName(SCHEMA_NAME)
                .schemaVersion(SCHEMA_VERSION)
                .consumedDataSpace(DATA_SPACE_4G)
                .consumedDataCategory(DATA_SPACE_4G)
                .consumedDataProvider(DATA_SPACE_4G)
                .consumedSchemaName(DATA_SPACE_4G)
                .consumedSchemaVersion(SCHEMA_VERSION)
                .isExternal(Boolean.FALSE)
                .build();

        final MessageSchemaResponseDto messageSchemaResponseDto = MessageSchemaResponseDto.builder()
                .id(1)
                .dataService(dataServiceResponseDto)
                .messageDataTopic(messageDataTopicResponseDto)
                .dataType(dataTypeResponseDto)
                .specificationReference(SPECIFICATION_REFERENCE)
                .build();

        final MessageSchemaResponseDto deserializedObject = mapper.readValue(expectedString, MessageSchemaResponseDto.class);
        final String builtDtoString = mapper.writeValueAsString(messageSchemaResponseDto);

        assertEquals(messageSchemaResponseDto, deserializedObject);
        assertEquals(expectedString, builtDtoString);

    }


    @Test
    void build_ResponseForUpdateMessageSchema() throws JsonProcessingException {

        final String expectedStringWithWhitespace = "{\n" +
                "  \"id\": 1,\n" +
                "  \"dataService\": {\n" +
                "    \"id\": 3,\n" +
                "    \"dataServiceName\": \"dataservicename102\",\n" +
                "    \"dataServiceInstance\": [{\n" +
                "      \"id\": 2,\n" +
                "      \"dataServiceInstanceName\": \"dsinst101\",\n" +
                "      \"consumedDataSpace\": \"4G\",\n" +
                "      \"consumedDataCategory\": \"4G\",\n" +
                "      \"consumedDataProvider\": \"4G\",\n" +
                "      \"consumedSchemaName\": \"SCH2\",\n" +
                "      \"consumedSchemaVersion\": \"2\"\n" +
                "    }],\n" +
                "    \"predicateParameter\": [{\n" +
                "      \"id\": 2,\n" +
                "      \"parameterName\": \"pd101\",\n" +
                "      \"isPassedToConsumedService\": true,\n" +
                "      \"dataService\": {\n" +
                "        \"id\": 3,\n" +
                "        \"dataServiceName\": \"dataservicename102\"\n" +
                "      }\n" +
                "    }]\n" +
                "  },\n" +
                "  \"messageDataTopic\": {\n" +
                "    \"encoding\": \"JSON\",\n" +
                "    \"id\": 1,\n" +
                "    \"dataProviderType\": {\n" +
                "      \"dataSpace\": {\n" +
                "        \"dataProviderTypeIds\": [4],\n" +
                "        \"id\": 5,\n" +
                "        \"name\": \"4G\"\n" +
                "      },\n" +
                "      \"dataCategoryType\": {\n" +
                "        \"id\": 4,\n" +
                "        \"dataCategoryName\": \"CM_EXPORTS1\"\n" +
                "      },\n" +
                "      \"id\": 4,\n" +
                "      \"providerTypeId\": \"vv101\",\n" +
                "      \"providerVersion\": \"Vv101\",\n" +
                "      \"notificationTopicIds\": [],\n" +
                "      \"messageDataTopicIds\": [1]\n" +
                "    },\n" +
                "    \"messageBus\": {\n" +
                "      \"accessEndpoints\": [],\n" +
                "      \"clusterName\": \"c1\",\n" +
                "      \"id\": 1,\n" +
                "      \"messageDataTopicIds\": [1],\n" +
                "      \"messageStatusTopicIds\": [1],\n" +
                "      \"name\": \"mb\",\n" +
                "      \"nameSpace\": \"2g\",\n" +
                "      \"notificationTopicIds\": [1]\n" +
                "    },\n" +
                "    \"messageSchemaIds\": [1],\n" +
                "    \"messageStatusTopic\": {\n" +
                "      \"encoding\": \"JSON\",\n" +
                "      \"id\": 1,\n" +
                "      \"messageBus\": {\n" +
                "        \"accessEndpoints\": [],\n" +
                "        \"clusterName\": \"c1\",\n" +
                "        \"id\": 1,\n" +
                "        \"messageDataTopicIds\": [1],\n" +
                "        \"messageStatusTopicIds\": [1],\n" +
                "        \"name\": \"mb\",\n" +
                "        \"nameSpace\": \"2g\",\n" +
                "        \"notificationTopicIds\": [1]\n" +
                "      },\n" +
                "      \"messageDataTopicIds\": [1],\n" +
                "      \"name\": \"topic102\",\n" +
                "      \"specificationReference\": \"SpecRef101\"\n" +
                "    },\n" +
                "    \"name\": \"topic102\"\n" +
                "  },\n" +
                "  \"dataType\": {\n" +
                "    \"id\": 2,\n" +
                "    \"mediumId\": 1,\n" +
                "    \"mediumType\": \"stream\",\n" +
                "    \"schemaName\": \"SCH2\",\n" +
                "    \"schemaVersion\": \"2\",\n" +
                "    \"consumedDataSpace\": \"4G\",\n" +
                "    \"consumedDataCategory\": \"4G\",\n" +
                "    \"consumedDataProvider\": \"4G\",\n" +
                "    \"consumedSchemaName\": \"SCH2\",\n" +
                "    \"consumedSchemaVersion\": \"2\"\n" +
                "  },\n" +
                "  \"specificationReference\": \"SpecRef101\"\n" +
                "}";
        final String expectedString = expectedStringWithWhitespace.replaceAll("\\s", "");

        final MessageBusResponseDto messageBusResponseDto = MessageBusResponseDto.builder()
                .id(1)
                .name(MESSAGE_BUS_NAME)
                .clusterName(CLUSTER_NAME)
                .nameSpace(NAMESPACE_NAME)
                .accessEndpoints(Collections.emptyList())
                .notificationTopicId(BUS_ID)
                .messageStatusTopicId(BUS_ID)
                .messageDataTopicId(BUS_ID)
                .build();
        final DataProviderTypeResponseDto dataProviderTypeResponseDto = DataProviderTypeResponseDto.builder()
                .id(4)
                .dataSpace(DataSpaceResponseDto.builder()
                        .id(5)
                        .name(DATA_SPACE_4G)
                        .dataProviderTypeId(4)
                        .build())
                .dataCategoryType(DataCategoryResponseDto.builder()
                        .id(4)
                        .dataCategoryName(DATA_CATEGORY_NAME)
                        .build())
                .notificationTopicIds(Collections.emptyList())
                .messageDataTopicId(BUS_ID)
                .providerVersion(PROVIDER_VERSION)
                .providerTypeId(PROVIDER_TYPE_ID)
                .build();
        final MessageStatusTopicResponseDto messageStatusTopicResponseDto = MessageStatusTopicResponseDto.builder()
                .id(1)
                .name(TOPIC_NAME)
                .specificationReference(SPECIFICATION_REFERENCE)
                .encoding(EncodingEnum.JSON)
                .messageDataTopicId(BUS_ID)
                .messageBus(messageBusResponseDto)
                .build();
        final MessageDataTopicResponseDto messageDataTopicResponseDto = MessageDataTopicResponseDto.builder()
                .id(BUS_ID)
                .name(TOPIC_NAME)
                .encoding(EncodingEnum.JSON)
                .messageSchemaId(BUS_ID)
                .messageBus(messageBusResponseDto)
                .dataProviderType(dataProviderTypeResponseDto)
                .messageStatusTopic(messageStatusTopicResponseDto)
                .build();
        final DataServiceInstanceResponseDto dataServiceInstanceResponseDto = DataServiceInstanceResponseDto.builder()
                .id(2)
                .dataServiceInstanceName(DATA_SERVICE_INSTANCE_NAME)
                .consumedDataSpace(DATA_SPACE_4G)
                .consumedDataCategory(DATA_SPACE_4G)
                .consumedDataProvider(DATA_SPACE_4G)
                .consumedSchemaVersion(SCHEMA_VERSION)
                .consumedSchemaName(SCHEMA_NAME)
                .build();
        final SupportedPredicateParameterResponseDto supportedPredicateParameterResponseDto = SupportedPredicateParameterResponseDto.builder()
                .id(2)
                .parameterName(PARAMETER_NAME)
                .isPassedToConsumedService(Boolean.TRUE)
                .dataService(DataServiceResponseDto.builder()
                        .id(3)
                        .dataServiceName(DATA_SERVICE_NAME)
                        .build())
                .build();
        final DataServiceResponseDto dataServiceResponseDto = DataServiceResponseDto.builder()
                .id(3)
                .dataServiceName(DATA_SERVICE_NAME)
                .dataServiceInstance(List.of(dataServiceInstanceResponseDto))
                .predicateParameter(List.of(supportedPredicateParameterResponseDto))
                .build();
        final DataTypeResponseDto dataTypeResponseDto = DataTypeResponseDto.builder()
                .id(2)
                .mediumType(MEDIUM_TYPE)
                .mediumId(BUS_ID)
                .schemaName(SCHEMA_NAME)
                .schemaVersion(SCHEMA_VERSION)
                .consumedDataSpace(DATA_SPACE_4G)
                .consumedDataCategory(DATA_SPACE_4G)
                .consumedDataProvider(DATA_SPACE_4G)
                .consumedSchemaName(SCHEMA_NAME)
                .consumedSchemaVersion(SCHEMA_VERSION)
                .build();

        final MessageSchemaResponseDto messageSchemaResponseDto = MessageSchemaResponseDto.builder()
                .id(1)
                .dataService(dataServiceResponseDto)
                .messageDataTopic(messageDataTopicResponseDto)
                .dataType(dataTypeResponseDto)
                .specificationReference(SPECIFICATION_REFERENCE)
                .build();

        final MessageSchemaResponseDto deserializedObject = mapper.readValue(expectedString, MessageSchemaResponseDto.class);
        final String builtDtoString = mapper.writeValueAsString(messageSchemaResponseDto);

        assertEquals(messageSchemaResponseDto, deserializedObject);
        assertEquals(expectedString, builtDtoString);

    }

    @Test
    void build_IgnoreUnknownProperties() throws JsonProcessingException {
        assertNotNull(mapper.readValue("{\"id\":1,\"foobar\":\"something\"}", MessageSchemaResponseDto.class));
    }
}
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

package com.ericsson.oss.air.aas.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.util.UriComponentsBuilder;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
@AutoConfigureStubRunner(ids = "com.ericsson.oss.dmi:eric-oss-data-catalog:+:stubs:9590",
                         stubsMode = StubRunnerProperties.StubsMode.REMOTE,
                         repositoryRoot = "https://arm.seli.gic.ericsson.se/artifactory/proj-eric-oss-dev-local")
public class DataCatalogRestContractTest {

    static final String REGISTER_MESSAGE_SCHEMA_URI = "http://localhost:9590/catalog/v1/message-schema/";

    static final String INPUT_SCHEMA_METADATA_URI = "http://localhost:9590/catalog/v1/data-type";

    final static HttpHeaders headers = new HttpHeaders();

    @BeforeEach
    void initialisingHeader() {
        headers.setContentType(MediaType.APPLICATION_JSON);
    }

    @Test
    @Disabled
    void getAllDataType_withNonExistingQueryParameters() {
        String expectedResponse = "[]";

        final String endpointUrl = UriComponentsBuilder
                .fromUriString(INPUT_SCHEMA_METADATA_URI)
                .queryParam("dataSpace", "5G")
                .queryParam("dataCategory", "PM_COUNTERss")
                .queryParam("dataProvider", "v2")
                .queryParam("schemaName", "SCHMNM")
                .queryParam("schemaVersion", "2")
                .queryParam("serviceName", "dsName")
                .queryParam("isExternal", "true")
                .toUriString();

        ResponseEntity<String> response = new TestRestTemplate().exchange(
                endpointUrl,
                HttpMethod.GET,
                new HttpEntity<>(null, headers),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
        assertEquals(expectedResponse, response.getBody());
    }

    @Test
    @Disabled
    void dataTypeContractForGETRequest_invalidQueryParams() {
        String expectedResponse = "{\"timeStamp\":\"2022-12-01T12:02:38.9692185\",\"message\":\"Invalid Query Params-Allowed QueryParams(dataSpace,"
                + " dataCategory, dataProvider, schemaName, schemaVersion, serviceName, isExternal)\"}";

        final String endpointUrl = UriComponentsBuilder
                .fromUriString(INPUT_SCHEMA_METADATA_URI)
                .queryParam("data-type-dataSpace", "5G")
                .queryParam("dataCategory", "PM_COUNTERs")
                .queryParam("dataProviders", "v2")
                .queryParam("schemaName", "SCHMNM")
                .queryParam("schemaVersion", "2")
                .queryParam("serviceName", "ds")
                .toUriString();

        ResponseEntity<String> response = new TestRestTemplate().exchange(
                endpointUrl,
                HttpMethod.GET,
                new HttpEntity<>(null, headers),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
        assertEquals(expectedResponse, response.getBody());
    }

    @Test
    void getAllDataType_noParams() {
        String expectedResponse = "[{\"id\":1,\"mediumType\":\"file\",\"mediumId\":1,\"schemaName\":\"RANBDR\",\"schemaVersion\":\"1.0.0\","
                + "\"isExternal\":false,\"fileFormat\":{\"id\":1,\"dataService\":{\"id\":1,\"dataServiceName\":\"ran-pm-counter-sftp-file-transfer\"},"
                + "\"dataType\":{\"id\":1,\"mediumType\":\"file\",\"mediumId\":1,\"schemaName\":\"RANBDR\",\"schemaVersion\":\"1.0.0\","
                + "\"isExternal\":false},\"bulkDataRepository\":{\"id\":1,\"name\":\"2g\",\"clusterName\":\"c1\",\"nameSpace\":\"na\","
                + "\"accessEndpoints\":[],\"fileFormatIds\":[1,2],\"fileRepoType\":\"SFTP\"},\"reportOutputPeriodList\":[0],"
                + "\"notificationTopic\":{\"id\":1,\"name\":\"ran-pm-counter-sftp-file-transfer\",\"specificationReference\":\"\",\"encoding\":\"JSON\","
                + "\"dataProviderType\":{\"id\":1,\"dataSpace\":{\"id\":1,\"name\":\"\",\"dataProviderTypeIds\":[1]},\"dataCategoryType\":{\"id\":1,"
                + "\"dataCategoryName\":\"PM_COUNTERS\"},\"dataProviderName\":\"enmFileNotificationService\",\"notificationTopicIds\":[1],"
                + "\"messageDataTopicIds\":[]},\"fileFormatIds\":[1],\"messageBus\":{\"id\":1,\"name\":\"1\",\"clusterName\":\"c1\",\"nameSpace\":\"na\","
                + "\"accessEndpoints\":[],\"notificationTopicIds\":[1,2],\"messageStatusTopicIds\":[],\"messageDataTopicIds\":[]}},"
                + "\"specificationReference\":\"\",\"dataEncoding\":\"XML\"}},{\"id\":2,\"mediumType\":\"file\",\"mediumId\":2,\"schemaName\":\"SCHMNM\","
                + "\"schemaVersion\":\"2\",\"isExternal\":true,\"consumedDataSpace\":\"5G\",\"consumedDataCategory\":\"5G\","
                + "\"consumedDataProvider\":\"5G\",\"consumedSchemaName\":\"5G\",\"consumedSchemaVersion\":\"2\",\"fileFormat\":{\"id\":2,"
                + "\"dataService\":{\"id\":2,\"dataServiceName\":\"dsName\"},\"dataType\":{\"id\":2,\"mediumType\":\"file\",\"mediumId\":2,"
                + "\"schemaName\":\"SCHMNM\",\"schemaVersion\":\"2\",\"isExternal\":true,\"consumedDataSpace\":\"5G\",\"consumedDataCategory\":\"5G\","
                + "\"consumedDataProvider\":\"5G\",\"consumedSchemaName\":\"5G\",\"consumedSchemaVersion\":\"2\"},\"bulkDataRepository\":{\"id\":1,"
                + "\"name\":\"2g\",\"clusterName\":\"c1\",\"nameSpace\":\"na\",\"accessEndpoints\":[],\"fileFormatIds\":[1,2],\"fileRepoType\":\"SFTP\"},"
                + "\"reportOutputPeriodList\":[0],\"notificationTopic\":{\"id\":2,\"name\":\"name\",\"specificationReference\":\"specRef\","
                + "\"encoding\":\"JSON\",\"dataProviderType\":{\"id\":2,\"dataSpace\":{\"id\":2,\"name\":\"5G\",\"dataProviderTypeIds\":[2]},"
                + "\"dataCategoryType\":{\"id\":2,\"dataCategoryName\":\"PM_COUNTERs\"},\"dataProviderName\":\"v2\",\"notificationTopicIds\":[2],"
                + "\"messageDataTopicIds\":[]},\"fileFormatIds\":[2],\"messageBus\":{\"id\":1,\"name\":\"1\",\"clusterName\":\"c1\",\"nameSpace\":\"na\","
                + "\"accessEndpoints\":[],\"notificationTopicIds\":[1,2],\"messageStatusTopicIds\":[],\"messageDataTopicIds\":[]}},"
                + "\"specificationReference\":\"specRef\",\"dataEncoding\":\"JSON\"}}]";

        final String endpointUrl = UriComponentsBuilder
                .fromUriString("http://localhost:9590/catalog/v1/data-type/")
                .toUriString();

        ResponseEntity<String> response = new TestRestTemplate().exchange(
                endpointUrl,
                HttpMethod.GET,
                new HttpEntity<>(null, headers),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
        assertEquals(expectedResponse, response.getBody());
    }

    @Test
    @Disabled
    void getAllDataType_byQueryParameters() {
        String expectedResponse = "[{\"id\":1,\"mediumType\":\"file\",\"mediumId\":2,\"schemaName\":\"SCHMNM\",\"schemaVersion\":\"2\","
                + "\"isExternal\":true,\"consumedDataSpace\":\"5G\",\"consumedDataCategory\":\"5G\",\"consumedDataProvider\":\"5G\","
                + "\"consumedSchemaName\":\"5G\",\"consumedSchemaVersion\":\"2\",\"fileFormat\":{\"id\":2,\"dataService\":{\"id\":1,"
                + "\"dataServiceName\":\"dsName\"},\"dataType\":{\"id\":2,\"mediumType\":\"file\",\"mediumId\":3,\"schemaName\":\"SCHMNM\","
                + "\"schemaVersion\":\"2\",\"isExternal\":true,\"consumedDataSpace\":\"5G\",\"consumedDataCategory\":\"5G\","
                + "\"consumedDataProvider\":\"5G\",\"consumedSchemaName\":\"5G\",\"consumedSchemaVersion\":\"2\"},\"bulkDataRepository\":{\"id\":1,"
                + "\"name\":\"testBDR\",\"clusterName\":\"testCluster\",\"nameSpace\":\"testNS\",\"accessEndpoints\":[\"http://endpoint1:1234/\"],"
                + "\"fileFormatIds\":[1,2,3],\"fileRepoType\":\"S3\"},\"reportOutputPeriodList\":[0],\"notificationTopic\":{\"id\":2,"
                + "\"name\":\"name\",\"specificationReference\":\"specRef\",\"encoding\":\"JSON\",\"dataProviderType\":{\"id\":6,"
                + "\"dataSpace\":{\"id\":2,\"name\":\"5G\",\"dataProviderTypeIds\":[6]},\"dataCategoryType\":{\"id\":1,"
                + "\"dataCategoryName\":\"PM_COUNTERs\"},\"notificationTopicIds\":[2],\"messageDataTopicIds\":[],\"providerVersion\":\"V2\","
                + "\"providerTypeId\":\"v2\"},\"fileFormatIds\":[2,3],\"messageBus\":{\"id\":1,\"name\":\"name\",\"clusterName\":\"clusterName\","
                + "\"nameSpace\":\"nameSpace\",\"accessEndpoints\":[\"http://endpoint1:1234/\"],\"notificationTopicIds\":[1,2],"
                + "\"messageStatusTopicIds\":[1,2],\"messageDataTopicIds\":[1,2]}},\"specificationReference\":\"specRef\",\"dataEncoding\":\"JSON\"}}]";

        final String endpointUrl = UriComponentsBuilder
                .fromUriString(INPUT_SCHEMA_METADATA_URI)
                .queryParam("dataSpace", "5G")
                .queryParam("dataCategory", "PM_COUNTERs")
                .queryParam("dataProvider", "v2")
                .queryParam("schemaName", "SCHMNM")
                .queryParam("schemaVersion", "2")
                .queryParam("serviceName", "dsName")
                .queryParam("isExternal", "true")
                .toUriString();

        ResponseEntity<String> response = new TestRestTemplate().exchange(
                endpointUrl,
                HttpMethod.GET,
                new HttpEntity<>(null, headers),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
        assertEquals(expectedResponse, response.getBody());
    }

    @Disabled
    @Test
    void register_CreateContract_Created() {
        String request = "{\"dataSpace\": {\"name\": \"4G\"}, \"dataService\": {\"dataServiceName\": \"ds\"}, "
                + "\"dataCategory\": {\"dataCategoryName\": \"CM_EXPORTS1\"}, \"dataProviderType\": "
                + "{\"providerVersion\": \"Vv101\", \"providerTypeId\": \"vv101\"}, \"messageStatusTopic\": {"
                + "\"name\": \"topic102\", \"messageBusId\": 1, \"specificationReference\": \"SpecRef101\", "
                + "\"encoding\": \"JSON\"}, \"messageDataTopic\": {\"name\": \"topic102\", \"messageBusId\": 1, "
                + "\"encoding\": \"JSON\"}, \"dataServiceInstance\": {\"dataServiceInstanceName\": \"dsinst101\", "
                + "\"controlEndPoint\": \"http://localhost:8082\", \"consumedDataSpace\": \"4G\", "
                + "\"consumedDataCategory\": \"4G\", \"consumedDataProvider\": \"4G\", \"consumedSchemaName\": \"SCH2\", "
                + "\"consumedSchemaVersion\": \"2\"}, \"dataType\": {\"mediumType\": \"stream\", \"schemaName\": \"SCH2\", "
                + "\"schemaVersion\": \"2\", \"isExternal\": true, \"consumedDataSpace\": \"4G\", "
                + "\"consumedDataCategory\": \"4G\", \"consumedDataProvider\": \"4G\", \"consumedSchemaName\": \"4G\", "
                + "\"consumedSchemaVersion\": \"2\"}, \"supportedPredicateParameter\": {\"parameterName\": \"pd101\", "
                + "\"isPassedToConsumedService\": true}, \"messageSchema\": {\"specificationReference\": \"SpecRef101\"}}";

        String expectedResponse = "{\"id\":3,\"messageDataTopic\":{\"id\":1,\"name\":\"topic102\",\"encoding\":\"JSON\",\"messageSchemaIds\":[1],"
                + "\"messageBus\":{\"id\":1,\"name\":\"1\",\"clusterName\":\"c1\",\"nameSpace\":\"na\",\"accessEndpoints\":[],"
                + "\"notificationTopicIds\":[],\"messageStatusTopicIds\":[1],\"messageDataTopicIds\":[1]},\"dataProviderType\":{\"id\":1,"
                + "\"dataSpace\":{\"id\":1,\"name\":\"4G\",\"dataProviderTypeIds\":[1]},\"dataCategoryType\":{\"id\":1,"
                + "\"dataCategoryName\":\"CM_EXPORTS1\"},\"dataProviderName\":null,\"notificationTopicIds\":[],\"messageDataTopicIds\":[1],"
                + "\"providerVersion\":\"Vv101\",\"providerTypeId\":\"vv101\"},\"messageStatusTopic\":{\"id\":1,\"name\":\"topic102\","
                + "\"specificationReference\":\"SpecRef101\",\"encoding\":\"JSON\",\"messageDataTopicIds\":[1],\"messageBus\":{\"id\":1,"
                + "\"name\":\"1\",\"clusterName\":\"c1\",\"nameSpace\":\"na\",\"accessEndpoints\":[],\"notificationTopicIds\":[],"
                + "\"messageStatusTopicIds\":[1],\"messageDataTopicIds\":[1]}}},\"dataService\":{\"id\":2,\"dataServiceInstance\":[{\"id\":2,"
                + "\"dataServiceInstanceName\":\"dsinst101\",\"controlEndPoint\":\"http://localhost:8082\",\"consumedDataSpace\":\"4G\","
                + "\"consumedDataCategory\":\"4G\",\"consumedDataProvider\":\"4G\",\"consumedSchemaVersion\":\"2\",\"consumedSchemaName\":\"SCH2\"}],"
                + "\"predicateParameter\":[{\"id\":2,\"parameterName\":\"pd101\",\"isPassedToConsumedService\":true}],\"dataServiceName\":\"ds\"},"
                + "\"dataType\":{\"id\":2,\"mediumType\":\"stream\",\"mediumId\":3,\"schemaName\":\"SCH2\",\"schemaVersion\":\"2\","
                + "\"isExternal\":true,\"consumedDataSpace\":\"4G\",\"consumedDataCategory\":\"4G\",\"consumedDataProvider\":\"4G\","
                + "\"consumedSchemaName\":\"4G\",\"consumedSchemaVersion\":\"2\"},\"specificationReference\":\"SpecRef101\"}";

        ResponseEntity<String> response = new TestRestTemplate().exchange(
                REGISTER_MESSAGE_SCHEMA_URI,
                HttpMethod.PUT,
                new HttpEntity<>(request, headers),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
        assertEquals(expectedResponse, response.getBody());
    }

    @Test
    void register_CreateContract_WithDataSpaceEmpty_Created() {
        String request = "{\"dataSpace\": {\"name\": \"\"}, \"dataService\": {\"dataServiceName\": \"dataservicename104\"}, "
                + "\"dataCategory\": {\"dataCategoryName\": \"CM_EXPORTS1\"}, \"dataProviderType\": "
                + "{\"providerVersion\": \"Vv101\", \"providerTypeId\": \"vv101\"}, \"messageStatusTopic\": {"
                + "\"name\": \"topic102\", \"messageBusId\": 1, \"specificationReference\": \"SpecRef101\", "
                + "\"encoding\": \"JSON\"}, \"messageDataTopic\": {\"name\": \"topic102\", \"messageBusId\": 1, "
                + "\"encoding\": \"JSON\"}, \"dataServiceInstance\": {\"dataServiceInstanceName\": \"dsinst101\", "
                + "\"controlEndPoint\": \"http://localhost:8082\", \"consumedDataSpace\": \"4G\", "
                + "\"consumedDataCategory\": \"4G\", \"consumedDataProvider\": \"4G\", \"consumedSchemaName\": \"SCH2\", "
                + "\"consumedSchemaVersion\": \"2\"}, \"dataType\": {\"mediumType\": \"stream\", \"schemaName\": \"SCH2\", "
                + "\"schemaVersion\": \"2\", \"isExternal\": true, \"consumedDataSpace\": \"4G\", "
                + "\"consumedDataCategory\": \"4G\", \"consumedDataProvider\": \"4G\", \"consumedSchemaName\": \"4G\", "
                + "\"consumedSchemaVersion\": \"2\"}, \"supportedPredicateParameter\": {\"parameterName\": \"pd101\", "
                + "\"isPassedToConsumedService\": true}, \"messageSchema\": {\"specificationReference\": \"SpecRef101\"}}";

        String expectedResponse = "{\"id\":4,\"messageDataTopic\":{\"id\":1,\"name\":\"topic102\",\"encoding\":\"JSON\",\"messageSchemaIds\":[1,3],"
                + "\"messageBus\":{\"id\":1,\"name\":\"1\",\"clusterName\":\"c1\",\"nameSpace\":\"na\",\"accessEndpoints\":[],"
                + "\"notificationTopicIds\":[],\"messageStatusTopicIds\":[1],\"messageDataTopicIds\":[1]},\"dataProviderType\":{\"id\":2,"
                + "\"dataSpace\":{\"id\":2,\"name\":\"\",\"dataProviderTypeIds\":[]},\"dataCategoryType\":{\"id\":1,"
                + "\"dataCategoryName\":\"CM_EXPORTS1\"},\"dataProviderName\":null,\"notificationTopicIds\":[],\"messageDataTopicIds\":[],"
                + "\"providerVersion\":\"Vv101\",\"providerTypeId\":\"vv101\"},\"messageStatusTopic\":{\"id\":1,\"name\":\"topic102\","
                + "\"specificationReference\":\"SpecRef101\",\"encoding\":\"JSON\",\"messageDataTopicIds\":[1],\"messageBus\":{\"id\":1,"
                + "\"name\":\"1\",\"clusterName\":\"c1\",\"nameSpace\":\"na\",\"accessEndpoints\":[],\"notificationTopicIds\":[],"
                + "\"messageStatusTopicIds\":[1],\"messageDataTopicIds\":[1]}}},\"dataService\":{\"id\":4,\"dataServiceInstance\":[{\"id\":4,"
                + "\"dataServiceInstanceName\":\"dsinst101\",\"controlEndPoint\":\"http://localhost:8082\",\"consumedDataSpace\":\"4G\","
                + "\"consumedDataCategory\":\"4G\",\"consumedDataProvider\":\"4G\",\"consumedSchemaVersion\":\"2\",\"consumedSchemaName\":\"SCH2\"}],"
                + "\"predicateParameter\":[{\"id\":4,\"parameterName\":\"pd101\",\"isPassedToConsumedService\":true}],\"dataServiceName\":\"dataservicename104\"},"
                + "\"dataType\":{\"id\":3,\"mediumType\":\"stream\",\"mediumId\":4,\"schemaName\":\"SCH2\",\"schemaVersion\":\"2\","
                + "\"isExternal\":true,\"consumedDataSpace\":\"4G\",\"consumedDataCategory\":\"4G\",\"consumedDataProvider\":\"4G\","
                + "\"consumedSchemaName\":\"4G\",\"consumedSchemaVersion\":\"2\"},\"specificationReference\":\"SpecRef101\"}";

        ResponseEntity<String> response = new TestRestTemplate().exchange(
                "http://localhost:9590/catalog/v1/message-schema",
                HttpMethod.PUT,
                new HttpEntity<>(request, headers),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
        assertEquals(expectedResponse, response.getBody());

    }

    @Test
    void register_UpdatedContract_Ok() {
        String request = "{\"id\":1, \"dataSpace\": {\"name\": \"4G\"}, \"dataService\": {\"dataServiceName\": \"dataservicename103\"}, "
                + "\"dataCategory\": {\"dataCategoryName\": \"CM_EXPORTS1\"}, \"dataProviderType\": "
                + "{\"providerVersion\": \"Vv101\", \"providerTypeId\": \"vv101\"}, \"messageStatusTopic\": {"
                + "\"name\": \"topic102\", \"messageBusId\": 1, \"specificationReference\": \"SpecRef101\", "
                + "\"encoding\": \"JSON\"}, \"messageDataTopic\": {\"name\": \"topic102\", \"messageBusId\": 1, "
                + "\"encoding\": \"JSON\"}, \"dataServiceInstance\": {\"dataServiceInstanceName\": \"dsinst101\", "
                + "\"controlEndPoint\": \"http://localhost:8082\", \"consumedDataSpace\": \"4G\", "
                + "\"consumedDataCategory\": \"4G\", \"consumedDataProvider\": \"4G\", \"consumedSchemaName\": \"SCH2\", "
                + "\"consumedSchemaVersion\": \"2\"}, \"dataType\": {\"mediumType\": \"stream\", \"schemaName\": \"SCH2\", "
                + "\"schemaVersion\": \"2\", \"isExternal\": true, \"consumedDataSpace\": \"4G\", "
                + "\"consumedDataCategory\": \"4G\", \"consumedDataProvider\": \"4G\", \"consumedSchemaName\": \"4G\", "
                + "\"consumedSchemaVersion\": \"2\"}, \"supportedPredicateParameter\": {\"parameterName\": \"pd101\", "
                + "\"isPassedToConsumedService\": true}, \"messageSchema\": {\"specificationReference\": \"SpecRef101\"}}";

        String expectedResponse = "{\"id\":1,\"messageDataTopic\":{\"id\":1,\"name\":\"topic102\",\"encoding\":\"JSON\",\"messageSchemaIds\":[1,3],"
                + "\"messageBus\":{\"id\":1,\"name\":\"1\",\"clusterName\":\"c1\",\"nameSpace\":\"na\",\"accessEndpoints\":[],"
                + "\"notificationTopicIds\":[],\"messageStatusTopicIds\":[1],\"messageDataTopicIds\":[1]},\"dataProviderType\":{\"id\":1,"
                + "\"dataSpace\":{\"id\":1,\"name\":\"4G\",\"dataProviderTypeIds\":[1]},\"dataCategoryType\":{\"id\":1,"
                + "\"dataCategoryName\":\"CM_EXPORTS1\"},\"dataProviderName\":null,\"notificationTopicIds\":[],\"messageDataTopicIds\":[1],"
                + "\"providerVersion\":\"Vv101\",\"providerTypeId\":\"vv101\"},\"messageStatusTopic\":{\"id\":1,\"name\":\"topic102\","
                + "\"specificationReference\":\"SpecRef101\",\"encoding\":\"JSON\",\"messageDataTopicIds\":[1],\"messageBus\":{\"id\":1,"
                + "\"name\":\"1\",\"clusterName\":\"c1\",\"nameSpace\":\"na\",\"accessEndpoints\":[],\"notificationTopicIds\":[],"
                + "\"messageStatusTopicIds\":[1],\"messageDataTopicIds\":[1]}}},\"dataService\":{\"id\":3,\"dataServiceInstance\":[{\"id\":3,"
                + "\"dataServiceInstanceName\":\"dsinst101\",\"controlEndPoint\":\"http://localhost:8082\",\"consumedDataSpace\":\"4G\","
                + "\"consumedDataCategory\":\"4G\",\"consumedDataProvider\":\"4G\",\"consumedSchemaVersion\":\"2\",\"consumedSchemaName\":\"SCH2\"}],"
                + "\"predicateParameter\":[{\"id\":3,\"parameterName\":\"pd101\",\"isPassedToConsumedService\":true}],"
                + "\"dataServiceName\":\"dataservicename103\"},\"dataType\":{\"id\":1,\"mediumType\":\"stream\",\"mediumId\":1,"
                + "\"schemaName\":\"SCH2\",\"schemaVersion\":\"2\",\"isExternal\":true,\"consumedDataSpace\":\"4G\",\"consumedDataCategory\":\"4G\","
                + "\"consumedDataProvider\":\"4G\",\"consumedSchemaName\":\"4G\",\"consumedSchemaVersion\":\"2\"},\"specificationReference\":\"SpecRef101\"}";

        ResponseEntity<String> response = new TestRestTemplate().exchange(
                "http://localhost:9590/catalog/v1/message-schema",
                HttpMethod.PUT,
                new HttpEntity<>(request, headers),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
        assertEquals(expectedResponse, response.getBody());
    }

    @Disabled
    @Test
    void register_existingValues_conflict() {
        String request = "{\"dataSpace\": {\"name\": \"4G\"}, \"dataService\": {\"dataServiceName\": \"dataservicename102\"}, "
                + "\"dataCategory\": {\"dataCategoryName\": \"CM_EXPORTS1\"}, \"dataProviderType\": {"
                + "\"providerVersion\": \"Vv101\", \"providerTypeId\": \"vv101\"}, \"messageStatusTopic\": {"
                + "\"name\": \"topic102\", \"messageBusId\": 1, \"specificationReference\": \"SpecRef101\", "
                + "\"encoding\": \"JSON\"}, \"messageDataTopic\": {\"name\": \"topic102\", \"messageBusId\": 1, "
                + "\"encoding\": \"JSON\"}, \"dataServiceInstance\": {\"dataServiceInstanceName\": \"dsinst101\", "
                + "\"controlEndPoint\": \"http://localhost:8082\", \"consumedDataSpace\": \"4G\", "
                + "\"consumedDataCategory\": \"4G\", \"consumedDataProvider\": \"4G\", \"consumedSchemaName\": \"SCH2\", "
                + "\"consumedSchemaVersion\": \"2\"}, \"dataType\": {\"mediumType\": \"stream\", \"schemaName\": \"SCH2\", "
                + "\"schemaVersion\": \"2\", \"isExternal\": true, \"consumedDataSpace\": \"4G\", "
                + "\"consumedDataCategory\": \"4G\", \"consumedDataProvider\": \"4G\", \"consumedSchemaName\": \"4G\", "
                + "\"consumedSchemaVersion\": \"2\"}, \"supportedPredicateParameter\": {\"parameterName\": \"pd101\", "
                + "\"isPassedToConsumedService\": true}, \"messageSchema\": {\"specificationReference\": \"SpecRef101\"}}";

        ResponseEntity<String> response = new TestRestTemplate().exchange(
                REGISTER_MESSAGE_SCHEMA_URI,
                HttpMethod.PUT,
                new HttpEntity<>(request, headers),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void register_dataTypeProviderFieldMissing_badRequest() {
        String request = "{\"dataSpace\": {\"name\": \"4G\"}, \"dataService\": {\"dataServiceName\": \"dataservicename102\"}, "
                + "\"dataCategory\": {\"dataCategoryName\": \"CM_EXPORTS1\"}, \"dataProviderType\": {\"providerVersion\": "
                + "\"Vv101\"}, \"messageStatusTopic\": {\"name\": \"topic102\", \"messageBusId\": 1, "
                + "\"specificationReference\": \"SpecRef101\", \"encoding\": \"JSON\"}, \"messageDataTopic\": {"
                + "\"name\": \"topic102\", \"messageBusId\": 1, \"encoding\": \"JSON\"}, \"dataServiceInstance\": {"
                + "\"dataServiceInstanceName\": \"dsinst101\", \"controlEndPoint\": \"http://localhost:8082\", "
                + "\"consumedDataSpace\": \"4G\", \"consumedDataCategory\": \"4G\", \"consumedDataProvider\": \"4G\", "
                + "\"consumedSchemaName\": \"SCH2\", \"consumedSchemaVersion\": \"2\"}, \"dataType\": {"
                + "\"mediumType\": \"stream\", \"schemaName\": \"SCH2\", \"schemaVersion\": \"2\", \"isExternal\": true, "
                + "\"consumedDataSpace\": \"4G\", \"consumedDataCategory\": \"4G\", \"consumedDataProvider\": \"4G\", "
                + "\"consumedSchemaName\": \"4G\", \"consumedSchemaVersion\": \"2\"}, \"supportedPredicateParameter\": {"
                + "\"parameterName\": \"pd101\", \"isPassedToConsumedService\": true}, \"messageSchema\": {"
                + "\"specificationReference\": \"SpecRef101\"}}";

        ResponseEntity<String> response = new TestRestTemplate().exchange(
                REGISTER_MESSAGE_SCHEMA_URI,
                HttpMethod.PUT,
                new HttpEntity<>(request, headers),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void register_dataServiceMissing_badRequest() {
        String request = "{\"dataSpace\": {\"name\": \"4G\"}, \"dataCategory\": {\"dataCategoryName\": \"CM_EXPORTS1\"}, "
                + "\"dataProviderType\": {\"providerVersion\": \"Vv101\", \"providerTypeId\": \"vv101\"}, "
                + "\"messageStatusTopic\": {\"name\": \"topic102\", \"messageBusId\": 1, \"specificationReference\": "
                + "\"SpecRef101\", \"encoding\": \"JSON\"}, \"messageDataTopic\": {\"name\": \"topic102\", "
                + "\"messageBusId\": 1, \"encoding\": \"JSON\"}, \"dataServiceInstance\": {\"dataServiceInstanceName\": "
                + "\"dsinst101\", \"controlEndPoint\": \"http://localhost:8082\", \"consumedDataSpace\": \"4G\", "
                + "\"consumedDataCategory\": \"4G\", \"consumedDataProvider\": \"4G\", \"consumedSchemaName\": \"SCH2\", "
                + "\"consumedSchemaVersion\": \"2\"}, \"dataType\": {\"mediumType\": \"stream\", \"schemaName\": \"SCH2\", "
                + "\"schemaVersion\": \"2\", \"isExternal\": true, \"consumedDataSpace\": \"4G\", \"consumedDataCategory\": "
                + "\"4G\", \"consumedDataProvider\": \"4G\", \"consumedSchemaName\": \"4G\", \"consumedSchemaVersion\": "
                + "\"2\"}, \"supportedPredicateParameter\": {\"parameterName\": \"pd101\", \"isPassedToConsumedService\": "
                + "true}, \"messageSchema\": {\"specificationReference\": \"SpecRef101\"}}";

        ResponseEntity<String> response = new TestRestTemplate().exchange(
                REGISTER_MESSAGE_SCHEMA_URI,
                HttpMethod.PUT,
                new HttpEntity<>(request, headers),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void register_dataSpaceNameMissing_badRequest() {
        String request = "{\"dataSpace\": {},\"dataService\": {\"dataServiceName\": \"dataservicename102\"}, "
                + "\"dataCategory\": {\"dataCategoryName\": \"CM_EXPORTS1\"}, \"dataProviderType\": {"
                + "\"providerVersion\": \"Vv101\", \"providerTypeId\": \"vv101\"}, \"messageStatusTopic\": {"
                + "\"name\": \"topic102\", \"messageBusId\": 1, \"specificationReference\": \"SpecRef101\", "
                + "\"encoding\": \"JSON\"}, \"messageDataTopic\": {\"name\": \"topic102\", \"messageBusId\": 1, "
                + "\"encoding\": \"JSON\"}, \"dataServiceInstance\": {\"dataServiceInstanceName\": \"dsinst101\", "
                + "\"controlEndPoint\": \"http://localhost:8082\", \"consumedDataSpace\": \"4G\", "
                + "\"consumedDataCategory\": \"4G\", \"consumedDataProvider\": \"4G\", \"consumedSchemaName\": \"SCH2\", "
                + "\"consumedSchemaVersion\": \"2\"}, \"dataType\": {\"mediumType\": \"stream\", \"schemaName\": \"SCH2\", "
                + "\"schemaVersion\": \"2\", \"isExternal\": true, \"consumedDataSpace\": \"4G\", \"consumedDataCategory\": "
                + "\"4G\", \"consumedDataProvider\": \"4G\", \"consumedSchemaName\": \"4G\", "
                + "\"consumedSchemaVersion\": \"2\"}, \"supportedPredicateParameter\": {\"parameterName\": \"pd101\", "
                + "\"isPassedToConsumedService\": true}, \"messageSchema\": {\"specificationReference\": \"SpecRef101\"}}";

        ResponseEntity<String> response = new TestRestTemplate().exchange(
                REGISTER_MESSAGE_SCHEMA_URI,
                HttpMethod.PUT,
                new HttpEntity<>(request, headers),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void register_messageSchemaMissing_badRequest() {
        String request = "{\"dataSpace\": {\"name\": \"4G\"}, \"dataService\": {\"dataServiceName\": \"dataservicename102\"}, "
                + "\"dataCategory\": {\"dataCategoryName\": \"CM_EXPORTS1\"}, \"dataProviderType\": {"
                + "\"providerVersion\": \"Vv101\", \"providerTypeId\": \"vv101\"}, \"messageStatusTopic\": {"
                + "\"name\": \"topic102\", \"messageBusId\": 1, \"specificationReference\": \"SpecRef101\", "
                + "\"encoding\": \"JSON\"}, \"messageDataTopic\": {\"name\": \"topic102\", \"messageBusId\": 1, "
                + "\"encoding\": \"JSON\"}, \"dataServiceInstance\": {\"dataServiceInstanceName\": \"dsinst101\", "
                + "\"controlEndPoint\": \"http://localhost:8082\", \"consumedDataSpace\": \"4G\", \"consumedDataCategory\": "
                + "\"4G\", \"consumedDataProvider\": \"4G\", \"consumedSchemaName\": \"SCH2\", \"consumedSchemaVersion\": "
                + "\"2\"}, \"dataType\": {\"mediumType\": \"stream\", \"schemaName\": \"SCH2\", \"schemaVersion\": \"2\", "
                + "\"isExternal\": true, \"consumedDataSpace\": \"4G\", \"consumedDataCategory\": \"4G\", "
                + "\"consumedDataProvider\": \"4G\", \"consumedSchemaName\": \"4G\", \"consumedSchemaVersion\": \"2\"}, "
                + "\"supportedPredicateParameter\": {\"parameterName\": \"pd101\", \"isPassedToConsumedService\": true}}";

        ResponseEntity<String> response = new TestRestTemplate().exchange(
                REGISTER_MESSAGE_SCHEMA_URI,
                HttpMethod.PUT,
                new HttpEntity<>(request, headers),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void register_messageStatusTopicMissing_badRequest() {
        String request = "{\"dataSpace\": {\"name\": \"4G\"}, \"dataService\": {\"dataServiceName\": \"dataservicename102\"}, "
                + "\"dataCategory\": {\"dataCategoryName\": \"CM_EXPORTS1\"}, \"dataProviderType\": {\"providerVersion\": "
                + "\"Vv101\", \"providerTypeId\": \"vv101\"}, \"messageDataTopic\": {\"name\": \"topic102\", "
                + "\"messageBusId\": 1, \"encoding\": \"JSON\"}, \"dataServiceInstance\": {\"dataServiceInstanceName\": "
                + "\"dsinst101\", \"controlEndPoint\": \"http://localhost:8082\", \"consumedDataSpace\": \"4G\", "
                + "\"consumedDataCategory\": \"4G\", \"consumedDataProvider\": \"4G\", \"consumedSchemaName\": \"SCH2\", "
                + "\"consumedSchemaVersion\": \"2\"}, \"dataType\": {\"mediumType\": \"stream\", \"schemaName\": \"SCH2\", "
                + "\"schemaVersion\": \"2\", \"isExternal\": true, \"consumedDataSpace\": \"4G\", \"consumedDataCategory\": "
                + "\"4G\", \"consumedDataProvider\": \"4G\", \"consumedSchemaName\": \"4G\", \"consumedSchemaVersion\": "
                + "\"2\"}, \"supportedPredicateParameter\": {\"parameterName\": \"pd101\", \"isPassedToConsumedService\": "
                + "true}, \"messageSchema\": {\"specificationReference\": \"SpecRef101\"}}";

        ResponseEntity<String> response = new TestRestTemplate().exchange(
                REGISTER_MESSAGE_SCHEMA_URI,
                HttpMethod.PUT,
                new HttpEntity<>(request, headers),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void register_providerTypeIdMissing_badRequest() {
        String request = "{\"dataSpace\": {\"name\": \"dataspacename\"}, \"dataService\": {\"dataServiceName\": \"dsName\"}, "
                + "\"dataProviderType\": {\"providerVersion\": \"V1\"}, \"messageStatusTopic\": {\"messageBusId\": 1, "
                + "\"encoding\": \"JSON\", \"name\": \"topic1\", \"specificationReference\": \"SpecRef\"}, "
                + "\"messageDataTopic\": {\"messageBusId\": 1, \"encoding\": \"JSON\", \"name\": \"topic1\"}, "
                + "\"dataType\": {\"mediumType\": \"stream\", \"schemaName\": \"SCH12\", \"schemaVersion\": \"1\", "
                + "\"isExternal\": true, \"consumedDataSpace\": \"10G2\", \"consumedDataCategory\": \"6G2\", "
                + "\"consumedDataProvider\": \"5G2\", \"consumedSchemaName\": \"5G2\", \"consumedSchemaVersion\": \"1\"}, "
                + "\"messageSchema\": {\"specificationReference\": \"SpecRef\"}}";

        ResponseEntity<String> response = new TestRestTemplate().exchange(
                REGISTER_MESSAGE_SCHEMA_URI,
                HttpMethod.PUT,
                new HttpEntity<>(request, headers),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void register_dataSpaceMissing_badRequest() {
        String request = "{\"dataService\": {\"dataServiceName\": \"dsName\"}, \"dataProviderType\": {"
                + "\"providerTypeId\": \"v1\", \"providerVersion\": \"V1\"}, \"messageStatusTopic\": {"
                + "\"messageBusId\": 1, \"encoding\": \"JSON\", \"name\": \"topic1\", \"specificationReference\": "
                + "\"SpecRef\"}, \"messageDataTopic\": {\"messageBusId\": 1, \"encoding\": \"JSON\",\"name\": \"topic1\"}, "
                + "\"dataType\": {\"mediumType\": \"stream\", \"schemaName\": \"SCH12\", \"schemaVersion\": \"1\", "
                + "\"isExternal\": true, \"consumedDataSpace\": \"10G2\", \"consumedDataCategory\": \"6G2\", "
                + "\"consumedDataProvider\": \"5G2\", \"consumedSchemaName\": \"5G2\", \"consumedSchemaVersion\": \"1\"}, "
                + "\"messageSchema\": {\"specificationReference\": \"SpecRef\"}}";

        ResponseEntity<String> response = new TestRestTemplate().exchange(
                REGISTER_MESSAGE_SCHEMA_URI,
                HttpMethod.PUT,
                new HttpEntity<>(request, headers),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void register_dataTopicNameMissing_badRequest() {
        String request = "{\"dataSpace\": {\"name\": \"dataspacename\"}, \"dataService\": {\"dataServiceName\": \"dsName\"}, "
                + "\"dataProviderType\": {\"providerTypeId\": \"v1\", \"providerVersion\": \"V1\"}, "
                + "\"messageStatusTopic\": {\"messageBusId\": 1, \"encoding\": \"JSON\", \"name\": \"topic1\", "
                + "\"specificationReference\": \"SpecRef\"}, \"messageDataTopic\": {\"messageBusId\": 1, "
                + "\"encoding\": \"JSON\"}, \"dataType\": {\"mediumType\": \"stream\", \"schemaName\": \"SCH12\", "
                + "\"schemaVersion\": \"1\", \"isExternal\": true, \"consumedDataSpace\": \"10G2\", "
                + "\"consumedDataCategory\": \"6G2\", \"consumedDataProvider\": \"5G2\", \"consumedSchemaName\": \"5G2\", "
                + "\"consumedSchemaVersion\": \"1\"}, \"messageSchema\": {\"specificationReference\": \"SpecRef\"}}";

        ResponseEntity<String> response = new TestRestTemplate().exchange(
                REGISTER_MESSAGE_SCHEMA_URI,
                HttpMethod.PUT,
                new HttpEntity<>(request, headers),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void register_messageStatusTopicNameMissing_badRequest() {
        String request = "{\"dataSpace\": {\"name\": \"dataspacename\"}, \"dataService\": {\"dataServiceName\": \"dsName\"}, "
                + "\"dataProviderType\": {\"providerTypeId\": \"v1\", \"providerVersion\": \"V1\"}, "
                + "\"messageStatusTopic\": {\"encoding\": \"JSON\", \"specificationReference\": \"SpecRef\"}, "
                + "\"messageDataTopic\": {\"encoding\": \"JSON\", \"name\": \"topic1\"}, \"dataType\": {"
                + "\"mediumType\": \"stream\", \"schemaName\": \"SCH12\", \"schemaVersion\": \"1\", "
                + "\"isExternal\": true, \"consumedDataSpace\": \"10G2\", \"consumedDataCategory\": \"6G2\", "
                + "\"consumedDataProvider\": \"5G2\", \"consumedSchemaName\": \"5G2\", \"consumedSchemaVersion\": \"1\"}, "
                + "\"messageSchema\": {\"specificationReference\": \"SpecRef\"}}";

        ResponseEntity<String> response = new TestRestTemplate().exchange(
                REGISTER_MESSAGE_SCHEMA_URI,
                HttpMethod.PUT,
                new HttpEntity<>(request, headers),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}

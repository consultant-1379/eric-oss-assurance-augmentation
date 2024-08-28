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

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;

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

/**
 * This is client contract tests for the POST /v1/augmentation-info/augmentation request that the AAS invokes to a registered ARDQ service(CARDQ) to
 * get augmentation data.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
@AutoConfigureStubRunner(ids = "com.ericsson.oss.air:eric-oss-core-reporting-dimension-query-main::stubs:10101",
                         stubsMode = StubRunnerProperties.StubsMode.REMOTE,
                         repositoryRoot = "https://arm.seli.gic.ericsson.se/artifactory/proj-bos-assurance-release-local")
public class CardqRestClientContractTest {

    final static String AUGMENTATION_URL = "http://localhost:10101/v1/augmentation-info/augmentation";

    final static HttpHeaders headers = new HttpHeaders();

    @BeforeEach
    void initialisingHeader() {
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    }

    @Test
    void shouldReturnErrorOnInvalidRequest() {
        final String requestBody = "invalidJson";

        final ResponseEntity<String> response = new TestRestTemplate().exchange(
                AUGMENTATION_URL,
                HttpMethod.POST,
                new HttpEntity<>(requestBody, headers),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldReturnErrorAugmentationMissedNodeFDN() {
        final String requestBody = "{\"inputFields\":[{\"name\":\"snssai\",\"value\":\"12-1\"}],\"augmentationFields\":[{\"name\":\"nsi\"},"
                + "{\"name\":\"nssi\"}]}";

        final ResponseEntity<String> response = new TestRestTemplate().exchange(
                AUGMENTATION_URL,
                HttpMethod.POST,
                new HttpEntity<>(requestBody, headers),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldReturnErrorDuplicateInputField() {
        final String requestBody = "{\"inputFields\":[{\"name\":\"nodeFDN\",\"value\":\"duplicate\"},{\"name\":\"snssai\",\"value\":\"11-1\"},{\"name"
                + "\":\"snssai\",\"value\":\"12-1\"}],\"augmentationFields\":[{\"name\":\"nsi\"},{\"name\":\"nssi\"}]}";

        final ResponseEntity<String> response = new TestRestTemplate().exchange(
                AUGMENTATION_URL,
                HttpMethod.POST,
                new HttpEntity<>(requestBody, headers),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldReturnErrorMissingAugmentationFields() {
        final String requestBody = "{\"inputFields\":[{\"name\":\"nodeFDN\",\"value\":\"pcc-amf1\"},{\"name\":\"snssai\",\"value\":\"12-1\"}]}";

        final ResponseEntity<String> response = new TestRestTemplate().exchange(
                AUGMENTATION_URL,
                HttpMethod.POST,
                new HttpEntity<>(requestBody, headers),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldReturnErrorMissingInputFields() {
        final String requestBody = "{\"augmentationFields\":[{\"name\":\"nsi\"},{\"name\":\"nssi\"}]}";

        final ResponseEntity<String> response = new TestRestTemplate().exchange(
                AUGMENTATION_URL,
                HttpMethod.POST,
                new HttpEntity<>(requestBody, headers),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Disabled("This junit is not stable and needs to be fixed, commented out to test DPaaS! See ESOA-12658.")
    @Test
    void shouldAcceptAugmentation_multipleAugmentationFields() {
        final String request = "{\"inputFields\": [{\"name\": \"nodeFDN\",\"value\": \"MeContext=PCC00010,ManagedElement=PCC00010\"},{\"name\": "
                + "\"snssai\",\"value\": \"1-1\"}],\"augmentationFields\": [{\"name\": \"nssi\"},{\"name\": \"site\"}]}";
        final String expectedResponse =
                "{\"fields\":[[{\"name\":\"nssi\",\"value\":\"NetworkSliceSubnet:NSSI-A11\"},{\"name\":\"site\",\"value\":\"GeographicSite:DataCenter1\"}],"
                        + "[{\"name\":\"nssi\",\"value\":\"NetworkSliceSubnet:NSSI-A1\"},{\"name\":\"site\",\"value\":\"GeographicSite:DataCenter1\"}]]}";

        final ResponseEntity<String> response = new TestRestTemplate().exchange(
                AUGMENTATION_URL,
                HttpMethod.POST,
                new HttpEntity<>(request, headers),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
        assertEquals(expectedResponse, response.getBody());
    }

    @Test
    void shouldAcceptAugmentation_singleAugmentationField_nssi() {
        final String request = "{\"inputFields\": ["
                + "{\"name\": \"nodeFDN\",\"value\": \"MeContext=PCC00010,ManagedElement=PCC00010\"},"
                + "{\"name\": \"snssai\",\"value\": \"1-1\"}],"
                + "\"augmentationFields\": [{\"name\": \"nssi\"}]}";
        final String expectedResponse = "{\"fields\":["
                + "[{\"name\":\"nssi\",\"value\":\"NetworkSliceSubnet:NSSI-A11\"}],"
                + "[{\"name\":\"nssi\",\"value\":\"NetworkSliceSubnet:NSSI-A1\"}]]}";

        final ResponseEntity<String> response = new TestRestTemplate().exchange(
                AUGMENTATION_URL,
                HttpMethod.POST,
                new HttpEntity<>(request, headers),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
        assertEquals(response.getBody(), expectedResponse);
    }

    @Test
    void shouldAcceptAugmentation_singleAugmentationField_site() {
        final String request = "{\"inputFields\": [{\"name\": \"nodeFDN\",\"value\": \"MeContext=PCC00010,ManagedElement=PCC00010\"},{\"name\": "
                + "\"snssai\",\"value\": \"1-1\"}],\"augmentationFields\": [{\"name\": \"site\"}]}";
        final String expectedResponse = "{\"fields\":[[{\"name\":\"site\",\"value\":\"GeographicSite:DataCenter1\"}]]}";

        final ResponseEntity<String> response = new TestRestTemplate().exchange(
                AUGMENTATION_URL,
                HttpMethod.POST,
                new HttpEntity<>(request, headers),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
        assertEquals(response.getBody(), expectedResponse);
    }
}

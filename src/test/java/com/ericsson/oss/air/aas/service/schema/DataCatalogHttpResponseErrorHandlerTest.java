/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.aas.service.schema;

import static com.ericsson.oss.air.util.logging.TestLoggingUtils.assertAuditEvent;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.List;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.ericsson.oss.air.aas.service.BaseHttpResponseErrorHandler;
import com.ericsson.oss.air.exception.http.problem.exception.HttpInternalServerErrorException;
import com.ericsson.oss.air.exception.http.problem.exception.HttpNotFoundRequestProblemException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;

@ExtendWith(MockitoExtension.class)
class DataCatalogHttpResponseErrorHandlerTest {

    private static final URI TEST_URL = URI.create(
            "http://localhost:8080/catalog/v1/data-type?dataSpace=5G&dataCategory=PM_COUNTERS&schemaName=AMF_Mobility_NetworkSlice_1");

    private static final HttpMethod TEST_METHOD = HttpMethod.GET;

    @InjectMocks
    private DataCatalogHttpResponseErrorHandler errorHandler;

    @Mock
    private ClientHttpResponse response;

    private Logger log;

    private ListAppender<ILoggingEvent> listAppender;

    @BeforeEach
    public void setUp() {
        this.log = (Logger) LoggerFactory.getLogger(BaseHttpResponseErrorHandler.class);
        this.log.setLevel(Level.INFO);

        this.listAppender = new ListAppender<>();
        this.listAppender.start();

        this.log.addAppender(this.listAppender);
    }

    @AfterEach
    public void tearDown() throws Exception {
        this.listAppender.stop();
    }

    @Test
    void hasError() throws IOException {
        when(response.getStatusCode()).thenReturn(HttpStatus.TOO_MANY_REQUESTS);
        assertTrue(errorHandler.hasError(response));

        when(response.getStatusCode()).thenReturn(HttpStatus.SERVICE_UNAVAILABLE);
        assertTrue(errorHandler.hasError(response));
    }

    @Test
    void handleError_notFound_success() throws IOException {
        final String notFoundResponseBody = "{\n" +
                "    \"timeStamp\": \"2023-04-13T19:01:26.692007\",\n" +
                "    \"message\": \"Requested Resource not found\"\n" +
                "    }";
        when(response.getStatusCode()).thenReturn(HttpStatus.NOT_FOUND);
        when(response.getBody()).thenReturn(new ByteArrayInputStream(notFoundResponseBody.getBytes()));
        assertThrows(HttpNotFoundRequestProblemException.class,
                () -> errorHandler.handleError(response));
    }

    @Test
    void handleError_badRequest_success() throws IOException {
        final String badRequestResponseBody = "{\"timeStamp\":\"2023-04-26T15:06:15.030577\",\"message\":\"Invalid Query Params-Allowed QueryParams(dataSpace, dataCategory, dataProvider, schemaName, schemaVersion, serviceName, isExternal)\"}";
        when(response.getStatusCode()).thenReturn(HttpStatus.BAD_REQUEST);
        when(response.getBody()).thenReturn(new ByteArrayInputStream(badRequestResponseBody.getBytes()));
        assertThrows(HttpInternalServerErrorException.class,
                () -> errorHandler.handleError(response));
    }

    @Test
    void handleError_conflict_success() throws IOException {
        final String conflictResponseBody = "{\n" +
                "    \"timeStamp\": \"2022-08-23T11:25:56.573865\",\n" +
                "    \"message\": \"Message Schema already exists\"\n" +
                "   }";
        when(response.getStatusCode()).thenReturn(HttpStatus.CONFLICT);
        when(response.getBody()).thenReturn(new ByteArrayInputStream(conflictResponseBody.getBytes()));
        assertThrows(HttpInternalServerErrorException.class,
                () -> errorHandler.handleError(response));
    }

    @Test
    void handleError_serviceUnavailable_success() throws IOException {
        when(response.getStatusCode()).thenReturn(HttpStatus.SERVICE_UNAVAILABLE);
        when(response.getBody()).thenReturn(new ByteArrayInputStream("" .getBytes()));
        assertThrows(HttpInternalServerErrorException.class,
                () -> errorHandler.handleError(response));
    }

    @Test
    void handleError_noContent_success() throws IOException {
        when(response.getStatusCode()).thenReturn(HttpStatus.NO_CONTENT);
        when(response.getBody()).thenReturn(new ByteArrayInputStream("" .getBytes()));
        assertThrows(HttpInternalServerErrorException.class,
                () -> errorHandler.handleError(response));
    }

    @Test
    void handleErrorWithUrlAndMethod_notFound_success() throws IOException {
        final String notFoundResponseBody = "{\n" +
                "    \"timeStamp\": \"2023-04-13T19:01:26.692007\",\n" +
                "    \"message\": \"Requested Resource not found\"\n" +
                "    }";
        when(response.getStatusCode()).thenReturn(HttpStatus.NOT_FOUND);
        when(response.getBody()).thenReturn(new ByteArrayInputStream(notFoundResponseBody.getBytes()));
        assertThrows(HttpNotFoundRequestProblemException.class,
                () -> errorHandler.handleError(TEST_URL, TEST_METHOD, response));

        final List<ILoggingEvent> loggingEventList = this.listAppender.list;

        assertFalse(loggingEventList.isEmpty());
        assertEquals(1, loggingEventList.size());
        assertAuditEvent(loggingEventList.get(0), Level.ERROR,
                "GET http://localhost:8080/catalog/v1/data-type?dataSpace=5G&dataCategory=PM_COUNTERS&schemaName=AMF_Mobility_NetworkSlice_1: 404 Not Found");

    }

    @Test
    void handleErrorWithUrlAndMethod_badRequest_success() throws IOException {
        final String badRequestResponseBody = "{\"timeStamp\":\"2023-04-26T15:06:15.030577\",\"message\":\"Invalid Query Params-Allowed QueryParams(dataSpace, dataCategory, dataProvider, schemaName, schemaVersion, serviceName, isExternal)\"}";
        when(response.getStatusCode()).thenReturn(HttpStatus.BAD_REQUEST);
        when(response.getBody()).thenReturn(new ByteArrayInputStream(badRequestResponseBody.getBytes()));
        assertThrows(HttpInternalServerErrorException.class,
                () -> errorHandler.handleError(TEST_URL, TEST_METHOD, response));

        final List<ILoggingEvent> loggingEventList = this.listAppender.list;

        assertFalse(loggingEventList.isEmpty());
        assertEquals(1, loggingEventList.size());
        assertAuditEvent(loggingEventList.get(0), Level.ERROR,
                "GET http://localhost:8080/catalog/v1/data-type?dataSpace=5G&dataCategory=PM_COUNTERS&schemaName=AMF_Mobility_NetworkSlice_1: 400 Bad Request");
    }

    @Test
    void handleErrorWithUrlAndMethod_conflict_success() throws IOException {
        final String conflictResponseBody = "{\n" +
                "    \"timeStamp\": \"2022-08-23T11:25:56.573865\",\n" +
                "    \"message\": \"Message Schema already exists\"\n" +
                "   }";
        when(response.getStatusCode()).thenReturn(HttpStatus.CONFLICT);
        when(response.getBody()).thenReturn(new ByteArrayInputStream(conflictResponseBody.getBytes()));
        assertThrows(HttpInternalServerErrorException.class,
                () -> errorHandler.handleError(TEST_URL, TEST_METHOD, response));

        final List<ILoggingEvent> loggingEventList = this.listAppender.list;

        assertFalse(loggingEventList.isEmpty());
        assertEquals(1, loggingEventList.size());
        assertAuditEvent(loggingEventList.get(0), Level.ERROR,
                "GET http://localhost:8080/catalog/v1/data-type?dataSpace=5G&dataCategory=PM_COUNTERS&schemaName=AMF_Mobility_NetworkSlice_1: 409 Conflict");
    }

    @Test
    void handleErrorWithUrlAndMethod_serviceUnavailable_success() throws IOException {
        when(response.getStatusCode()).thenReturn(HttpStatus.SERVICE_UNAVAILABLE);
        when(response.getBody()).thenReturn(new ByteArrayInputStream("" .getBytes()));
        assertThrows(HttpInternalServerErrorException.class,
                () -> errorHandler.handleError(TEST_URL, TEST_METHOD, response));

        final List<ILoggingEvent> loggingEventList = this.listAppender.list;

        assertFalse(loggingEventList.isEmpty());
        assertEquals(1, loggingEventList.size());
        assertAuditEvent(loggingEventList.get(0), Level.ERROR,
                "GET http://localhost:8080/catalog/v1/data-type?dataSpace=5G&dataCategory=PM_COUNTERS&schemaName=AMF_Mobility_NetworkSlice_1: 503 Service Unavailable");
    }

    @Test
    void handleErrorWithUrlAndMethod_IamATeapot_success() throws IOException {
        when(response.getStatusCode()).thenReturn(HttpStatus.I_AM_A_TEAPOT);
        when(response.getBody()).thenReturn(new ByteArrayInputStream("" .getBytes()));
        assertThrows(HttpInternalServerErrorException.class,
                () -> errorHandler.handleError(TEST_URL, TEST_METHOD, response));

        assertTrue(this.listAppender.list.isEmpty());
    }

}
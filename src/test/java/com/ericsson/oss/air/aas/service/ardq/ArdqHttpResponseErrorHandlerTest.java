/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.aas.service.ardq;

import static com.ericsson.oss.air.util.logging.TestLoggingUtils.assertAuditEvent;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.ericsson.oss.air.aas.service.BaseHttpResponseErrorHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;

@ExtendWith(MockitoExtension.class)
class ArdqHttpResponseErrorHandlerTest {

    private static final URI TEST_URL = URI.create("http://localhost:8080/v1/augmentation-info/augmentation");

    private static final HttpMethod TEST_METHOD = HttpMethod.POST;

    private ArdqHttpResponseErrorHandler ardqHttpResponseErrorHandler;

    @Mock
    private ClientHttpResponse response;

    @Mock
    private ResponseErrorHandler defaultResponseErrorHandler;

    private Logger log;

    private ListAppender<ILoggingEvent> listAppender;

    @BeforeEach
    public void setUp() {
        this.log = (Logger) LoggerFactory.getLogger(BaseHttpResponseErrorHandler.class);
        this.log.setLevel(Level.INFO);

        this.listAppender = new ListAppender<>();
        this.listAppender.start();

        this.log.addAppender(this.listAppender);

        this.ardqHttpResponseErrorHandler = new ArdqHttpResponseErrorHandler();
        this.ardqHttpResponseErrorHandler.setDefaultResponseErrorHandler(this.defaultResponseErrorHandler);
    }

    @AfterEach
    public void tearDown() throws Exception {
        this.listAppender.stop();
    }

    @Test
    void handleErrorWithUrlAndMethod_badRequest_success() throws IOException {

        when(response.getStatusCode()).thenReturn(HttpStatus.BAD_REQUEST);

        ardqHttpResponseErrorHandler.handleError(TEST_URL, TEST_METHOD, response);

        final List<ILoggingEvent> loggingEventList = this.listAppender.list;

        assertFalse(loggingEventList.isEmpty());
        assertEquals(1, loggingEventList.size());
        assertAuditEvent(loggingEventList.get(0), Level.ERROR,
                "POST http://localhost:8080/v1/augmentation-info/augmentation: 400 Bad Request");
    }

    @Test
    void handleErrorWithUrlAndMethod_unauthorized_success() throws IOException {

        when(response.getStatusCode()).thenReturn(HttpStatus.UNAUTHORIZED);
        ardqHttpResponseErrorHandler.handleError(TEST_URL, TEST_METHOD, response);

        final List<ILoggingEvent> loggingEventList = this.listAppender.list;

        assertFalse(loggingEventList.isEmpty());
        assertEquals(1, loggingEventList.size());
        assertAuditEvent(loggingEventList.get(0), Level.ERROR,
                "POST http://localhost:8080/v1/augmentation-info/augmentation: 401 Unauthorized");
    }

    @Test
    void handleErrorWithUrlAndMethod_serviceUnavailable_success() throws IOException {

        when(response.getStatusCode()).thenReturn(HttpStatus.SERVICE_UNAVAILABLE);
        ardqHttpResponseErrorHandler.handleError(TEST_URL, TEST_METHOD, response);

        final List<ILoggingEvent> loggingEventList = this.listAppender.list;

        assertFalse(loggingEventList.isEmpty());
        assertEquals(1, loggingEventList.size());
        assertAuditEvent(loggingEventList.get(0), Level.ERROR,
                "POST http://localhost:8080/v1/augmentation-info/augmentation: 503 Service Unavailable");
    }

    @Test
    void handleErrorWithUrlAndMethod_IamATeapot_success() throws IOException {

        when(response.getStatusCode()).thenReturn(HttpStatus.I_AM_A_TEAPOT);
        ardqHttpResponseErrorHandler.handleError(TEST_URL, TEST_METHOD, response);

        assertTrue(this.listAppender.list.isEmpty());
    }
}
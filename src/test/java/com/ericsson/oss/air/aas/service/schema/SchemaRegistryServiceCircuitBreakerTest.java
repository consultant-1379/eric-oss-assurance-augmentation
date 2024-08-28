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
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.ericsson.oss.air.aas.config.RetryConfiguration;
import com.ericsson.oss.air.aas.config.schemaregistry.SchemaRegistryConfiguration;
import com.ericsson.oss.air.exception.SchemaRegistryHttpServerErrorException;
import io.confluent.kafka.schemaregistry.avro.AvroSchema;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.springboot3.circuitbreaker.autoconfigure.CircuitBreakerAutoConfiguration;
import io.github.resilience4j.springboot3.retry.autoconfigure.RetryAutoConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = { SchemaRegistryService.class, CircuitBreakerAutoConfiguration.class, RetryAutoConfiguration.class,
        RetryConfiguration.class })
@ActiveProfiles("test")
class SchemaRegistryServiceCircuitBreakerTest {

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @Autowired
    private RetryRegistry retryRegistry;

    @Autowired
    private SchemaRegistryService schemaRegistryService;

    @MockBean
    private SchemaRegistryConfiguration schemaRegistryConfiguration;

    @Mock
    private SchemaRegistryClient schemaRegistryClient;

    private static final String SCHEMA_REGISTRY_CIRCUIT_BREAKER = "schemaRegistry";

    private static final String SUBJECT = "nose_test_topic_p1_r3_pw";

    private static final AvroSchema PARSED_SCHEMA = new AvroSchema(avroSchemaString(SUBJECT));

    private static final RestClientException httpClientException = new RestClientException("Testing Rest Client Exception", 404, 40404);

    private static final RestClientException httpServerException = new RestClientException("Testing Rest Server Exception", 500, 50004);

    private static final int EXPECTED_VERSION = 1;

    private CircuitBreaker circuitBreaker;

    private Retry retry;

    private Callable registerCallable;

    private Callable getVersionCallable;

    private Callable getLatestSchemaCallable;

    private Logger log;

    private ListAppender<ILoggingEvent> listAppender;

    @BeforeEach
    void setUp() {
        this.circuitBreaker = this.circuitBreakerRegistry.circuitBreaker(SCHEMA_REGISTRY_CIRCUIT_BREAKER);
        this.retry = this.retryRegistry.retry(SCHEMA_REGISTRY_CIRCUIT_BREAKER);

        this.registerCallable = () -> {
            this.schemaRegistryService.register(SUBJECT, PARSED_SCHEMA);
            return null;
        };

        this.getVersionCallable = () -> {
            this.schemaRegistryService.getVersion(SUBJECT, PARSED_SCHEMA);
            return null;
        };

        this.getLatestSchemaCallable = () -> {
            this.schemaRegistryService.getLatestSchema((SUBJECT));
            return null;
        };

        when(this.schemaRegistryConfiguration.getSchemaRegistryClient()).thenReturn(this.schemaRegistryClient);

        this.log = (Logger) LoggerFactory.getLogger(RetryConfiguration.class);
        this.log.setLevel(Level.INFO);

        this.listAppender = new ListAppender<>();
        this.listAppender.start();

        this.log.addAppender(this.listAppender);

    }

    @AfterEach
    void tearDown() {
        this.circuitBreaker.reset();
        this.listAppender.stop();

    }

    @Test
    void retry_config_LoadCorrectly() {
        assertEquals(3, this.retryRegistry.retry(SCHEMA_REGISTRY_CIRCUIT_BREAKER).getRetryConfig().getMaxAttempts());
    }

    @Test
    void circuitBreaker_config_LoadCorrectly() {
        assertEquals(5, this.circuitBreakerRegistry.circuitBreaker(SCHEMA_REGISTRY_CIRCUIT_BREAKER)
                .getCircuitBreakerConfig()
                .getMinimumNumberOfCalls());
    }

    @Test
    void registerMethod_Failed_withRetryTriggered() throws RestClientException, IOException {

        when(this.schemaRegistryClient.register(SUBJECT, PARSED_SCHEMA)).thenThrow(httpServerException);
        assertThrows(RestClientException.class, () -> this.retry.executeCallable(this.registerCallable));
        verify(this.schemaRegistryClient, times(3)).register(SUBJECT, PARSED_SCHEMA);

        final List<ILoggingEvent> loggingEventList = this.listAppender.list;

        assertFalse(loggingEventList.isEmpty());
        assertEquals(1, loggingEventList.size());
        assertAuditEvent(loggingEventList.get(0), Level.ERROR, "Retries exhausted after 3 attempts. Cause: ");
        assertEquals(SchemaRegistryHttpServerErrorException.class.getName(), loggingEventList.get(0).getThrowableProxy().getClassName());
    }

    @Test
    void registerMethod_Failed_NoRetryTriggered() throws RestClientException, IOException {

        when(this.schemaRegistryClient.register(SUBJECT, PARSED_SCHEMA)).thenThrow(httpClientException);
        assertThrows(RestClientException.class, () -> this.retry.executeCallable(this.registerCallable));
        verify(this.schemaRegistryClient, times(1)).register(SUBJECT, PARSED_SCHEMA);
    }

    @Test
    void registerMethod_Succeed_withRetryTriggered() throws RestClientException, IOException {
        given(this.schemaRegistryClient.register(SUBJECT, PARSED_SCHEMA))
                .willThrow(httpServerException)
                .willReturn(EXPECTED_VERSION);
        final long numberOfSuccessfulCallsWithRetryAttemptBefore = this.retry.getMetrics().getNumberOfSuccessfulCallsWithRetryAttempt();

        assertDoesNotThrow(() -> this.retry.executeCallable(this.registerCallable));

        final long numberOfSuccessfulCallsWithRetryAttemptAfter = this.retry.getMetrics().getNumberOfSuccessfulCallsWithRetryAttempt();

        verify(this.schemaRegistryClient, times(2)).register(SUBJECT, PARSED_SCHEMA);
        assertEquals(1, numberOfSuccessfulCallsWithRetryAttemptAfter - numberOfSuccessfulCallsWithRetryAttemptBefore);

    }

    @Test
    void registerMethod_Failed_due_to_CircuitBreakerOpened() {
        this.circuitBreaker.transitionToOpenState();
        assertThrows(CallNotPermittedException.class, () -> this.circuitBreaker.executeCallable(this.registerCallable));
    }

    @Test
    void registerMethod_Failed_withStateChange() throws RestClientException, IOException {

        when(this.schemaRegistryClient.register(SUBJECT, PARSED_SCHEMA)).thenThrow(RestClientException.class);

        for (int j = 0; j < 5; j++) {
            assertEquals(CircuitBreaker.State.CLOSED, this.circuitBreaker.getState());
            assertThrows(RestClientException.class, () -> this.circuitBreaker.executeCallable(this.registerCallable));
        }

        // Register method is involved 5 times
        verify(this.schemaRegistryClient, times(5)).register(SUBJECT, PARSED_SCHEMA);

        // Circuit Breaker state is changed to Open
        assertEquals(CircuitBreaker.State.OPEN, this.circuitBreaker.getState());

        assertThrows(CallNotPermittedException.class, () -> this.circuitBreaker.executeCallable(this.registerCallable));

        // Circuit Breaker hijack register method, so register is only involved 5 times
        verify(this.schemaRegistryClient, times(5)).register(SUBJECT, PARSED_SCHEMA);
    }

    @Test
    void registerMethod_ThrowsServiceUnavailableException_WithCircuitBreakerAndRetries() throws IOException, RestClientException {
        when(this.schemaRegistryClient.register(SUBJECT, PARSED_SCHEMA)).thenThrow(httpServerException);

        // first failure
        assertThrows(RestClientException.class,
                () -> this.retry.executeCallable(() -> this.circuitBreaker.executeCallable(this.registerCallable)));

        verify(this.schemaRegistryClient, times(3)).register(SUBJECT, PARSED_SCHEMA);

        // after the first failure, as HttpServiceUnavailableException is configured for retry, CallNotPermittedException will be thrown
        assertThrows(CallNotPermittedException.class,
                () -> this.retry.executeCallable(() -> this.circuitBreaker.executeCallable(this.registerCallable)));

        // schemaRegistryClient.register() will be called 5 times total, the last retry is hijacted by circuit breaker
        verify(this.schemaRegistryClient, times(5)).register(SUBJECT, PARSED_SCHEMA);

        final List<ILoggingEvent> loggingEventList = this.listAppender.list;

        assertFalse(loggingEventList.isEmpty());
        assertEquals(1, loggingEventList.size());
        assertAuditEvent(loggingEventList.get(0), Level.ERROR, "Retries exhausted after 3 attempts. Cause: ");
        assertEquals(SchemaRegistryHttpServerErrorException.class.getName(), loggingEventList.get(0).getThrowableProxy().getClassName());

    }

    @Test
    void getVersionMethod_isConfiguredWithCircuitBreaker() {
        this.circuitBreaker.transitionToOpenState();
        assertThrows(CallNotPermittedException.class, () -> this.circuitBreaker.executeCallable(this.getVersionCallable));
    }

    @Test
    void getVersionMethod_isConfiguredWithRetry() throws IOException, RestClientException {
        when(this.schemaRegistryService.getVersion(SUBJECT, PARSED_SCHEMA)).thenThrow(httpServerException);
        assertThrows(RestClientException.class, () -> this.retry.executeCallable(this.getVersionCallable));
        verify(this.schemaRegistryClient, times(3)).getVersion(SUBJECT, PARSED_SCHEMA);

        final List<ILoggingEvent> loggingEventList = this.listAppender.list;

        assertFalse(loggingEventList.isEmpty());
        assertEquals(1, loggingEventList.size());
        assertAuditEvent(loggingEventList.get(0), Level.ERROR, "Retries exhausted after 3 attempts. Cause: ");
        assertEquals(SchemaRegistryHttpServerErrorException.class.getName(), loggingEventList.get(0).getThrowableProxy().getClassName());
    }

    @Test
    void getLatestSchemaMetadata_isConfiguredWithCircuitBreaker() {
        this.circuitBreaker.transitionToOpenState();
        assertThrows(CallNotPermittedException.class, () -> this.circuitBreaker.executeCallable(this.getLatestSchemaCallable));
    }

    @Test
    void getLatestSchemaMetadata_isConfiguredWithRetry() throws IOException, RestClientException {
        when(this.schemaRegistryClient.getLatestSchemaMetadata(SUBJECT)).thenThrow(httpServerException);
        assertThrows(RestClientException.class, () -> this.retry.executeCallable(this.getLatestSchemaCallable));
        verify(this.schemaRegistryClient, times(3)).getLatestSchemaMetadata(SUBJECT);

        final List<ILoggingEvent> loggingEventList = this.listAppender.list;

        assertFalse(loggingEventList.isEmpty());
        assertEquals(1, loggingEventList.size());
        assertAuditEvent(loggingEventList.get(0), Level.ERROR, "Retries exhausted after 3 attempts. Cause: ");
        assertEquals(SchemaRegistryHttpServerErrorException.class.getName(), loggingEventList.get(0).getThrowableProxy().getClassName());
    }

    private static String avroSchemaString(final String name) {
        return "{\"type\":\"record\",\"name\":\"" + name + "\"," + "\"fields\":[{\"name\":\"name\",\"type\":\"string\"},"
                + "{\"name\":\"secondFiled\",\"type\":\"int\"}]}";
    }
}
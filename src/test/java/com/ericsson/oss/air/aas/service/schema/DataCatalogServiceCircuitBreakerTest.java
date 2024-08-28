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

import static com.ericsson.oss.air.aas.handler.RegistrationTestUtil.INPUT_SCHEMA_REFERENCE_OBJ;
import static com.ericsson.oss.air.aas.service.schema.DataCatalogService.REGISTER_MESSAGE_SCHEMA_URI;
import static com.ericsson.oss.air.util.logging.TestLoggingUtils.assertAuditEvent;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.concurrent.Callable;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.ericsson.oss.air.aas.config.RetryConfiguration;
import com.ericsson.oss.air.aas.model.datacatalog.request.InnerMessageSchemaRequestDto;
import com.ericsson.oss.air.aas.model.datacatalog.request.MessageSchemaRequestDto;
import com.ericsson.oss.air.aas.model.datacatalog.response.MessageSchemaResponseDto;
import com.ericsson.oss.air.exception.http.problem.exception.HttpInternalServerErrorException;
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
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@SpringBootTest(classes = { DataCatalogService.class, CircuitBreakerAutoConfiguration.class, RetryAutoConfiguration.class, RetryConfiguration.class })
@ActiveProfiles("test")
class DataCatalogServiceCircuitBreakerTest {

    private static final String DATA_CATALOG_URL = "http://localhost:9590";

    private static final String DATA_CATALOG_CIRCUIT_BREAKER = "dataCatalog";

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @Autowired
    private RetryRegistry retryRegistry;

    @Autowired
    private DataCatalogService dataCatalogService;

    @MockBean
    private RestTemplate restTemplate;

    private CircuitBreaker circuitBreaker;

    private Retry retry;

    private Callable registerCallable;

    private Callable retrieveSchemaMetadataCallable;

    private Logger log;

    private ListAppender<ILoggingEvent> listAppender;

    @BeforeEach
    void setup() {
        this.circuitBreaker = this.circuitBreakerRegistry.circuitBreaker(DATA_CATALOG_CIRCUIT_BREAKER);

        this.retry = this.retryRegistry.retry(DATA_CATALOG_CIRCUIT_BREAKER);

        this.registerCallable = () -> {
            this.dataCatalogService.register(buildMessageSchemaRequestDto());
            return null;
        };

        this.retrieveSchemaMetadataCallable = () -> {
            this.dataCatalogService.retrieveSchemaMetadata(INPUT_SCHEMA_REFERENCE_OBJ);
            return null;
        };

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
    void retry_config_loadCorrectly() {
        assertEquals(3, this.retryRegistry.retry(DATA_CATALOG_CIRCUIT_BREAKER).getRetryConfig().getMaxAttempts());
    }

    @Test
    void circuitBreaker_config_loadCorrectly() {
        assertEquals(5, this.circuitBreakerRegistry.circuitBreaker(DATA_CATALOG_CIRCUIT_BREAKER)
                .getCircuitBreakerConfig()
                .getMinimumNumberOfCalls());
    }

    @Test
    void register_isConfiguredWithCircuitBreaker() {
        this.circuitBreaker.transitionToOpenState();
        assertThrows(CallNotPermittedException.class, () -> this.circuitBreaker.executeCallable(this.registerCallable));
    }

    @Test
    void register_isConfiguredWithRetry() {

        final Exception exception = new HttpInternalServerErrorException("500 INTERNAL_SERVER_ERROR", null, null);

        given(this.restTemplate.exchange(DATA_CATALOG_URL + REGISTER_MESSAGE_SCHEMA_URI, HttpMethod.PUT,
                this.getRequestEntityForMessageSchemaRequestDto(buildMessageSchemaRequestDto()), MessageSchemaResponseDto.class))
                .willThrow(ResourceAccessException.class)
                .willThrow(exception);
        assertThrows(HttpInternalServerErrorException.class, () -> this.retry.executeCallable(this.registerCallable));
        verify(this.restTemplate, times(3)).exchange(DATA_CATALOG_URL + REGISTER_MESSAGE_SCHEMA_URI, HttpMethod.PUT,
                this.getRequestEntityForMessageSchemaRequestDto(buildMessageSchemaRequestDto()),
                MessageSchemaResponseDto.class);

        final List<ILoggingEvent> loggingEventList = this.listAppender.list;

        assertFalse(loggingEventList.isEmpty());
        assertEquals(1, loggingEventList.size());
        assertAuditEvent(loggingEventList.get(0), Level.ERROR, "Retries exhausted after 3 attempts. Cause: ", exception, "500 INTERNAL_SERVER_ERROR");
    }

    @Test
    void retrieveInputSchemaMetadata_isConfiguredWithCircuitBreaker() {
        this.circuitBreaker.transitionToOpenState();
        assertThrows(CallNotPermittedException.class, () -> this.circuitBreaker.executeCallable(this.retrieveSchemaMetadataCallable));
    }

    @Test
    void retrieveInputSchemaMetadata_isConfiguredWithRetry() {

        final Exception exception = new HttpInternalServerErrorException("503 SERVICE_UNAVAILABLE", null, null);

        given(this.restTemplate.exchange(anyString(), any(), any(), any(ParameterizedTypeReference.class)))
                .willThrow(ResourceAccessException.class)
                .willThrow(exception);

        assertThrows(HttpInternalServerErrorException.class, () -> this.retry.executeCallable(this.retrieveSchemaMetadataCallable));
        verify(this.restTemplate, times(3)).exchange(anyString(), any(), any(), any(ParameterizedTypeReference.class));

        final List<ILoggingEvent> loggingEventList = this.listAppender.list;

        assertFalse(loggingEventList.isEmpty());
        assertEquals(1, loggingEventList.size());
        assertAuditEvent(loggingEventList.get(0), Level.ERROR, "Retries exhausted after 3 attempts. Cause: ", exception, "503 SERVICE_UNAVAILABLE");
    }

    private static MessageSchemaRequestDto buildMessageSchemaRequestDto() {
        return MessageSchemaRequestDto.builder()
                .messageSchema(InnerMessageSchemaRequestDto.builder()
                        .specificationReference("foobar")
                        .build())
                .build();
    }

    private HttpEntity getRequestEntityForMessageSchemaRequestDto(final MessageSchemaRequestDto messageSchemaRequestDto) {
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        final HttpEntity<MessageSchemaRequestDto> requestEntity = new HttpEntity<>(messageSchemaRequestDto, headers);

        return requestEntity;
    }

}

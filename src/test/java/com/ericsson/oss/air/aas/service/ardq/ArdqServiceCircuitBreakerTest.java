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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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
import com.ericsson.oss.air.aas.model.ardq.ArdqRequestDto;
import com.ericsson.oss.air.aas.model.ardq.ArdqResponseDto;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.springboot3.circuitbreaker.autoconfigure.CircuitBreakerAutoConfiguration;
import io.github.resilience4j.springboot3.retry.autoconfigure.RetryAutoConfiguration;
import io.micrometer.core.instrument.Counter;
import jakarta.validation.Validator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatusCode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@SpringBootTest(classes = { ArdqService.class, CircuitBreakerAutoConfiguration.class, RetryAutoConfiguration.class, RetryConfiguration.class })
@ActiveProfiles("test")
public class ArdqServiceCircuitBreakerTest {

    private static final String VALID_URL = "http://eric-oss-cardq:8080";

    private static final String ARDQ_CIRCUIT_BREAKER = "ardq";

    private static final List<String> AUGMENTED_VALUES = List.of("NSI-B", "NSI-C");

    private static final ArdqRequestDto REQUEST = ArdqRequestDto.newRequest()
            .addInputField("input", "value")
            .addOutputField("output");

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @Autowired
    private RetryRegistry retryRegistry;

    @Autowired
    private ArdqService ardqService;

    @MockBean
    private RestTemplate restTemplate;

    @MockBean
    private Validator validator;

    @MockBean
    private Counter counter;

    private CircuitBreaker circuitBreaker;

    private Retry retry;

    private Callable getAugmentationDataCallable;

    private Logger log;

    private ListAppender<ILoggingEvent> listAppender;

    @BeforeEach
    void setup() {

        this.circuitBreaker = this.circuitBreakerRegistry.circuitBreaker(ARDQ_CIRCUIT_BREAKER);

        this.retry = this.retryRegistry.retry(ARDQ_CIRCUIT_BREAKER);

        this.getAugmentationDataCallable = () -> {
            this.ardqService.getAugmentationData(VALID_URL, REQUEST);
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
        assertEquals(3, this.retryRegistry.retry(ARDQ_CIRCUIT_BREAKER).getRetryConfig().getMaxAttempts());
    }

    @Test
    void circuitBreaker_config_loadCorrectly() {
        assertEquals(5, this.circuitBreakerRegistry.circuitBreaker(ARDQ_CIRCUIT_BREAKER)
                .getCircuitBreakerConfig()
                .getMinimumNumberOfCalls());
    }

    @Test
    void getAugmentationData_isConfiguredWithCircuitBreaker() {
        this.circuitBreaker.transitionToOpenState();
        assertThrows(CallNotPermittedException.class, () -> this.circuitBreaker.executeCallable(this.getAugmentationDataCallable));
    }

    @Test
    void getAugmentationData_isConfiguredWithRetry() {

        final Exception exception = new HttpServerErrorException(HttpStatusCode.valueOf(500));

        given(this.restTemplate.exchange(anyString(), any(), any(), eq(ArdqResponseDto.class)))
                .willThrow(ResourceAccessException.class)
                .willThrow(exception);

        assertThrows(HttpServerErrorException.class, () -> this.retry.executeCallable(this.getAugmentationDataCallable));
        verify(this.restTemplate, times(3)).exchange(anyString(), any(), any(), eq(ArdqResponseDto.class));

        final List<ILoggingEvent> loggingEventList = this.listAppender.list;

        assertFalse(loggingEventList.isEmpty());
        assertEquals(1, loggingEventList.size());
        assertAuditEvent(loggingEventList.get(0), Level.ERROR, "Retries exhausted after 3 attempts. Cause: ", exception, "500 INTERNAL_SERVER_ERROR");
    }
}

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

package com.ericsson.oss.air.aas.exception.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.ericsson.oss.air.aas.handler.exception.CircuitBreakerHandler;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.junit.jupiter.api.Test;

public class CircuitBreakerHandlerTest {

    private final CircuitBreakerHandler handler = new CircuitBreakerHandler();

    @Test
    void testCallNotPermittedExceptionHandler() {
        CircuitBreaker breaker = CircuitBreaker.ofDefaults("test_breaker");
        try {
            throw CallNotPermittedException.createCallNotPermittedException(breaker);
        } catch (CallNotPermittedException exception) {
            handler.handleCallNotPermittedException(exception);
            assertEquals("test_breaker", exception.getCausingCircuitBreakerName());
        }
    }
}

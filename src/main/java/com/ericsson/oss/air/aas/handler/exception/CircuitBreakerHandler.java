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

package com.ericsson.oss.air.aas.handler.exception;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * A Handler to handle any exceptions thrown by Circuit Breaker.
 */
@ControllerAdvice
@Slf4j
public class CircuitBreakerHandler {

    /**
     * A handler for CircuitBreakers CallNotPermittedException
     *
     * @param exception The exception that was thrown
     */
    @ExceptionHandler({ CallNotPermittedException.class })
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public void handleCallNotPermittedException(CallNotPermittedException exception) {
        log.warn(exception.getMessage());
    }
}

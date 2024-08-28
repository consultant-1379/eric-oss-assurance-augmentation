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

package com.ericsson.oss.air.exception.http.problem.exception;

import lombok.Builder;
import org.springframework.http.HttpStatus;

/**
 * An Exception Extension of the HttpProblemException class for 503 Service Unavailable.
 */
public class HttpServiceUnavailableException extends HttpProblemException {
    @Builder
    public HttpServiceUnavailableException(final String description, final String instance, final String type) {
        super(HttpStatus.SERVICE_UNAVAILABLE, description, instance, type);
    }
}

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

import org.springframework.http.HttpStatus;

import lombok.Builder;

/**
 * An Exception Extension of the HttpProblemException class for 404 Not Found Request.
 */
public class HttpNotFoundRequestProblemException extends HttpProblemException {

    @Builder
    public HttpNotFoundRequestProblemException(final String description, final String instance, final String type) {
        super(HttpStatus.NOT_FOUND, description, instance, type);
    }
}

/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.exception.http.problem.exception;

import com.ericsson.oss.air.api.generated.model.Problem;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * RuntimeException to indicate an  HTTP exception during an AAS registration flow.
 */
@Getter
public class HttpProblemException extends RuntimeException {

    private static final String DEFAULT_TYPE = "about:blank";

    private final String type;

    private final HttpStatus httpStatus;

    private final String description;

    private final String instance;

    public HttpProblemException(final HttpStatus httpStatus, final String description, final String instance, final String type) {
        super(description);

        this.httpStatus = httpStatus;
        this.description = description;
        this.instance = instance;
        this.type = type != null ? type : DEFAULT_TYPE;
    }

    /**
     * Creates a Problem instance populated with the content of the HttpProblemException.
     */
    public Problem getProblem() {
        return new Problem()
                .type(type)
                .title(httpStatus.getReasonPhrase())
                .status(httpStatus.value())
                .detail(description)
                .instance(instance);
    }
}

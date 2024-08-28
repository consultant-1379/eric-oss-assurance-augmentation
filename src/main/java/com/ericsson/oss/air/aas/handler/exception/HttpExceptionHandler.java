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

import com.ericsson.oss.air.api.generated.model.Problem;
import com.ericsson.oss.air.exception.http.problem.exception.HttpProblemException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Http Exception handler class used to handle the throwing of generic and custom Http Exceptions
 */
@ControllerAdvice
@Slf4j
public class HttpExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * Generic Http Exception handler method
     *
     * @param throwable the generic exception that is thrown
     * @return the response entity with the problem object in the body
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Problem> handleAll(final Throwable throwable) {

        log.error("Error occurred for: [{}] with error: {}", throwable.getClass().getSimpleName(),
                throwable.getMessage());

        final Problem problem = new Problem()
                .title(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .detail(throwable.getMessage())
                .type("about:blank");

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .contentType(MediaType.APPLICATION_JSON)
                .body(problem);
    }

    /**
     * Custom Exception handler for HttpProblemExceptions
     *
     * @param httpProblemException the httpProblemException that is thrown
     * @return the response entity with the problem object in the body
     */
    @ExceptionHandler(HttpProblemException.class)
    public ResponseEntity<Problem> handleHttpProblemException(final HttpProblemException httpProblemException) {

        log.error("Error occurred for: [{}] with error: {}", httpProblemException.getClass().getSimpleName(),
                httpProblemException.getDescription());
        final Problem problem = httpProblemException.getProblem();

        return ResponseEntity.status(httpProblemException.getHttpStatus().value())
                .contentType(MediaType.APPLICATION_JSON)
                .body(problem);
    }
}

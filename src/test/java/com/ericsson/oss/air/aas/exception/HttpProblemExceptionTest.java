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

package com.ericsson.oss.air.aas.exception;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import com.ericsson.oss.air.api.generated.model.Problem;
import com.ericsson.oss.air.exception.http.problem.exception.HttpBadRequestProblemException;
import com.ericsson.oss.air.exception.http.problem.exception.HttpConflictRequestProblemException;
import com.ericsson.oss.air.exception.http.problem.exception.HttpNotFoundRequestProblemException;
import com.ericsson.oss.air.exception.http.problem.exception.HttpProblemException;

public class HttpProblemExceptionTest {
    @Test
    public void test_ProblemInstanceGeneratedFromHttpProblemException() {
        Problem problem = new HttpProblemException(HttpStatus.BAD_REQUEST, "Bad Request", "Test Instance", null).getProblem();
        assertTrue(problem.getStatus() == 400 &&
                           problem.getTitle().equals("Bad Request"));
    }

    @Test
    public void test_ProblemInstanceGeneratedFromBadRequestException() {
        Problem problem = HttpBadRequestProblemException.builder().build().getProblem();
        assertTrue(problem.getStatus() == 400 &&
                           problem.getTitle().equals("Bad Request"));
    }

    @Test
    public void test_ProblemInstanceGeneratedFromConflictRequestException() {
        Problem problem = HttpNotFoundRequestProblemException.builder().build().getProblem();
        assertTrue(problem.getStatus() == 404 &&
                           problem.getTitle().equals("Not Found"));
    }

    @Test
    public void test_ProblemInstanceGeneratedFromNotFoundRequestException() {
        Problem problem = HttpConflictRequestProblemException.builder().build().getProblem().type("test");
        assertTrue(problem.getStatus() == 409 &&
                           problem.getTitle().equals("Conflict"));
    }
}

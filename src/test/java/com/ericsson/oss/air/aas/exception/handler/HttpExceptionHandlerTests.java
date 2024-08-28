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

import static org.assertj.core.api.Assertions.assertThat;

import com.ericsson.oss.air.aas.handler.exception.HttpExceptionHandler;
import com.ericsson.oss.air.api.generated.model.Problem;
import com.ericsson.oss.air.exception.http.problem.exception.HttpBadRequestProblemException;
import com.ericsson.oss.air.exception.http.problem.exception.HttpConflictRequestProblemException;
import com.ericsson.oss.air.exception.http.problem.exception.HttpNotFoundRequestProblemException;
import com.ericsson.oss.air.exception.http.problem.exception.HttpProblemException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class HttpExceptionHandlerTests {

    HttpExceptionHandler httpExceptionHandler = new HttpExceptionHandler();

    @Test
    public void handleAll_returnEntityWithInternalServerError(){
        try{
            throw new RuntimeException("test");
        }catch (Exception e){
            ResponseEntity<Problem> problemResponseEntity = httpExceptionHandler.handleAll(e);
            Problem problem =problemResponseEntity.getBody();
            assertThat(problem.getTitle()).isEqualTo("Internal Server Error");
            assertThat(problem.getStatus()).isEqualTo(500);
            assertThat(problem.getDetail()).isEqualTo("test");
        }

    }

    @Test
    public void handleHttpProblemException_returnEntityWithInternalServerError(){
        try{
            throw new HttpProblemException(HttpStatus.INTERNAL_SERVER_ERROR,"test description","test instance", "about:blank");
        }catch(HttpProblemException httpProblemException){
            Problem problem = httpProblemException.getProblem();
            assertThat(problem.getTitle()).isEqualTo("Internal Server Error");
            assertThat(problem.getStatus()).isEqualTo(500);
            assertThat(problem.getType()).isEqualTo("about:blank");
        }

    }

    @Test
    public void handleHttpProblemException_withHttpConflictRequestProblemException_returnEntityConflictError(){
        try{
            throw HttpConflictRequestProblemException.builder().build();
        }catch(HttpProblemException httpProblemException){
            Problem problem = httpProblemException.getProblem();
            assertThat(problem.getTitle()).isEqualTo("Conflict");
            assertThat(problem.getStatus()).isEqualTo(409);
            assertThat(problem.getType()).isEqualTo("about:blank");
        }

    }

    @Test
    public void handleHttpProblemException_withHttpNotFoundRequestProblemException_returnEntityNotFoundError(){
        try{
            throw HttpNotFoundRequestProblemException.builder().build();
        }catch(HttpProblemException httpProblemException){
            Problem problem = httpProblemException.getProblem();
            assertThat(problem.getTitle()).isEqualTo("Not Found");
            assertThat(problem.getStatus()).isEqualTo(404);
            assertThat(problem.getType()).isEqualTo("about:blank");
        }

    }

    @Test
    public void handleHttpProblemException_withHttpBadRequestProblemException_returnEntityBadRequestError() {
        try {
            throw HttpBadRequestProblemException.builder().build();
        } catch (HttpProblemException httpProblemException) {
            Problem problem = httpProblemException.getProblem();
            assertThat(problem.getTitle()).isEqualTo("Bad Request");
            assertThat(problem.getStatus()).isEqualTo(400);
            assertThat(problem.getType()).isEqualTo("about:blank");
        }
    }

    @Test
    public void handleHttpProblemException_withHttpBadRequestProblemException_returnEntityBadRequestErrorWithUpdatedURL() {
        try {
            throw HttpBadRequestProblemException.builder()
                    .description("Test Description")
                    .instance("test value")
                    .type("/v1/augmentation/registration/ardq")
                    .build();
        } catch (HttpProblemException httpProblemException) {
            Problem problem = httpProblemException.getProblem();
            assertThat(problem.getTitle()).isEqualTo("Bad Request");
            assertThat(problem.getStatus()).isEqualTo(400);
            assertThat(problem.getType()).isEqualTo("/v1/augmentation/registration/ardq");
        }
    }

    @Test
    public void handleHttpProblemException_withConflictProblemException_returnEntityConflictErrorWithUpdatedURL() {
        try {
            throw HttpConflictRequestProblemException.builder()
                    .description("Test Description")
                    .instance("test value")
                    .type("/v1/augmentation/registration/ardq")
                    .build();
        } catch (HttpProblemException httpProblemException) {
            Problem problem = httpProblemException.getProblem();
            assertThat(problem.getTitle()).isEqualTo("Conflict");
            assertThat(problem.getStatus()).isEqualTo(409);
            assertThat(problem.getType()).isEqualTo("/v1/augmentation/registration/ardq");
        }
    }

    @Test
    public void handleHttpProblemException_withHttpNotFoundProblemException_returnEntityNotFoundErrorWithUpdatedURL() {
        try {
            throw HttpNotFoundRequestProblemException.builder()
                    .description("Test Description")
                    .instance("test value")
                    .type("/v1/augmentation/registration/ardq")
                    .build();
        } catch (HttpProblemException httpProblemException) {
            Problem problem = httpProblemException.getProblem();
            assertThat(problem.getTitle()).isEqualTo("Not Found");
            assertThat(problem.getStatus()).isEqualTo(404);
            assertThat(problem.getType()).isEqualTo("/v1/augmentation/registration/ardq");
        }
    }
}


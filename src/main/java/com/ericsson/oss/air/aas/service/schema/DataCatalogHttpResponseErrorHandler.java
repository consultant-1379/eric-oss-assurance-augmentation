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

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.ericsson.oss.air.aas.model.datacatalog.response.DataCatalogErrorResponse;
import com.ericsson.oss.air.aas.service.BaseHttpResponseErrorHandler;
import com.ericsson.oss.air.exception.http.problem.exception.HttpInternalServerErrorException;
import com.ericsson.oss.air.exception.http.problem.exception.HttpNotFoundRequestProblemException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.ObjectUtils;

/**
 * Implementation of ResponseErrorHandler for Data Catalog REST responses
 */
@Slf4j
public class DataCatalogHttpResponseErrorHandler extends BaseHttpResponseErrorHandler {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /*
     * Handles the error in the given response with the given resolved status code.
     * @param response client http response
     */
    @Override
    public void handleError(final ClientHttpResponse response) throws IOException {
        log.error("Data Catalog request failed: {} - {}", response.getStatusCode().value(), ((HttpStatus) response.getStatusCode()).getReasonPhrase());

        final String body = new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);

        DataCatalogErrorResponse errorResponse = null;
        if (!ObjectUtils.isEmpty(body)) {
            errorResponse = OBJECT_MAPPER.readValue(body, DataCatalogErrorResponse.class);
            log.error("Failure details: {}", errorResponse.getMessage());
        }

        final HttpStatus status = (HttpStatus) response.getStatusCode();
        final String errorDescription = ObjectUtils.isEmpty(errorResponse) ? response.getStatusText() : errorResponse.getMessage();

        switch (status) {
            case NOT_FOUND -> throw HttpNotFoundRequestProblemException.builder()
                    .description(errorDescription)
                    .build();
            case SERVICE_UNAVAILABLE, BAD_REQUEST, CONFLICT -> throw HttpInternalServerErrorException.builder()
                    .description("Unable to complete request to Data Catalog due to error: " + errorDescription)
                    .build();
            default -> {
                log.info("Unmatched error response: {}", response.getStatusCode());
                throw HttpInternalServerErrorException.builder()
                        .description(errorDescription)
                        .build();
            }
        }
    }

}
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

import java.io.IOException;

import com.ericsson.oss.air.aas.service.BaseHttpResponseErrorHandler;
import lombok.AccessLevel;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.ResponseErrorHandler;

/**
 * Implementation of ResponseErrorHandler for ARDQ REST responses
 */
@Slf4j
public class ArdqHttpResponseErrorHandler extends BaseHttpResponseErrorHandler {

    @Setter(AccessLevel.PACKAGE) // Intended only for unit tests
    private ResponseErrorHandler defaultResponseErrorHandler = new DefaultResponseErrorHandler();

    @Override
    public void handleError(final ClientHttpResponse response) throws IOException {
        this.defaultResponseErrorHandler.handleError(response);
    }
}
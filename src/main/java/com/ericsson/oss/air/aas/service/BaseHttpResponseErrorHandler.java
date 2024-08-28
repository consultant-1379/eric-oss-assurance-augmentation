/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.aas.service;

import static com.ericsson.oss.air.util.ExceptionUtils.isRestClientConnectivityException;

import java.io.IOException;
import java.net.URI;
import java.util.Objects;

import com.ericsson.oss.air.util.logging.audit.AuditLogFactory;
import com.ericsson.oss.air.util.logging.audit.AuditLogger;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;

/**
 * Base abstraction of the ResponseErrorHandler for AAS, which will be injected into RestTemplate through RestTemplateBuilder. This implementation handles the HTTP
 * errors returned by remote APIs.
 */
@Slf4j
public abstract class BaseHttpResponseErrorHandler implements ResponseErrorHandler {

    private static final AuditLogger AUDIT_LOGGER = AuditLogFactory.getLogger(BaseHttpResponseErrorHandler.class);

    /**
     * Delegates to HttpStatusCode with the response status code.
     *
     * @param response client http response
     */
    @Override
    public boolean hasError(final ClientHttpResponse response) throws IOException {
        return (response.getStatusCode().is4xxClientError() || response.getStatusCode().is5xxServerError());
    }

    /**
     * Logs an audit log depending on the status code and delegates the error handling to {@link #handleError(ClientHttpResponse)}.
     *
     * @param url      the client request url
     * @param method   the HTTP method used for the client request
     * @param response the response from the client
     * @throws IOException
     */
    @Override
    public void handleError(final URI url, final HttpMethod method, final ClientHttpResponse response) throws IOException {

        final int statusCodeValue = response.getStatusCode().value();
        final HttpStatus status = HttpStatus.resolve(statusCodeValue);

        if (isRestClientConnectivityException(statusCodeValue)) {
            AUDIT_LOGGER.error("{} {}: {} {}", method, url, statusCodeValue, Objects.nonNull(status) ? status.getReasonPhrase() : Strings.EMPTY);
        }

        ResponseErrorHandler.super.handleError(url, method, response);
    }

}
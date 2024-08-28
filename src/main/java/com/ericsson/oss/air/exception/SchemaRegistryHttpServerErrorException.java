/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.exception;

import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;

/**
 * A RestClientException wrapper for sever Error
 */
public class SchemaRegistryHttpServerErrorException extends RestClientException {

    /**
     * Instantiates a new Schema registry http server error exception.
     *
     * @param message   the message
     * @param status    the status
     * @param errorCode the error code
     */
    public SchemaRegistryHttpServerErrorException(final String message, final int status, final int errorCode) {
        super(message, status, errorCode);
    }

}

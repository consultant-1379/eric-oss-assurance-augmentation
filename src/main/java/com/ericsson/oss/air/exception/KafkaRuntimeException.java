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

/**
 * RuntimeException of Kafka
 */
public class KafkaRuntimeException extends RuntimeException {

    /**
     * KafkaRuntimeException constructor with message and cause
     *
     * @param message exception detail message
     */
    public KafkaRuntimeException(final String message) {
        super(message);
    }

    /**
     * KafkaRuntimeException constructor with message and cause
     *
     * @param message exception detail message
     * @param cause   exception
     */
    public KafkaRuntimeException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * KafkaRuntimeException constructor with cause
     *
     * @param cause exception
     */
    public KafkaRuntimeException(final Throwable cause) {
        super(cause);
    }

}

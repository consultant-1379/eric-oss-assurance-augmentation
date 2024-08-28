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
 * RuntimeException to indicate any exception in Aas DAOs.
 */

public class AasDaoException extends RuntimeException {

    /**
     * Creates a AasDaoException with a message.
     *
     * @param message detailed exception message
     */
    public AasDaoException(final String message) {
        super(message);
    }

    /**
     * Returns a new {@code AasDaoException} with the specified cause.
     *
     * @param cause cause of this exception
     */
    public AasDaoException(final Throwable cause) {
        super(cause);
    }

    /**
     * Returns a new {@code AasDaoException} with the specified message and cause.
     *
     * @param message message for this exception
     * @param cause   cause of this exception
     */
    public AasDaoException(final String message, final Throwable cause) {
        super(message, cause);
    }
}

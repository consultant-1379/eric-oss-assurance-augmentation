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

package com.ericsson.oss.air.exception;

/**
 * RuntimeException to indicate an exception during an AAS validation flow.
 */
public class AasValidationException extends Exception {

    /**
     * Creates an AasValidationException with a message.
     *
     * @param message detailed exception message
     */
    public AasValidationException(final String message) {
        super(message);
    }
}

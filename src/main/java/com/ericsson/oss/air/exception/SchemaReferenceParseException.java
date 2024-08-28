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
 * The type Schema reference parse exception.
 */
public class SchemaReferenceParseException extends RuntimeException{

    /**
     * Instantiates a new Schema reference parse exception.
     *
     * @param message the message
     */
    public SchemaReferenceParseException(final String message) {
        super(message);
    }
}

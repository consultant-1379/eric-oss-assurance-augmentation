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
 * The type Schema Namespace parse exception.
 */
public class SchemaNamespaceParseException extends RuntimeException {

    /**
     * Instantiates a new schema namespace parse exception.
     *
     * @param message the message
     */
    public SchemaNamespaceParseException(final String message) {
        super(message);
    }
}

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
 * The type Specification reference parse exception.
 */
public class SpecificationReferenceParseException extends RuntimeException {

    /**
     * Instantiates a new Specification reference parse exception.
     *
     * @param errorMessage the error message
     */
    public SpecificationReferenceParseException(final String errorMessage) {
        super(errorMessage);
    }
}

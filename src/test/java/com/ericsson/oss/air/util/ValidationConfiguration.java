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

package com.ericsson.oss.air.util;

import jakarta.validation.Validation;
import jakarta.validation.Validator;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * Bean configuration class defining the Validator bean used in test cases.
 */
@TestConfiguration
public class ValidationConfiguration {

    /**
     * Returns a bean of type {@code javax.validation.Validator}.
     *
     * @return a bean of type {@code javax.validation.Validator}
     */
    @Bean
    public Validator getValidator() {
        return Validation.buildDefaultValidatorFactory().getValidator();
    }
}

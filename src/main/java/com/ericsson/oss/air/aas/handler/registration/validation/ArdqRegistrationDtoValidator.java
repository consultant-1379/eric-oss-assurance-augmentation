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

package com.ericsson.oss.air.aas.handler.registration.validation;

import java.util.function.Predicate;

import com.ericsson.oss.air.api.generated.model.ArdqRegistrationDto;

/**
 * The interface ARDQ registration DTO validator.
 */
public interface ArdqRegistrationDtoValidator extends Predicate<ArdqRegistrationDto> {

    /**
     * AND operation for ArdqRegistrationDtoValidator.
     *
     * @param other the other
     * @return the ARDQ registration DTO validator
     */
    default ArdqRegistrationDtoValidator and(final ArdqRegistrationDtoValidator other) {
        return t -> Predicate.super.and(other).test(t);
    }

    /**
     * Or operation for ArdqRegistrationDtoValidator.
     *
     * @param other the other
     * @return the ARDQ registration DTO validator
     */
    default ArdqRegistrationDtoValidator or(final ArdqRegistrationDtoValidator other) {
        return t -> {
            try {
                if (this.test(t)) {
                    return true;
                }

            } catch (final RuntimeException e) {
                if (other.test(t)) {
                    return true;
                } else {
                    throw e;
                }
            }
            return other.test(t);
        };
    }

    @Override
    default ArdqRegistrationDtoValidator negate() {
        return t -> {
            try {
                return !(this.test(t));
            } catch (final RuntimeException e) { // NOSONAR
                return true;
            }
        };
    }
}

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

import static org.junit.jupiter.api.Assertions.*;

import com.ericsson.oss.air.api.generated.model.ArdqRegistrationDto;
import org.junit.jupiter.api.Test;

class ArdqRegistrationDtoValidatorTest {

    final ValidatorFail fail = new ValidatorFail();
    final ValidatorSuccess success = new ValidatorSuccess();
    final ValidatorFailWithException exception = new ValidatorFailWithException();
    final ArdqRegistrationDto dto = new ArdqRegistrationDto();

    @Test
    void testAnd() {
        assertTrue(this.success.test(this.dto));
        assertTrue(this.success.and(this.success).test(this.dto));
        assertFalse(this.success.and(this.fail).test(this.dto));
        assertFalse(this.fail.and(this.fail).test(this.dto));
    }

    @Test
    void testOr() {
        assertTrue(this.success.test(this.dto));
        assertTrue(this.success.or(this.fail).test(this.dto));
        assertTrue(this.fail.or(this.success).test(this.dto));

        assertFalse(this.fail.test(this.dto));
        assertFalse(this.fail.or(this.fail).test(this.dto));
    }

    @Test
    void testOr_withException() {
        assertThrows(Exception.class, () -> this.exception.test(this.dto));
        assertThrows(Exception.class, () -> this.fail.or(this.exception).test(this.dto));
        assertThrows(Exception.class, () -> this.exception.or(this.fail).test(this.dto));
        assertDoesNotThrow(()-> this.success.or(this.exception).test(this.dto));
        assertDoesNotThrow(()-> this.exception.or(this.success).test(this.dto));
    }

    @Test
    void testNegate() {
        assertTrue(this.fail.negate().test(this.dto));
        assertTrue(this.exception.negate().test(this.dto));
        assertFalse(this.success.negate().test(this.dto));
    }

    static class ValidatorFail implements ArdqRegistrationDtoValidator {
        @Override
        public boolean test(final ArdqRegistrationDto ardqRegistrationDto) {
            return false;
        }
    }

    static class ValidatorFailWithException implements ArdqRegistrationDtoValidator{
        @Override
        public boolean test(final ArdqRegistrationDto ardqRegistrationDto) {
            throw new RuntimeException();
        }
    }

    static class ValidatorSuccess implements ArdqRegistrationDtoValidator{
        @Override
        public boolean test(final ArdqRegistrationDto ardqRegistrationDto) {
            return true;
        }
    }
}
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

import static org.mockito.ArgumentMatchers.any;

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.ericsson.oss.air.api.generated.model.ArdqRegistrationDto;
import com.ericsson.oss.air.exception.http.problem.exception.HttpBadRequestProblemException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UrlValidatorTest {

    ArdqRegistrationDto ardqRegistrationDto;

    @BeforeEach
    void Setup() {
        this.ardqRegistrationDto = new ArdqRegistrationDto();
        this.ardqRegistrationDto.setArdqUrl("http://www.google.com");
    }

    @Test
    void test_NoException() {
        try (final MockedStatic<InetAddress> urlUtil = Mockito.mockStatic(InetAddress.class)) {
            urlUtil.when(() -> InetAddress.getByName(any(String.class))).thenReturn(null);
            final UrlValidator urlValidator = new UrlValidator();
            Assertions.assertDoesNotThrow(() -> urlValidator.test(this.ardqRegistrationDto));
        }
    }

    @Test
    void test_UnknownHost() {
        try (final MockedStatic<InetAddress> urlUtil = Mockito.mockStatic(InetAddress.class)) {
            urlUtil.when(() -> InetAddress.getByName(any(String.class))).thenThrow(new UnknownHostException());
            final UrlValidator urlValidator = new UrlValidator();
            Assertions.assertThrows(HttpBadRequestProblemException.class, () -> urlValidator.test(this.ardqRegistrationDto));
        }
    }
}
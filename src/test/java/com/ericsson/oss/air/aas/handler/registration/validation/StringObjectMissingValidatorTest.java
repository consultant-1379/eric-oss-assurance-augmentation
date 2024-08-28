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

import java.util.Optional;

import com.ericsson.oss.air.aas.repository.ArdqRegistrationDao;
import com.ericsson.oss.air.api.generated.model.ArdqRegistrationDto;
import com.ericsson.oss.air.exception.http.problem.exception.HttpNotFoundRequestProblemException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class StringObjectMissingValidatorTest {

    @Mock
    private ArdqRegistrationDao ardqRegistrationDao;

    private StringObjectMissingValidator validator;

    @BeforeEach
    public void setup(){
        validator = new StringObjectMissingValidator(this.ardqRegistrationDao);
    }


    @Test
    void test_IDValidatorTest_DtoExists() {
        Mockito.when(this.ardqRegistrationDao.findByArdqId(ArgumentMatchers.any())).thenReturn(Optional.of(new ArdqRegistrationDto()));
        Assertions.assertDoesNotThrow(() -> validator.test("cardq"));
    }

    @Test
    void test_IDValidatorTest_DtoNotExists() {
        Mockito.when(this.ardqRegistrationDao.findByArdqId(ArgumentMatchers.any())).thenReturn(Optional.empty());
        Assertions.assertThrows(HttpNotFoundRequestProblemException.class, () -> validator.test("cardq"));
    }
}

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

package com.ericsson.oss.air.aas.config.metrics;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Optional;

import com.ericsson.oss.air.aas.repository.ArdqRegistrationDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
class GaugeDaoFunctionFactoryTest {

    private GaugeDaoFunctionFactory gaugeDaoFunctionFactory = new GaugeDaoFunctionFactory();

    @Mock
    private ArdqRegistrationDao ardqRegistrationDao;

    private GaugeDaoFunctionFactory.GaugeDaoFunction<ArdqRegistrationDao> gaugeDaoFunction;

    @BeforeEach
    void setUp() {
        this.gaugeDaoFunction = this.gaugeDaoFunctionFactory.createGaugeDaoFunction(() -> this.ardqRegistrationDao.getTotalRegistrations().orElse(0));
    }

    @Test
    void createGaugeDaoFunction_DaoFunctionReturnsValidValue() {

        final Double validValue = 2.0;
        when(this.ardqRegistrationDao.getTotalRegistrations()).thenReturn(Optional.of(validValue.intValue()));

        assertEquals(validValue, this.gaugeDaoFunction.applyAsDouble(this.ardqRegistrationDao));
    }

    @Test
    void createGaugeDaoFunction_DaoFunctionThrowsException_ReturnsInitValue() {

        when(this.ardqRegistrationDao.getTotalRegistrations()).thenThrow(RuntimeException.class);

        assertEquals(0, this.gaugeDaoFunction.applyAsDouble(this.ardqRegistrationDao));
    }

    @Test
    void createGaugeDaoFunction_DaoFunctionReturnsNaN_ReturnsInitValue() {

        final GaugeDaoFunctionFactory.GaugeDaoFunction<ArdqRegistrationDao> function = this.gaugeDaoFunctionFactory.createGaugeDaoFunction(
                () -> Double.NaN);

        assertEquals(0, function.applyAsDouble(this.ardqRegistrationDao));
    }
}

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

import java.util.function.DoubleSupplier;
import java.util.function.ToDoubleFunction;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

/**
 * Factory for creating {@link GaugeDaoFunction}'s, which wrap a DAO method for a gauge that supplies a double value
 * so that it can be initialized to zero. This wrapper prevents NaN from being reported in situations such as when
 * AAS' metrics are being requested by an external system and AAS has not finished setting up its DB.
 */
@Configuration
@Slf4j
public class GaugeDaoFunctionFactory {

    private static final double GAUGE_INIT_VALUE = 0;

    private static final String NO_GAUGE_VALUE_MSG = "Cannot get gauge value";

    /**
     * Creates a specialized {@link ToDoubleFunction<T>} for gauges using a DAO method to get their value.
     * If the DAO method throws an exception or returns NaN, then zero will be returned.
     *
     * @param doubleSupplier the DAO method to wrap
     * @return a specialized {@link ToDoubleFunction<T>} for gauges using a DAO method
     * @param <T> the DAO object type, e.g. PMDefinitionDAO
     */
    public <T> GaugeDaoFunction<T> createGaugeDaoFunction(final DoubleSupplier doubleSupplier) {
        return new GaugeDaoFunction<>(doubleSupplier);
    }

    /*
     * (non-javadoc)
     *
     * Wraps a DAO method for a gauge that supplies a double value so that it can be initialized to a default value.
     */
    record GaugeDaoFunction<T> (DoubleSupplier doubleSupplier) implements ToDoubleFunction<T> {

        @Override
        public double applyAsDouble(final T value) {
            try {

                final double gaugeValue = this.doubleSupplier.getAsDouble();
                return Double.isNaN(gaugeValue) ? GAUGE_INIT_VALUE : gaugeValue;

            } catch (final Exception e) {

                log.debug(NO_GAUGE_VALUE_MSG, e);
                return GAUGE_INIT_VALUE;
            }
        }

    }

}

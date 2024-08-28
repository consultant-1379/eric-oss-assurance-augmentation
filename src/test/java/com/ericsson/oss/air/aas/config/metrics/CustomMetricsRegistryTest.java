/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.aas.config.metrics;

import static com.ericsson.oss.air.aas.config.metrics.CustomMetrics.CUSTOM_METRIC_TAG;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import com.ericsson.oss.air.aas.config.JdbcConfig;
import com.ericsson.oss.air.aas.repository.impl.inmemorydb.ArdqRegistrationDaoImpl;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.commons.util.StringUtils;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest(classes = {CustomMetricsRegistry.class, SimpleMeterRegistry.class, ArdqRegistrationDaoImpl.class, GaugeDaoFunctionFactory.class})
class CustomMetricsRegistryTest {

    @Autowired
    private ApplicationContext applicationContext;


    @Test
    void metricInstantiation_AllMetricsHaveCustomTagAndDescription () {

        final SimpleMeterRegistry registry = this.applicationContext.getBean(SimpleMeterRegistry.class);

        for (final Meter meter : registry.getMeters()) {

            final Meter.Id meterId = meter.getId();
            final String tagValue = meterId.getTag(CUSTOM_METRIC_TAG);

            assertEquals(meterId.getName(), tagValue);
            assertTrue(StringUtils.isNotBlank(meterId.getDescription()));
        }
    }

}
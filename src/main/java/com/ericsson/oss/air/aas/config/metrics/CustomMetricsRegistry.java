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

import static com.ericsson.oss.air.aas.config.metrics.CustomMetrics.CUSTOM_METRIC_TAG;

import com.ericsson.oss.air.aas.repository.ArdqRegistrationDao;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registers custom AAS metrics in the Micrometer MeterRegistry.
 */
@Configuration
@Slf4j
public class CustomMetricsRegistry {

    @Autowired
    private MeterRegistry registry;

    /**
     * Creates the counter for output records.
     *
     * @return the counter for output records.
     */
    @Bean
    public Counter outputRecordsCounter() {
        return this.createCounter(CustomMetrics.AAS_AUGMENTATION_OUTPUT_RECORDS.getValue(), "Count of output records");
    }

    /**
     * Creates the counter for input records to be augmented.
     *
     * @return the counter for input records to be augmented.
     */
    @Bean
    public Counter augmentedInputRecordsCounter() {
        return this.createCounter(CustomMetrics.AAS_AUGMENTATION_INPUT_RECORDS.getValue(), "Count of input records to be augmented");
    }

    /**
     * Creates the counter for input records that do not require augmentation.
     *
     * @return the counter for input records that do not require augmentation.
     */
    @Bean
    public Counter nonAugmentedInputRecordsCounter() {
        return this.createCounter(CustomMetrics.AAS_INPUT_RECORDS.getValue(), "Count of input records that do not require augmentation");
    }

    /**
     * Creates the counter for augmentation processing errors.
     *
     * @return the counter for augmentation processing errors.
     */
    @Bean
    public Counter augmentationErrorsCounter() {
        return this.createCounter(CustomMetrics.AAS_AUGMENTATION_ERRORS.getValue(), "Count of augmentation processing errors");
    }

    /**
     * Creates the counter for successfully deleted ARDQ registrations.
     *
     * @return the counter for successfully deleted ARDQ registrations.
     */
    @Bean
    public Counter deletedArdqRegistrationsCounter() {
        return this.createCounter(CustomMetrics.AAS_DELETED_REGISTRATIONS_COUNT.getValue(), "Count of successfully deleted ARDQ registrations");
    }

    /**
     * Creates the counter for successfully created ARDQ registrations.
     *
     * @return the counter for successfully created ARDQ registrations.
     */
    @Bean
    public Counter createdArdqRegistrationsCounter() {
        return this.createCounter(CustomMetrics.AAS_CREATED_REGISTRATIONS_COUNT.getValue(), "Count of successfully created ARDQ registrations");
    }

    /**
     * Creates the counter for successfully updated ARDQ registrations.
     *
     * @return the counter for successfully updated ARDQ registrations.
     */
    @Bean
    public Counter updatedArdqRegistrationsCounter() {
        return this.createCounter(CustomMetrics.AAS_UPDATED_REGISTRATIONS_COUNT.getValue(), "Count of successfully updated ARDQ registrations");
    }

    /**
     * Creates the counter for valid ARDQ responses during augmentation processing.
     *
     * @return the counter for valid ARDQ responses during augmentation processing.
     */
    @Bean
    public Counter ardqValidResponsesCounter() {
        return this.createCounter(CustomMetrics.ARDQ_VALID_RESPONSES_COUNT.getValue(), "Count of valid ARDQ responses during augmentation processing");
    }


    /**
     * Creates the counter for invalid ARDQ responses during augmentation processing.
     *
     * @return the counter for invalid ARDQ responses during augmentation processing.
     */
    @Bean
    public Counter ardqInvalidResponsesCounter() {
        return this.createCounter(CustomMetrics.ARDQ_INVALID_RESPONSES_COUNT.getValue(), "Count of invalid ARDQ responses during augmentation processing");
    }

    /**
     * Creates the counter for error ARDQ responses during augmentation processing.
     *
     * @return the counter for error ARDQ responses during augmentation processing.
     */
    @Bean
    public Counter ardqErrorResponsesCounter() {
        return this.createCounter(CustomMetrics.ARDQ_ERROR_RESPONSES_COUNT.getValue(), "Count of error ARDQ responses during augmentation processing");
    }


    @Autowired
    void registerRegistrationCount(final ArdqRegistrationDao ardqRegistrationDao, final GaugeDaoFunctionFactory gaugeDaoFunctionFactory) {

        final String gaugeName = CustomMetrics.AAS_REGISTRATIONS_COUNT.getValue();

        Gauge.builder(gaugeName, ardqRegistrationDao,
                        gaugeDaoFunctionFactory.createGaugeDaoFunction(() -> ardqRegistrationDao.getTotalRegistrations().orElse(0)))
                .tags(CUSTOM_METRIC_TAG, gaugeName)
                .description("Total count of ARDQ registrations")
                .register(this.registry);
    }


    /*
     * Utility method to create AAS counters.
     */
    private Counter createCounter(final String counterName, final String description) {

        return Counter.builder(counterName)
                .tags(CUSTOM_METRIC_TAG, counterName)
                .description(description)
                .register(this.registry);
    }


}

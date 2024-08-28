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

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

/**
 * Enum defining the custom AAS Metric Names for augmentation input and output records, errors and registration counts.
 * At runtime, all metric names will be prefixed with the AAS service acronym 'aas'.
 * For example '{@code augmentation_output_records_int_total}' will appear as '{@code  aas_augmentation_output_records_int_total}' in the
 * {@code /actuator/prometheus} output.
 * <p>
 * Custom naming conventions:
 * <ul>
 * <li>All metrics have the units appended to the name</li>
 * <li>For metrics represent a cumulative count, e.g. total number of augmentation output record, word 'total' is appended rather than the units</li>
 * <li>Maximum length of a custom metric name is 50 characters</li>
 * </ul>
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum CustomMetrics {

    // Record Metrics
    AAS_AUGMENTATION_INPUT_RECORDS("augmentation_input_records_int_total"),
    AAS_AUGMENTATION_OUTPUT_RECORDS("augmentation_output_records_int_total"),
    AAS_INPUT_RECORDS("input_records_int_total"),
    AAS_AUGMENTATION_ERRORS("augmentation_errors_int_total"),

    // ARDQ Registration Metrics
    AAS_REGISTRATIONS_COUNT("registrations_int_total"),
    AAS_DELETED_REGISTRATIONS_COUNT("deleted_registrations_int_total"),
    AAS_CREATED_REGISTRATIONS_COUNT("created_registrations_int_total"),
    AAS_UPDATED_REGISTRATIONS_COUNT("updated_registrations_int_total"),

    // ARDQ Response Metrics
    ARDQ_VALID_RESPONSES_COUNT("ardq_valid_responses_int_total"),
    ARDQ_INVALID_RESPONSES_COUNT("ardq_invalid_responses_int_total"),
    ARDQ_ERROR_RESPONSES_COUNT("ardq_error_responses_int_total");


    private final String metricName;

    /**
     * Get the metric name
     */
    public String getValue() {
        return this.metricName;
    }

    static final String CUSTOM_METRIC_TAG = "aas_custom_metric";

}

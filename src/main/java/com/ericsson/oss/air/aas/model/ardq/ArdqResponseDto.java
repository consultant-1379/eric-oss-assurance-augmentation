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

package com.ericsson.oss.air.aas.model.ardq;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

/**
 * ARDQ response object. Contains the fields to be augmented and their values
 */
@Data
@Jacksonized
@Builder
public class ArdqResponseDto {
    /**
     * A nested List of AugmentationField objects that must be augmented to the input.
     */
    @JsonProperty
    @Valid
    private List<@Valid @NotEmpty List<@Valid AugmentationField>> fields;

    @Data
    @Jacksonized
    @Builder
    public static class AugmentationField {
        /**
         * Name of the field to be augmented.
         */
        @JsonProperty
        @NotBlank
        private String name;
        /**
         * Values of the field to be augmented.
         */
        @JsonProperty
        private String value;
    }
}
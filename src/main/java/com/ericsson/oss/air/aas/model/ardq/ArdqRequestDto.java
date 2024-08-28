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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * ARDQ query resource. Informational fields from the record will passed in as the input fields. The desired fields for augmentation will be passed in
 * as the augmentationFields (without values)
 */
@EqualsAndHashCode
@Getter
@NoArgsConstructor(staticName = "newRequest")
@ToString
public class ArdqRequestDto {
    /**
     * Input fields used to generate augmented field values.
     */
    @JsonProperty
    private final Set<QueryField> inputFields = new HashSet<>();

    /**
     * List of desired fields for augmentation.
     */
    @JsonProperty
    private final List<AugmentationFieldRequest> augmentationFields = new ArrayList<>();

    /**
     * The ARDQ query type, ex. core
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String queryType;

    /**
     * Set the queryType.
     *
     * @param queryType the augmentation query type
     * @return the updated request instance
     */
    public ArdqRequestDto setQueryType(final String queryType) {
        this.queryType = queryType;
        return this;
    }

    /**
     * Add a new input field to the augmentation request.
     * <p>
     * Note that no duplication check is performed, therefore calling to this method always add a new element to the request.
     *
     * @param name  name of the input field
     * @param value input field value
     * @return the updated request instance
     */
    public ArdqRequestDto addInputField(final String name, final String value) {
        inputFields.add(QueryField.of(name, value));
        return this;
    }

    /**
     * Add a new output field to the augmentation request.
     * <p>
     * Note that no duplication check is performed, therefore calling to this method always add a new element to the request.
     *
     * @param name name of the output field
     * @return the updated request instance
     */
    public ArdqRequestDto addOutputField(final String name) {
        augmentationFields.add(AugmentationFieldRequest.of(name));
        return this;
    }

    /**
     * Fields to be included in a query to ARDQ service provider.
     */
    @AllArgsConstructor(staticName = "of")
    @EqualsAndHashCode
    @ToString
    public static class QueryField {
        /**
         * Name of an input field.
         */
        @JsonProperty
        private String name;

        /**
         * Value of the input field.
         */
        @JsonProperty
        private String value;
    }

    /**
     * Augmentation fields that are returned from an ARDQ service provider.
     */
    @AllArgsConstructor(staticName = "of")
    @EqualsAndHashCode
    @ToString
    public static class AugmentationFieldRequest {
        /**
         * Name of an augmentation field.
         */
        @JsonProperty
        private String name;
    }
}

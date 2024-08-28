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

package com.ericsson.oss.air.aas.model.datacatalog;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

/**
 * Represents a data encoding type.
 */
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public enum EncodingEnum {

    AVRO("AVRO"),

    JSON("JSON"),

    PROTOBUF("PROTOBUF");

    private final String value;

    /**
     * Gets the string value of the {@code EncodingEnum}.
     *
     * @return the string value of the {@code EncodingEnum}.
     */
    @JsonValue
    public String getValue() {
        return this.value;
    }

    @Override
    public String toString() {
        return String.valueOf(this.value);
    }

    /**
     * Creates a {@code EncodingEnum} from a string.
     *
     * @param stringValue the string to be converted
     * @return a {@code EncodingEnum}
     */
    @JsonCreator
    public static EncodingEnum fromValue(final String stringValue) {

        for (final EncodingEnum encodingEnum : EncodingEnum.values()) {
            if (encodingEnum.getValue().equalsIgnoreCase(stringValue)) {
                return encodingEnum;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + stringValue + "'");
    }
}

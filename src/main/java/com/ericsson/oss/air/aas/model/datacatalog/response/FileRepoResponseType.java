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

package com.ericsson.oss.air.aas.model.datacatalog.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

/**
 * Represents a file transfer/storage solution.
 */
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public enum FileRepoResponseType {

    S3("S3"), SFTP ("SFTP");

    private final String value;

    /**
     * Gets the string value of the {@code FileRepoResponseType}.
     *
     * @return the string value of the {@code FileRepoResponseType}.
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
     * Creates a {@code FileRepoResponseType} from a string.
     *
     * @param stringValue the string to be converted
     * @return a {@code FileRepoResponseType}
     */
    @JsonCreator
    public static FileRepoResponseType fromValue(final String stringValue) {

        for (final FileRepoResponseType fileRepoResponseType : FileRepoResponseType.values()) {
            if (fileRepoResponseType.getValue().equalsIgnoreCase(stringValue)) {
                return fileRepoResponseType;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + stringValue + "'");
    }

}

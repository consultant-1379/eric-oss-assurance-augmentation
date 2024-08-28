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

package com.ericsson.oss.air.aas.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ericsson.oss.air.exception.SchemaReferenceParseException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * Represents the details of input schema reference
 */
@Data
@Slf4j
@Builder(toBuilder = true, setterPrefix = "with")
@AllArgsConstructor
public class SchemaReference {

    private static final Pattern INPUT_SCHEMA_REF_PATTERN = Pattern.compile("^([^|]+)\\|([^|]+)\\|([^|]+)$");

    /**
     * Name of the Data Space
     */
    @NonNull
    private String dataSpace;

    /**
     * Name of the Data Category
     */
    @NonNull
    private String dataCategory;

    /**
     * Name of the Schema
     */
    @NonNull
    private String schemaName;

    @Override
    public String toString() {
        return String.format("%s|%s|%s", this.dataSpace, this.getDataCategory(), this.getSchemaName());
    }

    /**
     * Constructs an InputSchemaReference object by parsing the input schema reference string.
     *
     * @param schemaReference the schema reference string that should follow this pattern "^([^|]+)\|([^|]+)\|([^|]+)$"
     */
    public static SchemaReference parse(@NonNull final String schemaReference) throws SchemaReferenceParseException {

        final String errorMessage = "Invalid schema reference format. Expected format: '<dataSpace>|<dataCategory>|<schemaName>'. "
                + "Actual value: '" + schemaReference + "'.";
        final Matcher matcher = INPUT_SCHEMA_REF_PATTERN.matcher(schemaReference);
        if (!matcher.matches()) {
            log.error(errorMessage);
            throw new SchemaReferenceParseException(errorMessage);
        }

        return new SchemaReference(matcher.group(1), matcher.group(2), matcher.group(3));
    }
}

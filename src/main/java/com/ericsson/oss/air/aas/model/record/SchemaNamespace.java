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

package com.ericsson.oss.air.aas.model.record;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ericsson.oss.air.exception.SchemaNamespaceParseException;
import com.ericsson.oss.air.exception.SchemaReferenceParseException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * Represents the details of SchemaNamespace
 */
@Data
@Slf4j
@Builder(toBuilder = true,
         setterPrefix = "with")
@AllArgsConstructor
public class SchemaNamespace {

    private static final Pattern SCHEMA_NAMESPACE_PATTERN = Pattern.compile("^([^.]+)\\.([^.]+)\\.([^.]+)$");

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
     * Name of the Data Provider
     */
    @NonNull
    private String dataProvider;

    @Override
    public String toString() {
        return String.format("%s.%s.%s", this.dataSpace, this.dataProvider, this.dataCategory);
    }

    /**
     * Constructs a schema namespace object by parsing the schema namespace string.
     *
     * @param schemaNamespace the schema namespace string that should follow this pattern "^([^.]+)\.([^.]+)\.([^.]+)$"
     */
    public static SchemaNamespace parse(final String schemaNamespace) throws SchemaReferenceParseException {

        final String errorMessage = "Invalid schema namespace format. Expected format: '<dataSpace>.<dataProvider>.<dataCategory>'. "
                + "Actual value: '" + schemaNamespace + "'.";

        final Matcher matcher = SCHEMA_NAMESPACE_PATTERN.matcher(schemaNamespace);
        if (!matcher.matches()) {
            log.error(errorMessage);
            throw new SchemaNamespaceParseException(errorMessage);
        }

        return SchemaNamespace.builder()
                .withDataSpace(matcher.group(1))
                .withDataProvider(matcher.group(2))
                .withDataCategory(matcher.group(3))
                .build();

    }

}

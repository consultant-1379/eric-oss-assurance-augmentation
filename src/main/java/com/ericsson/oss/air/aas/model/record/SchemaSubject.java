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

import com.ericsson.oss.air.exception.SchemaReferenceParseException;
import com.ericsson.oss.air.exception.SchemaSubjectParseException;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.Schema;

/**
 * Represents the details of SchemaSubject
 */
@Data
@Slf4j
@Builder(toBuilder = true,
         setterPrefix = "with")
@AllArgsConstructor
public class SchemaSubject {

    private static final Pattern SCHEMA_SUBJECT_PATTERN = Pattern.compile("^([^.]+)\\.([^.]+)\\.([^.]+)\\.([^.]+)$");

    /**
     * Schema name
     */
    @NonNull
    private String schemaName;

    /**
     * Schema namespace
     */
    @NonNull
    @Valid
    private SchemaNamespace schemaNamespace;

    @Override
    public String toString() {
        return String.format("%s.%s", this.schemaNamespace, this.schemaName);
    }

    /**
     * Constructs a schema subject object by parsing the schema subject string.
     *
     * @param schemaSubject the schema subject string that should follow this pattern "^([^.]+)\.([^.]+)\.([^.]+)\.([^.]+)$"
     */
    public static SchemaSubject parse(final String schemaSubject) throws SchemaReferenceParseException {

        final String errorMessage = "Invalid schema subject format. Expected format: '<dataSpace>.<dataProvider>.<dataCategory>.<schemaName>'. "
                + "Actual value: '" + schemaSubject + "'.";

        final Matcher matcher = SCHEMA_SUBJECT_PATTERN.matcher(schemaSubject);
        if (!matcher.matches()) {
            log.error(errorMessage);
            throw new SchemaSubjectParseException(errorMessage);
        }

        return SchemaSubject.builder()
                .withSchemaNamespace(SchemaNamespace.builder()
                        .withDataSpace(matcher.group(1))
                        .withDataProvider(matcher.group(2))
                        .withDataCategory(matcher.group(3))
                        .build())
                .withSchemaName(matcher.group(4))
                .build();

    }

    /**
     * Return a SchemaSubject object from Schema object
     *
     * @param schema the schema
     * @return the schema subject
     */
    public static SchemaSubject parse(final Schema schema) {
        return new SchemaSubject(schema.getName(), SchemaNamespace.parse(schema.getNamespace()));

    }

}

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

import com.ericsson.oss.air.aas.model.record.SchemaSubject;
import com.ericsson.oss.air.exception.SpecificationReferenceParseException;
import lombok.Builder;
import lombok.Data;

/**
 * Model object to represent Specification reference.
 */
@Data
@Builder(toBuilder = true)
public class SpecificationReference {

    private static final Pattern SPECIFICATION_REFERENCE_PATTERN = Pattern.compile("^([^.]+)\\.([^.]+)\\.([^.]+)\\.([^.]+)/(\\d+)$");

    private String dataSpace;

    private String dataProvider;

    private String dataCategory;

    private String schemaName;

    private int schemaVersion;

    /**
     * Parse specification reference.
     *
     * @param specificationReferenceStr the specification reference str
     * @return the specification reference
     */
    public static SpecificationReference parse(final String specificationReferenceStr) {

        final Matcher matcher = SPECIFICATION_REFERENCE_PATTERN.matcher(specificationReferenceStr);

        if (!matcher.matches()) {
            final String errorMessage = "Unable to parse specification reference. Expected format "
                    + "<dataSpace>.<dataProvider>.<dataCategory>.<schemaName>/<version>. "
                    + "Actual value '" + specificationReferenceStr + "'.";
            throw new SpecificationReferenceParseException(errorMessage);
        }

        return SpecificationReference.builder()
                .dataSpace(matcher.group(1))
                .dataProvider(matcher.group(2))
                .dataCategory(matcher.group(3))
                .schemaName(matcher.group(4))
                .schemaVersion(Integer.parseInt(matcher.group(5)))
                .build();

    }

    /**
     * Create schema subject
     *
     * @return schema subject
     */
    public String getSchemaSubject() {
        return this.dataSpace + "." + this.dataProvider + "." + this.dataCategory + "." + this.schemaName;
    }

    public SchemaSubject getSchemaSubjectObject() {
        return SchemaSubject.parse(this.getSchemaSubject());
    }

    /**
     * Return string representation of {@link SpecificationReference}
     *
     * @return string representation of {@link SpecificationReference}
     */
    public String toString() {
        return this.getSchemaSubject() + "/" + this.schemaVersion;
    }

}

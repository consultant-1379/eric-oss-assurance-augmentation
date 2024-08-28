/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.aas.handler.registration.provider;

import com.ericsson.oss.air.util.Digest;
import lombok.AccessLevel;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * This class is responsible for providing PMSC-specific generated names/Ids.  PMSC configuration includes following field that have strict uniqueness,
 * character, and length restrictions:
 *
 * <ul>
 *     <li>Output schema name</li>
 * </ul>
 * <p>
 * This class will generate output schema name that is always constrained to the maximum supported by PM Stats Calculator (55 characters).
 */
@Component
public class OutputSchemaNameProvider {

    private static final String PMSCH = "pmsch";
    private static final String PREFIX = "aas_";
    private static final int MAX_OUTPUT_SCHEMA_NAME_LENGTH = 55;

    @Setter(AccessLevel.PACKAGE) // intended only for unit tests
    @Value("${analytics.target}")
    private String analyticsTarget;

    /**
     * Returns a generated output schema name.  The output schema name is generated using the provided input schema name and ardqId.
     * The format depends on the analytics target and length of the generated output schema name. If the analytics target is not "pmsch" OR the generated output schema name length will be less than the maximum length 55 characters,
     * the output schema name will be in the form
     *
     * <pre>ardqId_inputSchemaName</pre>
     * <p>
     * If analytics target is "pmsch" AND the generated output schema name length will be greater than the maximum, a fixed length hash is returned.
     *
     * @param inputSchemaName Input schema name
     * @param ardqId          ArdqId
     * @return a generated output schema name. The length of the output schema name is constrained to the maximum length of 55 characters.
     **/
    public String generate(final String ardqId, final String inputSchemaName) {

        final String rawName = ardqId + "_" + inputSchemaName;

        if (!this.analyticsTarget.equalsIgnoreCase(PMSCH) || this.isValidLength(rawName)) {
            return rawName;
        }

        final String generatedName = PREFIX + new Digest().getDigestAsHex(rawName);
        return this.trimName(generatedName);

    }

    /*
     * (non-javadoc)
     *
     * Trims the provided schema name to the length specified.
     */
    private String trimName(final String schemaName) {
        return schemaName.length() > MAX_OUTPUT_SCHEMA_NAME_LENGTH ? schemaName.substring(0, MAX_OUTPUT_SCHEMA_NAME_LENGTH) : schemaName;
    }

    private boolean isValidLength(final String rawName) {
        return rawName.length() <= MAX_OUTPUT_SCHEMA_NAME_LENGTH;
    }
}

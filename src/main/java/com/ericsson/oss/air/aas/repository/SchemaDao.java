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

package com.ericsson.oss.air.aas.repository;

import java.util.List;
import java.util.Optional;

import com.ericsson.oss.air.aas.model.IoSchema;
import org.apache.avro.Schema;

/**
 * A DAO interface for schema related operations in repository
 */
public interface SchemaDao {

    /**
     * Save a Schema object
     *
     * @param schemaReference   the schema reference
     * @param schema            the schema
     */
    void saveSchema(final String schemaReference, final Schema schema);

    /**
     * Retrieves the schema by the schema reference
     *
     * @param schemaReference   the reference to the schema
     * @return the schema
     */
    Optional<Schema> getSchema(final String schemaReference);

    /**
     * The total number of schemas in repository
     *
     * @return the number of schemas
     */
    int totalSchemas();

    /**
     * Deletes the schema by schema reference
     *
     * @param schemaReference   the schema reference
     * @return the number of schemas deleted
     */
    void deleteSchema(final String schemaReference);

    /**
     * Get the output schema reference string by the ARDQ registration ID and the input schema reference
     *
     * @param ardqId               the ardq id
     * @param inputSchemaReference the input schema reference string
     * @return the output schema reference string
     */
    Optional<String> getOutputSchemaReference(final String ardqId, final String inputSchemaReference);

    /**
     * Gets a list of output schema reference related to given ardqId
     *
     * @param ardqId the ardq id
     * @return the related output schema reference list
     */
    List<String> getOutputSchemaReferenceList(final String ardqId);

    /**
     * Update io schema mapping with a list of {@code IoSchema}
     *
     * @param ioSchemas a list of {@code IoSchema}
     */
    void updateIOSchemaMapping(final List<IoSchema> ioSchemas);

    /**
     * Gets io schemas by given ardq id
     *
     * @param ardqId the ardq id
     * @return the io schemas
     */
    List<IoSchema> getIOSchemas(final String ardqId);

    /**
     * Gets ids of affected ARDQ registrations
     *
     * @param inputSchemaReference the input schema reference
     * @return ids of affected ARDQ registrations
     */
    List<String> getAffectedArdqRegistrationIds(final String inputSchemaReference);

}

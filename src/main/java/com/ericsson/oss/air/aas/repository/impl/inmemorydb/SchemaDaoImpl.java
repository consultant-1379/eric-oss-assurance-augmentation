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

package com.ericsson.oss.air.aas.repository.impl.inmemorydb;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.ericsson.oss.air.aas.model.IoSchema;
import com.ericsson.oss.air.aas.repository.SchemaDao;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.Schema;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Repository;

/**
 * The implementation of Schema DAO operations in in-memory data store
 */
@Repository
@NoArgsConstructor
@Slf4j
public class SchemaDaoImpl implements SchemaDao {

    private final Map<String, Schema> schemaMap = new HashMap<>();

    private final Map<Pair<String, String>, IoSchema> ioSchemaMap = new HashMap<>();

    @Override
    public void saveSchema(final String schemaReference, final Schema schema) {
        this.schemaMap.put(schemaReference, schema);
    }

    @Override
    public Optional<Schema> getSchema(final String schemaReference) {
        return Optional.ofNullable(schemaMap.get(schemaReference));
    }

    @Override
    public int totalSchemas() {
        return this.schemaMap.size();
    }

    @Override
    public void deleteSchema(final String schemaReference) {
        this.schemaMap.remove(schemaReference);
    }

    @Override
    public Optional<String> getOutputSchemaReference(final String ardqId, final String inputSchemaReference) {

        final IoSchema ioSchema = this.ioSchemaMap.get(Pair.of(ardqId, inputSchemaReference));
        return Optional.ofNullable(ioSchema).map(IoSchema::getOutputSchemaReference);
    }

    @Override
    public List<String> getOutputSchemaReferenceList(final String ardqId) {

        return this.getAllIoSchema()
                .stream()
                .filter(ioSchema -> ioSchema.getArdqRegistrationId().equals(ardqId))
                .map(IoSchema::getOutputSchemaReference)
                .collect(Collectors.toList());
    }

    @Override
    public void updateIOSchemaMapping(final List<IoSchema> ioSchemas) {
        ioSchemas.forEach(ioSchema -> {
            final Pair<String, String> ioSchemaKey = Pair.of(ioSchema.getArdqRegistrationId(), ioSchema.getInputSchemaReference());
            this.ioSchemaMap.put(ioSchemaKey, ioSchema);
        });
    }

    @Override
    public List<IoSchema> getIOSchemas(final String ardqId) {
        return this.getAllIoSchema()
                .stream()
                .filter(ioSchema -> ioSchema.getArdqRegistrationId().equals(ardqId))
                .toList();
    }

    /**
     * Gets all IoSchema collection
     *
     * @return the all IoSchema collection
     */
    Set<IoSchema> getAllIoSchema() {
        return new HashSet<>(this.ioSchemaMap.values());
    }

    @Override
    public List<String> getAffectedArdqRegistrationIds(final String inputSchemaReference) {

        return this.getAllIoSchema()
                .stream()
                .filter(ioSchema -> ioSchema.getInputSchemaReference().equals(inputSchemaReference))
                .map(IoSchema::getArdqRegistrationId)
                .collect(Collectors.toList());
    }

}

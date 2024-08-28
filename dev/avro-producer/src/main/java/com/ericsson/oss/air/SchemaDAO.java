/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.ericsson.oss.air.producer.SchemaRegistryService;
import io.confluent.kafka.schemaregistry.client.SchemaMetadata;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.Schema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

@Component
@NoArgsConstructor
@Slf4j
public class SchemaDAO {

    private final Map<String, Schema> schemaMap = new HashMap<>();
    private final Map<String, SchemaMetadata> metadataMap = new HashMap<>();

    SchemaRegistryService schemaRegistryService;

    @Autowired
    public SchemaDAO(final SchemaRegistryService schemaRegistryService) {
        this.schemaRegistryService = schemaRegistryService;
    }

    public Schema getSchema(final String subject) {

        if (this.schemaMap.containsKey(subject)) {
            return this.schemaMap.get(subject);
        }

        this.updateMap(subject);
        final String schemaStr = this.metadataMap.get(subject).getSchema();
        final Schema schema = new Schema.Parser().parse(schemaStr);
        if (!ObjectUtils.isEmpty(schema)) {
            this.schemaMap.put(subject, schema);
        }

        return schema;
    }

    public Integer getVersion(final String subject) {
        this.updateMap(subject);
        return this.metadataMap.get(subject).getVersion();
    }

    public Integer getSchemaId(final String subject) {
        this.updateMap(subject);
        return this.metadataMap.get(subject).getId();
    }

    private void updateMap(final String subject) {
        if (this.metadataMap.containsKey(subject)) {
            return;
        }

        try {
            log.info("Fetching schemas meta for {}", subject);
            final SchemaMetadata latestSchemaMetadata = this.schemaRegistryService.getSchemaMetadata(subject);
            this.metadataMap.put(subject, latestSchemaMetadata);
        } catch (final RestClientException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}

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

package com.ericsson.oss.air.util;

import com.ericsson.oss.air.aas.model.record.SchemaNamespace;
import com.ericsson.oss.air.aas.model.record.SchemaSubject;

import io.confluent.kafka.schemaregistry.avro.AvroSchema;
import io.confluent.kafka.schemaregistry.avro.AvroSchemaUtils;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import lombok.NoArgsConstructor;

/**
 * a CustomApKafkaAvroSerializer to serialize data correctly with schema registry
 */
@NoArgsConstructor
public class CustomApKafkaAvroSerializer extends KafkaAvroSerializer {

    @Override
    public byte[] serialize(final String topic, final Object recordObject) {
        if (recordObject == null) {
            return new byte[0];
        } else {
            final var schema = AvroSchemaUtils.getSchema(recordObject, this.useSchemaReflection, this.avroReflectionAllowNull,
                                                         this.removeJavaProperties);
            final AvroSchema avroSchema = new AvroSchema(schema);
            final SchemaNamespace schemaNamespace = SchemaNamespace.parse(schema.getNamespace());
            final SchemaSubject schemaSubject = new SchemaSubject(schema.getName(), schemaNamespace);

            return this.serializeImpl(schemaSubject.toString(), recordObject, avroSchema);
        }
    }
}

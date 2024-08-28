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

import java.text.MessageFormat;
import java.util.Map;

import io.confluent.kafka.schemaregistry.avro.AvroSchema;
import io.confluent.kafka.schemaregistry.avro.AvroSchemaUtils;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.serializers.KafkaAvroSerializer;

/**
 * a CustomApKafkaAvroSerializer to serialize data correctly with schema registry
 */
public class CustomApKafkaAvroSerializer extends KafkaAvroSerializer {

    /**
     * Instantiates a new Custom ap kafka avro serializer.
     */
    public CustomApKafkaAvroSerializer() {
    }

    /**
     * Instantiates a new Custom ap kafka avro serializer.
     *
     * @param client the client
     */
    public CustomApKafkaAvroSerializer(final SchemaRegistryClient client) {
        super(client);
    }

    /**
     * Instantiates a new Custom ap kafka avro serializer.
     *
     * @param client the client
     * @param props  the props
     */
    public CustomApKafkaAvroSerializer(final SchemaRegistryClient client, final Map<String, ?> props) {
        super(client, props);
    }

    @Override
    public byte[] serialize(final String topic, final Object recordObject) {
        if (recordObject == null) {
            return new byte[0];
        } else {
            final var schema = AvroSchemaUtils.getSchema(recordObject, this.useSchemaReflection, this.avroReflectionAllowNull,
                    this.removeJavaProperties);
            final var avroSchema = new AvroSchema(schema);
            final var subject = MessageFormat.format("{0}.{1}", schema.getNamespace(), schema.getName());
            return this.serializeImpl(subject, recordObject, avroSchema);
        }
    }
}

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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.confluent.kafka.schemaregistry.avro.AvroSchema;
import io.confluent.kafka.schemaregistry.client.MockSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;

@ExtendWith(MockitoExtension.class)
class CustomApKafkaAvroSerializerTest {

    @Mock
    SchemaRegistryClient schemaRegistryClient = new MockSchemaRegistryClient();

    String avroSchemaString;
    CustomApKafkaAvroSerializer serializer;

    @BeforeEach
    void init() {
        this.avroSchemaString = "{\"type\": \"record\",\"name\": \"AMF_Mobility_NetworkSlice\",\"namespace\": \"5G.CORE.PM_COUNTERS\",\"fields\": "
                + "[{\"name\": \"dnPrefix\",\"type\": [\"null\",\"string\"],\"default\""
                + ": null,\"doc\": \"DN prefix for the sending network node\"},{\"name\": \"nodeFDN\",\"type\": "
                + "\"string\",\"doc\": \"Local DN of the sending network node\"},{\"name\": \"elementType\",\"type\": [\"null\",\"string\"],"
                + "\"default\": null,\"doc\": \"Type of sending network node (e.g. PCC or PCG)\"},{\"name\": \"moFdn\",\"type\": \"string\","
                + "\"doc\": \"DN of the resource being measured\"},{\"name\": \"snssai\",\"type\": [\"null\",\"string\"],\"default\": null,\"doc\": "
                + "\"Unique identifier of the 5G network slice\"},{\"name\": \"snssai_sst\",\"type\": [\"null\",\"string\"],\"default\": null,"
                + "\"doc\": "
                + "\"Service/Slice type\"},{\"name\": \"snssai_sd\",\"type\": [\"null\",\"string\"],\"default\": null,\"doc\": "
                + "\"Slice differentiatior\"},{\"name\": \"ropBeginTime\",\"type\": \"string\",\"doc\": "
                + "\"Collection begin timestamp in UTC format\"},{\"name\": \"ropEndTime\",\"type\": \"string\",\"doc\": "
                + "\"Collection end timestamp in UTC format\"},{\"name\": \"suspect\",\"type\": [\"null\",\"boolean\"],\"default\": null,"
                + "\"doc\": \"Reliability flag for collected data. Default is false (reliable data).\"},{\"name\": \"pmCounters\",\"type\": "
                + "[\"null\","
                + "{\"name\": \"pmMetricsSchema\",\"type\": \"record\",\"fields\": [{\"name\": \"VS_NS_NbrRegisteredSub_5GS\",\"type\": "
                + "[\"null\",\"int\"],\"default\": null,\"doc\": \"Number of AMF subscribers\"}]}],\"default\": null}]}";
        final Map<String, Object> props = new HashMap<>();
        props.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, "mock://localhost");
        props.put(AbstractKafkaSchemaSerDeConfig.AUTO_REGISTER_SCHEMAS, true);
        this.serializer = new CustomApKafkaAvroSerializer();
        this.serializer.configure(props, false);
    }

    @Test
    void serialization_NullObject_pass() {
        final byte[] result = this.serializer.serialize("topic_example", null);
        assertEquals(0, result.length);
    }

    @Test
    void serialization_sampleRecord_success() {
        final var schemaParser = new Schema.Parser();
        final AvroSchema schema = new AvroSchema(schemaParser.parse(this.avroSchemaString));
        final GenericData.Record schemaRecord = new GenericData.Record(schema.rawSchema());
        schemaRecord.put("dnPrefix", "dnPrefix");
        schemaRecord.put("nodeFDN", "nodeFDN");
        schemaRecord.put("elementType", "elementType");
        schemaRecord.put("moFdn", "moFDN");
        schemaRecord.put("snssai", "snssai");
        schemaRecord.put("ropBeginTime", "ropBeginTime");
        schemaRecord.put("ropEndTime", "ropEndTime");
        schemaRecord.put("suspect", false);
        final byte[] result = this.serializer.serialize("topic_example", schemaRecord);
        assertEquals(79, result.length);
    }
}
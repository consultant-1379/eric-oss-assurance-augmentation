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

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Objects;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.junit.jupiter.api.Test;

public class CustomAvroSerializerTest {

    private CustomAvroSerializer<GenericRecord> serializer;

    @Test
    public void test_serialize() {
        serializer = new CustomAvroSerializer<>();
        GenericRecord avroRecord = createAvroRecord();
        byte[] bytes = serializer.serialize("test", avroRecord);
        assertTrue(Objects.nonNull(bytes));
    }

    @Test
    public void test_serializeWithNullData() {
        serializer = new CustomAvroSerializer<>();
        assertNull(serializer.serialize("test", null));
    }

    private GenericRecord createAvroRecord() {
        String userSchema = "{\"namespace\": \"example.avro\", \"type\": \"record\", " +
                "\"name\": \"User\"," +
                "\"fields\": [{\"name\": \"name\", \"type\": \"string\"}]}";
        Schema.Parser parser = new Schema.Parser();
        Schema schema = parser.parse(userSchema);
        GenericRecord avroRecord = new GenericData.Record(schema);
        avroRecord.put("name", "testUser");
        return avroRecord;
    }
}

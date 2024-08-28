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

import static com.ericsson.oss.air.aas.handler.RegistrationTestUtil.INPUT_SCHEMA;
import static com.ericsson.oss.air.aas.handler.RegistrationTestUtil.INPUT_SCHEMA_NAME;
import static com.ericsson.oss.air.aas.handler.RegistrationTestUtil.SCHEMA_NAMESPACE;
import static com.ericsson.oss.air.aas.handler.RegistrationTestUtil.SCHEMA_SUBJECT;

import com.ericsson.oss.air.exception.SchemaNamespaceParseException;
import com.ericsson.oss.air.exception.SchemaSubjectParseException;
import io.confluent.kafka.schemaregistry.avro.AvroSchema;
import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SchemaSubjectTest {

    private SchemaNamespace schemaNamespace;

    private SchemaSubject schemaSubject;

    @BeforeEach
    void setUp() {
        this.schemaNamespace = SchemaNamespace.builder()
                .withDataSpace("5G")
                .withDataProvider("CORE")
                .withDataCategory("PM_COUNTERS")
                .build();

        this.schemaSubject = SchemaSubject.builder()
                .withSchemaNamespace(this.schemaNamespace)
                .withSchemaName(INPUT_SCHEMA_NAME)
                .build();
    }

    @AfterEach
    void tearDown() {
        this.schemaNamespace = null;
        this.schemaSubject = null;
    }

    @Test
    void test_valid_schemaSubject() {
        final SchemaSubject schemaSubject = SchemaSubject.parse(SCHEMA_SUBJECT);
        Assertions.assertNotNull(schemaSubject);
        Assertions.assertEquals("5G", schemaSubject.getSchemaNamespace().getDataSpace());
        Assertions.assertEquals("PM_COUNTERS", schemaSubject.getSchemaNamespace().getDataCategory());
        Assertions.assertEquals("CORE", schemaSubject.getSchemaNamespace().getDataProvider());
        Assertions.assertEquals("AMF_Mobility_NetworkSlice_1", schemaSubject.getSchemaName());
    }

    @Test
    void test_invalid_schemaSubject() {
        Assertions.assertThrows(SchemaSubjectParseException.class, () -> {
            SchemaSubject.parse("invalidSchemaSubject");
        });
    }

    @Test
    void testSetSchemaNamespace() {
        final SchemaNamespace schemaNamespace = SchemaNamespace.builder()
                .withDataSpace("4G")
                .withDataProvider("RAN")
                .withDataCategory("PM_COUNTERS")
                .build();
        this.schemaSubject.setSchemaNamespace(schemaNamespace);
        Assertions.assertEquals(schemaNamespace, this.schemaSubject.getSchemaNamespace());
    }

    @Test
    void testSetSchemaName() {
        this.schemaSubject.setSchemaName("newSchemaName");
        Assertions.assertEquals("newSchemaName", this.schemaSubject.getSchemaName());
    }

    @Test
    void testToString() {
        Assertions.assertEquals(SCHEMA_SUBJECT, this.schemaSubject.toString());
    }

    @Test
    void test_valid_schema() {
        final AvroSchema avroSchema = new AvroSchema(INPUT_SCHEMA);
        final SchemaSubject schemaSubject = SchemaSubject.parse(avroSchema.rawSchema());
        Assertions.assertEquals("schema_from_DMM", schemaSubject.getSchemaName());
        Assertions.assertEquals(SCHEMA_NAMESPACE, schemaSubject.getSchemaNamespace().toString());
    }

    @Test
    void test_invalid_schema() {
        final Schema schema =
                SchemaBuilder.record("input_schema").namespace("test").fields()
                        .name("existing_field_1").type().stringType().noDefault()
                        .endRecord();

        Assertions.assertThrows(SchemaNamespaceParseException.class, () -> SchemaSubject.parse(schema));
    }

}

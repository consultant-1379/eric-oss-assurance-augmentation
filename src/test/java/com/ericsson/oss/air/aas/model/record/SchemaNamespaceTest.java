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

import static com.ericsson.oss.air.aas.handler.RegistrationTestUtil.SCHEMA_NAMESPACE;

import com.ericsson.oss.air.exception.SchemaNamespaceParseException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SchemaNamespaceTest {

    private SchemaNamespace schemaNamespace;

    @BeforeEach
    void setUp() {
        this.schemaNamespace = SchemaNamespace.builder()
                .withDataSpace("5G")
                .withDataProvider("CORE")
                .withDataCategory("PM_COUNTERS")
                .build();
    }

    @AfterEach
    void tearDown() {
        this.schemaNamespace = null;
    }

    @Test
    void test_valid_schemaNamespace() {
        final SchemaNamespace schemaNamespace = SchemaNamespace.parse(SCHEMA_NAMESPACE);
        Assertions.assertNotNull(schemaNamespace);
        Assertions.assertEquals("5G", schemaNamespace.getDataSpace());
        Assertions.assertEquals("PM_COUNTERS", schemaNamespace.getDataCategory());
        Assertions.assertEquals("CORE", schemaNamespace.getDataProvider());
    }

    @Test
    void test_invalid_schemaNamespace() {
        Assertions.assertThrows(SchemaNamespaceParseException.class, () -> SchemaNamespace.parse("invalidSchemaNamespace"));
    }

    @Test
    void testSetDataSpace() {
        this.schemaNamespace.setDataSpace("newDataSpace");
        Assertions.assertEquals("newDataSpace", this.schemaNamespace.getDataSpace());
    }

    @Test
    void testSetDataCategory() {
        this.schemaNamespace.setDataCategory("newDataCategory");
        Assertions.assertEquals("newDataCategory", this.schemaNamespace.getDataCategory());
    }

    @Test
    void testSetSchemaName() {
        this.schemaNamespace.setDataProvider("newDataProvider");
        Assertions.assertEquals("newDataProvider", this.schemaNamespace.getDataProvider());
    }

    @Test
    void testToString() {
        Assertions.assertEquals(SCHEMA_NAMESPACE, this.schemaNamespace.toString());
    }

}

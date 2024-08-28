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

package com.ericsson.oss.air.aas.model;

import static com.ericsson.oss.air.aas.handler.RegistrationTestUtil.INPUT_SCHEMA_NAME;
import static com.ericsson.oss.air.aas.handler.RegistrationTestUtil.INPUT_SCHEMA_REFERENCE;

import com.ericsson.oss.air.exception.AasValidationException;
import com.ericsson.oss.air.exception.SchemaReferenceParseException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SchemaReferenceTest {

    SchemaReference schemaReference;

    @BeforeEach
    void setUp() {
        this.schemaReference = new SchemaReference("5G", "PM_COUNTERS", INPUT_SCHEMA_NAME);
    }

    @AfterEach
    void tearDown() {
        this.schemaReference = null;
    }

    @Test
    void test_valid_inputSchemaReference() throws AasValidationException {
        final SchemaReference schemaReference = SchemaReference.parse(INPUT_SCHEMA_REFERENCE);
        Assertions.assertNotNull(schemaReference);
        Assertions.assertEquals("5G", schemaReference.getDataSpace());
        Assertions.assertEquals("PM_COUNTERS", schemaReference.getDataCategory());
        Assertions.assertEquals(INPUT_SCHEMA_NAME, schemaReference.getSchemaName());
    }

    @Test
    void test_invalid_inputSchemaReference() {
        Assertions.assertThrows(SchemaReferenceParseException.class, () -> {
            SchemaReference.parse("invalidInputSchemaRef");
        });
    }

    @Test
    void testSetDataSpace() {
        schemaReference.setDataSpace("newDataSpace");
        Assertions.assertEquals("newDataSpace", schemaReference.getDataSpace());
    }

    @Test
    void testSetDataCategory() {
        schemaReference.setDataCategory("newDataCategory");
        Assertions.assertEquals("newDataCategory", schemaReference.getDataCategory());
    }

    @Test
    void testSetSchemaName() {
        schemaReference.setSchemaName("newSchemaName");
        Assertions.assertEquals("newSchemaName", schemaReference.getSchemaName());
    }

    @Test
    void testToString() {
        Assertions.assertEquals(INPUT_SCHEMA_REFERENCE, schemaReference.toString());
    }
}

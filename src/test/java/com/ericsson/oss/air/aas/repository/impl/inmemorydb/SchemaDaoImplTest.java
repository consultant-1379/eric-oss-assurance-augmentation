/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.aas.repository.impl.inmemorydb;

import static com.ericsson.oss.air.aas.handler.RegistrationTestUtil.ARDQ_ID;
import static com.ericsson.oss.air.aas.handler.RegistrationTestUtil.INPUT_SCHEMA_REFERENCE;
import static com.ericsson.oss.air.aas.handler.RegistrationTestUtil.OUTPUT_SCHEMA_NAME;
import static com.ericsson.oss.air.aas.handler.RegistrationTestUtil.OUTPUT_SCHEMA_REFERENCE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import com.ericsson.oss.air.aas.model.IoSchema;
import com.ericsson.oss.air.api.generated.model.ArdqAugmentationFieldDto;
import org.apache.avro.Schema;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SchemaDaoImplTest {

    private SchemaDaoImpl schemaDAOImpl;


    private static Schema getDummyOutputSchema() {
        return Schema.createRecord(OUTPUT_SCHEMA_NAME, "test", "aas", false);
    }


    private static ArdqAugmentationFieldDto getDummyArdqAugmentationField() {
        return new ArdqAugmentationFieldDto().output("nsi").addInputItem("snssai").addInputItem("moFDN");
    }

    @BeforeEach
    void setUp() {
        this.schemaDAOImpl = new SchemaDaoImpl();

        final IoSchema ioSchema1 = new IoSchema(ARDQ_ID, INPUT_SCHEMA_REFERENCE + "_1", OUTPUT_SCHEMA_REFERENCE + "_1");
        final IoSchema ioSchema2 = new IoSchema(ARDQ_ID, INPUT_SCHEMA_REFERENCE + "_2", OUTPUT_SCHEMA_REFERENCE + "_2");
        final IoSchema ioSchema3 = new IoSchema(ARDQ_ID, INPUT_SCHEMA_REFERENCE + "_3", OUTPUT_SCHEMA_REFERENCE + "_3");
        final IoSchema ioSchema4 = new IoSchema(ARDQ_ID + "_2", INPUT_SCHEMA_REFERENCE + "_1", OUTPUT_SCHEMA_REFERENCE + "_2_3");

        this.schemaDAOImpl.updateIOSchemaMapping(List.of(ioSchema1, ioSchema2, ioSchema3));
        this.schemaDAOImpl.updateIOSchemaMapping(List.of(ioSchema4));

    }

    @AfterEach
    void tearDown() {
        this.schemaDAOImpl = null;
    }

    @Test
    void saveSchema() {
        this.schemaDAOImpl.saveSchema(INPUT_SCHEMA_REFERENCE, getDummyOutputSchema());

        assertEquals(Optional.of(getDummyOutputSchema()), this.schemaDAOImpl.getSchema(INPUT_SCHEMA_REFERENCE));
    }

    @Test
    void saveSchema_retrieveOutputSchema() {
        this.schemaDAOImpl.saveSchema(INPUT_SCHEMA_REFERENCE, getDummyOutputSchema());

        assertEquals(Optional.of(getDummyOutputSchema()), this.schemaDAOImpl.getSchema(INPUT_SCHEMA_REFERENCE));
    }

    @Test
    void saveSchema_nonExistentSchemaReference_outputSchemaNotFound() {
        this.schemaDAOImpl.saveSchema(INPUT_SCHEMA_REFERENCE, getDummyOutputSchema());

        assertEquals(Optional.empty(), this.schemaDAOImpl.getSchema("incorrect-reference"));
    }

    @Test
    void totalSchemas() {
        assertEquals(0, this.schemaDAOImpl.totalSchemas());
    }

    @Test
    void deleteSchema() {
        this.schemaDAOImpl.saveSchema(INPUT_SCHEMA_REFERENCE, getDummyOutputSchema());
        assertEquals(1, this.schemaDAOImpl.totalSchemas());

        this.schemaDAOImpl.deleteSchema(INPUT_SCHEMA_REFERENCE);
        assertEquals(0, this.schemaDAOImpl.totalSchemas());
    }

    @Test
    void getOutputSchemaReference_valid_inputSchemaReference() {
        final Optional<String> outputSchemaReference = this.schemaDAOImpl.getOutputSchemaReference(ARDQ_ID, INPUT_SCHEMA_REFERENCE);
        assertFalse(outputSchemaReference.isPresent());

        final Optional<String> outputSchemaReference1 = this.schemaDAOImpl.getOutputSchemaReference(ARDQ_ID, INPUT_SCHEMA_REFERENCE + "_1");
        assertTrue(outputSchemaReference1.isPresent());
        assertEquals(OUTPUT_SCHEMA_REFERENCE + "_1", outputSchemaReference1.get());
    }

    @Test
    void getRelatedOutputSchemaReferenceList() {

        assertEquals(3, this.schemaDAOImpl.getOutputSchemaReferenceList(ARDQ_ID).size());

        assertEquals(1, this.schemaDAOImpl.getOutputSchemaReferenceList(ARDQ_ID + "_2").size());
    }

    @Test
    void updateIOSchemaMapping_valid() {

        this.schemaDAOImpl.updateIOSchemaMapping(List.of(new IoSchema(ARDQ_ID, INPUT_SCHEMA_REFERENCE + "_1", OUTPUT_SCHEMA_REFERENCE)));
        assertEquals(4, this.schemaDAOImpl.getAllIoSchema().size());

        this.schemaDAOImpl.updateIOSchemaMapping(List.of(new IoSchema("ARDQ_ID2", INPUT_SCHEMA_REFERENCE, OUTPUT_SCHEMA_REFERENCE)));
        assertEquals(5, this.schemaDAOImpl.getAllIoSchema().size());

    }

    @Test
    void getIOSchemas_valid() {

        final List<IoSchema> ioSchemas = this.schemaDAOImpl.getIOSchemas(ARDQ_ID);
        assertEquals(3, ioSchemas.size());

    }

    @Test
    void getAffectedArdqRegistrationIds() {
        assertEquals(2, this.schemaDAOImpl.getAffectedArdqRegistrationIds(INPUT_SCHEMA_REFERENCE + "_1").size());
    }
}

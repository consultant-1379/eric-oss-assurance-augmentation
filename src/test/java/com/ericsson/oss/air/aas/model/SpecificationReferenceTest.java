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

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.ericsson.oss.air.exception.SpecificationReferenceParseException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SpecificationReferenceTest {

    @Test
    void getSchemaSubjectTest() {
        final SpecificationReference specificationReference = SpecificationReference.builder()
                .dataSpace("dataSpace")
                .dataProvider("dataProvider")
                .dataCategory("dataCategory")
                .schemaName("schemaName")
                .build();

        assertEquals("dataSpace.dataProvider.dataCategory.schemaName", specificationReference.getSchemaSubject());
    }

    @Test
    void getSchemaVersionTest() {
        final SpecificationReference specificationReference = SpecificationReference.builder()
                .dataSpace("dataSpace")
                .dataProvider("dataProvider")
                .dataCategory("dataCategory")
                .schemaName("schemaName")
                .build();

        specificationReference.setSchemaVersion(1);

        assertEquals("dataSpace.dataProvider.dataCategory.schemaName/1", specificationReference.toString());

    }

    @Test
    void parseTest() {
        final SpecificationReference specificationReference = SpecificationReference.parse("dataSpace.dataProvider.dataCategory.schemaName/1");
        assertEquals("dataSpace.dataProvider.dataCategory.schemaName/1", specificationReference.toString());
    }

    @Test
    void parseTest_invalid_specificationReference() {
        Assertions.assertThrows(SpecificationReferenceParseException.class, () -> {
            SpecificationReference.parse("invalidData");
        });
    }
}

/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.aas.repository.impl.jdbc.mapper;

import static com.ericsson.oss.air.aas.handler.RegistrationTestUtil.ARDQ_ID;
import static com.ericsson.oss.air.aas.handler.RegistrationTestUtil.INPUT_SCHEMA_REFERENCE;
import static com.ericsson.oss.air.aas.handler.RegistrationTestUtil.OUTPUT_SCHEMA_REFERENCE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.ericsson.oss.air.aas.model.IoSchema;
import com.ericsson.oss.air.aas.repository.impl.jdbc.SchemaDaoJdbcImpl;
import lombok.SneakyThrows;
import org.h2.tools.SimpleResultSet;
import org.junit.jupiter.api.Test;

class IoSchemaMapperTest {

    @Test
    @SneakyThrows
    void mapRow() {

        final IoSchemaMapper mapper = new IoSchemaMapper();

        final SimpleResultSet resultSet = new SimpleResultSet();
        resultSet.addColumn(SchemaDaoJdbcImpl.COLUMN_ARDQ_ID, 0, 0, 0);
        resultSet.addColumn(SchemaDaoJdbcImpl.COLUMN_INPUT_SCHEMA_REF, 0, 0, 0);
        resultSet.addColumn(SchemaDaoJdbcImpl.COLUMN_OUTPUT_SCHEMA_REF, 0, 0, 0);

        resultSet.addRow(ARDQ_ID, INPUT_SCHEMA_REFERENCE, OUTPUT_SCHEMA_REFERENCE);
        resultSet.next();

        final IoSchema ioSchema = mapper.mapRow(resultSet, 0);

        assertNotNull(ioSchema);
        assertEquals(new IoSchema(ARDQ_ID, INPUT_SCHEMA_REFERENCE, OUTPUT_SCHEMA_REFERENCE), ioSchema);

    }
}
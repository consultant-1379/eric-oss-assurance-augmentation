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

import static com.ericsson.oss.air.aas.repository.impl.jdbc.SchemaDaoJdbcImpl.COLUMN_ARDQ_ID;
import static com.ericsson.oss.air.aas.repository.impl.jdbc.SchemaDaoJdbcImpl.COLUMN_INPUT_SCHEMA_REF;
import static com.ericsson.oss.air.aas.repository.impl.jdbc.SchemaDaoJdbcImpl.COLUMN_OUTPUT_SCHEMA_REF;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.ericsson.oss.air.aas.model.IoSchema;
import org.springframework.jdbc.core.RowMapper;

/**
 * The type Io schema mapper.
 */
public class IoSchemaMapper implements RowMapper<IoSchema>  {

    @Override
    public IoSchema mapRow(final ResultSet rs, final int rowNum) throws SQLException {

        final String ardqId = rs.getString(COLUMN_ARDQ_ID);
        final String inputSchemaReference = rs.getString(COLUMN_INPUT_SCHEMA_REF);
        final String outputSchemaReference = rs.getString(COLUMN_OUTPUT_SCHEMA_REF);

        return new IoSchema(ardqId, inputSchemaReference, outputSchemaReference);
    }

}

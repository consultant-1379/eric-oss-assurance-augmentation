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

package com.ericsson.oss.air.aas.repository.impl.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import com.ericsson.oss.air.aas.config.JdbcConfig;
import com.ericsson.oss.air.aas.model.IoSchema;
import com.ericsson.oss.air.aas.repository.SchemaDao;
import com.ericsson.oss.air.aas.repository.impl.jdbc.mapper.IoSchemaMapper;
import io.github.resilience4j.retry.annotation.Retry;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.Schema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.ObjectUtils;

/**
 * SchemaDao jdbc implementation to store for sharable resources.
 */
@Repository
@Slf4j
@Primary
@AllArgsConstructor
@ConditionalOnBean(JdbcConfig.class)
public class SchemaDaoJdbcImpl implements SchemaDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public static final String TABLE_SCHEMA = "schema";

    public static final String TABLE_IO_SCHEMA = "io_schema";

    public static final String COLUMN_ARDQ_ID = "ardq_id";

    public static final String COLUMN_INPUT_SCHEMA_REF = "input_schema_ref";

    public static final String COLUMN_OUTPUT_SCHEMA_REF = "output_schema_ref";

    public static final String COLUMN_FIELD_FIELD_SPEC = "field_spec";

    public static final String INSERT_IO_SCHEMA = "INSERT INTO " + TABLE_IO_SCHEMA
            + " (ardq_id, input_schema_ref, output_schema_ref)"
            + " VALUES (?, ?, ?)"
            + " ON CONFLICT (ardq_id, input_schema_ref, output_schema_ref) DO NOTHING";

    public static final String GET_IO_SCHEMA_REFERENCE_BY_ARDQ_ID = "SELECT ardq_id, input_schema_ref, output_schema_ref"
            + " FROM " + TABLE_IO_SCHEMA
            + " WHERE ardq_id = ?";

    public static final String GET_OUTPUT_SCHEMA_REFERENCE = "SELECT output_schema_ref"
            + " FROM " + TABLE_IO_SCHEMA
            + " WHERE ardq_id = ?"
            + " AND input_schema_ref = ?";

    public static final String GET_OUTPUT_SCHEMA_REFERENCE_BY_ARDQ_ID = "SELECT output_schema_ref"
            + " FROM " + TABLE_IO_SCHEMA
            + " WHERE ardq_id = ?";

    public static final String GET_AFFECTED_ARDQ = "SELECT DISTINCT ardq_id"
            + " FROM " + TABLE_IO_SCHEMA
            + " WHERE input_schema_ref = ?";

    public static final String INSERT_SCHEMA = "INSERT INTO " + TABLE_SCHEMA
            + " (reference, name, schema)"
            + " VALUES (?, ?, to_json(?::json))"
            + " ON CONFLICT (reference) DO UPDATE"
            + " SET name = EXCLUDED.name,"
            + " schema = EXCLUDED.schema";

    public static final String GET_SCHEMA = "SELECT schema"
            + " FROM " + TABLE_SCHEMA
            + " WHERE reference = ?";

    public static final String COUNT_SCHEMAS = "SELECT COUNT(*)"
            + " FROM " + TABLE_SCHEMA
            + " WHERE schema IS NOT NULL";

    public static final String DELETE_SCHEMA = "DELETE FROM " + TABLE_SCHEMA
            + " WHERE reference = ?";

    @PostConstruct
    private void postConstruct() {
        log.info("SchemaDao bean is created with JDBC support");
    }

    @Override
    @Retry(name = "jdbc")
    public void saveSchema(final String schemaReference, final Schema schema) {
        this.jdbcTemplate.update(INSERT_SCHEMA, schemaReference, schema.getName(), schema.toString());
    }

    @Override
    @Retry(name = "jdbc")
    public Optional<Schema> getSchema(final String schemaReference) {
        final List<String> outputSchemaString = this.jdbcTemplate.queryForList(GET_SCHEMA, String.class, schemaReference);

        if (ObjectUtils.isEmpty(outputSchemaString)) {
            return Optional.empty();
        }

        final Schema.Parser schemaParser = new Schema.Parser();
        final Schema outputSchema = schemaParser.parse(outputSchemaString.get(0));

        return Optional.of(outputSchema);
    }

    @Override
    @Retry(name = "jdbc")
    public int totalSchemas() {
        return this.jdbcTemplate.queryForObject(COUNT_SCHEMAS, Integer.class);
    }

    @Override
    @Retry(name = "jdbc")
    public void deleteSchema(final String schemaReference) {

        if (ObjectUtils.isEmpty(schemaReference)) {
            return;
        }

        this.jdbcTemplate.update(DELETE_SCHEMA, schemaReference);
    }

    @Override
    @Retry(name = "jdbc")
    public Optional<String> getOutputSchemaReference(final String ardqId, final String inputSchemaReference) {

        try {
            final String outputSchemaReference = this.jdbcTemplate.queryForObject(GET_OUTPUT_SCHEMA_REFERENCE, String.class, ardqId,
                    inputSchemaReference);
            return Optional.ofNullable(outputSchemaReference);
        } catch (final EmptyResultDataAccessException e) {
            log.debug("No matched output schema reference ", e);
            return Optional.empty();
        }
    }

    @Override
    @Retry(name = "jdbc")
    public List<String> getOutputSchemaReferenceList(final String ardqId) {

        return this.jdbcTemplate.queryForList(GET_OUTPUT_SCHEMA_REFERENCE_BY_ARDQ_ID, String.class, ardqId);
    }

    @Override
    @Retry(name = "jdbc")
    public void updateIOSchemaMapping(final List<IoSchema> ioSchemas) {

        final BatchPreparedStatementSetter batchPreparedStatementSetter = new BatchPreparedStatementSetter() {
            @Override
            public void setValues(final PreparedStatement ps, final int i) throws SQLException {
                ps.setString(1, ioSchemas.get(i).getArdqRegistrationId());
                ps.setString(2, ioSchemas.get(i).getInputSchemaReference());
                ps.setString(3, ioSchemas.get(i).getOutputSchemaReference());
            }

            @Override
            public int getBatchSize() {
                return ioSchemas.size();
            }
        };
        this.jdbcTemplate.batchUpdate(INSERT_IO_SCHEMA, batchPreparedStatementSetter);
    }

    @Override
    public List<IoSchema> getIOSchemas(final String ardqId) {

        return this.jdbcTemplate.query(GET_IO_SCHEMA_REFERENCE_BY_ARDQ_ID, new IoSchemaMapper(), ardqId);

    }

    @Override
    @Retry(name = "jdbc")
    public List<String> getAffectedArdqRegistrationIds(final String inputSchemaReference) {

        return this.jdbcTemplate.queryForList(GET_AFFECTED_ARDQ, String.class, inputSchemaReference);
    }

}

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

import static com.ericsson.oss.air.aas.handler.RegistrationTestUtil.ARDQ_ID;
import static com.ericsson.oss.air.aas.handler.RegistrationTestUtil.INPUT_SCHEMA_REFERENCE;
import static com.ericsson.oss.air.aas.handler.RegistrationTestUtil.OUTPUT_SCHEMA;
import static com.ericsson.oss.air.aas.handler.RegistrationTestUtil.OUTPUT_SCHEMA_NAME;
import static com.ericsson.oss.air.aas.handler.RegistrationTestUtil.OUTPUT_SCHEMA_REFERENCE;
import static com.ericsson.oss.air.aas.handler.RegistrationTestUtil.OUTPUT_SCHEMA_STRING;
import static com.ericsson.oss.air.aas.repository.impl.jdbc.SchemaDaoJdbcImpl.COUNT_SCHEMAS;
import static com.ericsson.oss.air.aas.repository.impl.jdbc.SchemaDaoJdbcImpl.GET_OUTPUT_SCHEMA_REFERENCE_BY_ARDQ_ID;
import static com.ericsson.oss.air.aas.repository.impl.jdbc.SchemaDaoJdbcImpl.GET_SCHEMA;
import static com.ericsson.oss.air.aas.repository.impl.jdbc.SchemaDaoJdbcImpl.INSERT_IO_SCHEMA;
import static com.ericsson.oss.air.aas.repository.impl.jdbc.SchemaDaoJdbcImpl.INSERT_SCHEMA;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.same;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

@ExtendWith(MockitoExtension.class)
class SchemaDaoJdbcImplTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    private SchemaDaoJdbcImpl schemaDaoJdbc;

    @BeforeEach
    void setup() {
        this.schemaDaoJdbc = new SchemaDaoJdbcImpl(this.jdbcTemplate);
    }


    @Test
    void saveOutputSchema() {
        this.schemaDaoJdbc.saveSchema(INPUT_SCHEMA_REFERENCE, OUTPUT_SCHEMA);

        verify(this.jdbcTemplate, times(1)).update(eq(INSERT_SCHEMA), eq(INPUT_SCHEMA_REFERENCE), eq(OUTPUT_SCHEMA_NAME), anyString());
    }

    @Test
    void getOutputSchema() {
        when(this.jdbcTemplate.queryForList(eq(GET_SCHEMA), same(String.class), eq(OUTPUT_SCHEMA_REFERENCE)))
                .thenReturn(List.of(OUTPUT_SCHEMA_STRING));

        assertEquals(Optional.of(OUTPUT_SCHEMA), this.schemaDaoJdbc.getSchema(OUTPUT_SCHEMA_REFERENCE));
    }

    @Test
    void getOutputSchema_returnNullOrEmpty() {
        when(this.jdbcTemplate.queryForList(eq(GET_SCHEMA), same(String.class), eq(OUTPUT_SCHEMA_REFERENCE)))
                .thenReturn(null);

        assertEquals(Optional.empty(), this.schemaDaoJdbc.getSchema(OUTPUT_SCHEMA_REFERENCE));

        when(this.jdbcTemplate.queryForList(eq(GET_SCHEMA), same(String.class), eq(OUTPUT_SCHEMA_REFERENCE)))
                .thenReturn(List.of());

        assertEquals(Optional.empty(), this.schemaDaoJdbc.getSchema(OUTPUT_SCHEMA_REFERENCE));

    }

    @Test
    void totalSchemas() {
        when(this.jdbcTemplate.queryForObject(COUNT_SCHEMAS, Integer.class)).thenReturn(10);
        assertEquals(10, this.schemaDaoJdbc.totalSchemas());
    }


    @Test
    void deleteSchema() {
        this.schemaDaoJdbc.deleteSchema("first-id");

        verify(this.jdbcTemplate, times(1)).update(anyString(), any(String.class));
    }

    @Test
    void deleteSchema_null_schemaReference() {
        this.schemaDaoJdbc.deleteSchema(null);

        verify(this.jdbcTemplate, never()).update(anyString(), any(String.class));
    }

    @Test
    void getRelatedOutputSchemaReferenceList_return_OutputSchemaReferenceList() {
        when(this.jdbcTemplate.queryForList(eq(GET_OUTPUT_SCHEMA_REFERENCE_BY_ARDQ_ID), same(String.class), eq(ARDQ_ID)))
                .thenReturn(List.of(OUTPUT_SCHEMA_REFERENCE));

        assertEquals(List.of(OUTPUT_SCHEMA_REFERENCE), this.schemaDaoJdbc.getOutputSchemaReferenceList(ARDQ_ID));
    }

    @Test
    void getOutputSchemaReference_return_outputSchemaReference() {
        when(this.jdbcTemplate.queryForObject(anyString(), same(String.class), eq(ARDQ_ID), eq(INPUT_SCHEMA_REFERENCE)))
                .thenReturn(OUTPUT_SCHEMA_REFERENCE);
        final Optional<String> outputSchemaReference = this.schemaDaoJdbc.getOutputSchemaReference(ARDQ_ID, INPUT_SCHEMA_REFERENCE);
        assertTrue(outputSchemaReference.isPresent());
        assertEquals(OUTPUT_SCHEMA_REFERENCE, outputSchemaReference.get());
    }

    @Test
    void getOutputSchemaReference_return_OptionalEmpty() {
        when(this.jdbcTemplate.queryForObject(anyString(), same(String.class), eq(ARDQ_ID), eq(INPUT_SCHEMA_REFERENCE)))
                .thenThrow(EmptyResultDataAccessException.class);
        assertFalse(this.schemaDaoJdbc.getOutputSchemaReference(ARDQ_ID, INPUT_SCHEMA_REFERENCE).isPresent());
    }

    @Test
    void getAffectedArdqRegistrationIds_return_aListOfId() {
        when(this.jdbcTemplate.queryForList(anyString(), same(String.class), eq(INPUT_SCHEMA_REFERENCE)))
                .thenReturn(List.of(ARDQ_ID));
        assertEquals(List.of(ARDQ_ID), this.schemaDaoJdbc.getAffectedArdqRegistrationIds(INPUT_SCHEMA_REFERENCE));
    }

    @Test
    void updateIOSchemaMapping() {

        this.schemaDaoJdbc.updateIOSchemaMapping(new ArrayList<>());

        verify(this.jdbcTemplate, times(1)).batchUpdate(eq(INSERT_IO_SCHEMA), any(BatchPreparedStatementSetter.class));
    }

}
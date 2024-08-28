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

package com.ericsson.oss.air.aas.repository.impl.jdbc.mapper;

import static com.ericsson.oss.air.aas.handler.RegistrationTestUtil.FIELD_DTO1;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.sql.SQLException;

import com.ericsson.oss.air.aas.repository.impl.jdbc.SchemaDaoJdbcImpl;
import com.ericsson.oss.air.api.generated.model.ArdqAugmentationFieldDto;
import com.ericsson.oss.air.exception.AasDaoException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.h2.tools.SimpleResultSet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ArdqAugmentationFieldDtoMapperTest {
    @Test
    void mapRowTest() throws SQLException, JsonProcessingException {
        final String fieldsString = new ObjectMapper().writeValueAsString(FIELD_DTO1);
        final ArdqAugmentationFieldDtoMapper mapper = new ArdqAugmentationFieldDtoMapper();

        final SimpleResultSet resultSet = new SimpleResultSet();
        resultSet.addColumn(SchemaDaoJdbcImpl.COLUMN_FIELD_FIELD_SPEC, 0, 0, 0);

        resultSet.addRow(fieldsString);
        resultSet.next();

        final ArdqAugmentationFieldDto actualDto = mapper.mapRow(resultSet, 0);
        assertNotNull(actualDto);
        assertEquals(FIELD_DTO1, actualDto);
    }

    @Test
    void mapRow_SQLException() throws SQLException {
        final ArdqAugmentationFieldDtoMapper mapper = new ArdqAugmentationFieldDtoMapper();

        final SimpleResultSet resultSet = new SimpleResultSet();
        resultSet.addColumn("RANDOM_COLUMN", 0, 0, 0);
        resultSet.next();

        Assertions.assertThrows(SQLException.class, () -> mapper.mapRow(resultSet, 0));
    }

    @Test
    void mapRow_JsonProcessingException() throws JsonProcessingException, SQLException {
        final ArdqAugmentationFieldDtoMapper mapper = new ArdqAugmentationFieldDtoMapper();

        final SimpleResultSet resultSet = new SimpleResultSet();
        resultSet.addColumn(SchemaDaoJdbcImpl.COLUMN_FIELD_FIELD_SPEC, 0, 0, 0);
        resultSet.addRow("shouldCauseAasDaoError");
        resultSet.next();

        Assertions.assertThrows(AasDaoException.class, () -> mapper.mapRow(resultSet, 0));
    }
}

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

import static com.ericsson.oss.air.aas.handler.RegistrationTestUtil.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.sql.SQLException;

import com.ericsson.oss.air.aas.repository.impl.jdbc.ArdqRegistrationDaoJdbcImpl;
import com.ericsson.oss.air.api.generated.model.ArdqRegistrationDto;
import com.ericsson.oss.air.exception.AasDaoException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.h2.tools.SimpleResultSet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ArdqRegistrationDtoMapperTest {

    @Test
    void mapRowTest() throws JsonProcessingException, SQLException {
        final String ArdqAugmentationRuleDtoString = new ObjectMapper().writeValueAsString(TWO_RULES_REGISTRATION_DTO.getRules());

        final ArdqRegistrationDtoMapper mapper = new ArdqRegistrationDtoMapper();

        final SimpleResultSet resultSet = new SimpleResultSet();
        resultSet.addColumn(ArdqRegistrationDaoJdbcImpl.COLUMN_ARDQ_ID, 0, 0, 0);
        resultSet.addColumn(ArdqRegistrationDaoJdbcImpl.COLUMN_ARDQ_URL, 0, 0, 0);
        resultSet.addColumn(ArdqRegistrationDaoJdbcImpl.COLUMN_ARDQ_RULES, 0, 0, 0);
        resultSet.addColumn(ArdqRegistrationDaoJdbcImpl.COLUMN_ARDQ_TYPE, 0, 0, 0);

        resultSet.addRow(ARDQ_ID, ARDQ_URL, ArdqAugmentationRuleDtoString, ARDQ_TYPE);
        resultSet.next();

        final ArdqRegistrationDto actualDto = mapper.mapRow(resultSet, 0);
        assertNotNull(actualDto);
        assertEquals(TWO_RULES_REGISTRATION_DTO, actualDto);
    }

    @Test
    void mapRow_SQLException() throws SQLException {
        final ArdqRegistrationDtoMapper mapper = new ArdqRegistrationDtoMapper();

        final SimpleResultSet resultSet = new SimpleResultSet();
        resultSet.addColumn("RANDOM_COLUMN", 0, 0, 0);
        resultSet.next();

        Assertions.assertThrows(SQLException.class, () -> mapper.mapRow(resultSet, 0));
    }

    @Test
    void mapRow_JsonProcessingException() throws JsonProcessingException, SQLException {
        final ArdqRegistrationDtoMapper mapper = new ArdqRegistrationDtoMapper();

        final SimpleResultSet resultSet = new SimpleResultSet();
        resultSet.addColumn(ArdqRegistrationDaoJdbcImpl.COLUMN_ARDQ_ID, 0, 0, 0);
        resultSet.addColumn(ArdqRegistrationDaoJdbcImpl.COLUMN_ARDQ_URL, 0, 0, 0);
        resultSet.addColumn(ArdqRegistrationDaoJdbcImpl.COLUMN_ARDQ_RULES, 0, 0, 0);
        resultSet.addColumn(ArdqRegistrationDaoJdbcImpl.COLUMN_ARDQ_TYPE, 0, 0, 0);

        resultSet.addRow("shouldCauseAasDaoError", "test", "test", "test");
        resultSet.next();

        Assertions.assertThrows(AasDaoException.class, () -> mapper.mapRow(resultSet, 0));
    }

}

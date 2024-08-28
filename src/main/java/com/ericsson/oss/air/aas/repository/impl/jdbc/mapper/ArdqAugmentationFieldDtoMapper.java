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

import static com.ericsson.oss.air.aas.repository.impl.jdbc.SchemaDaoJdbcImpl.COLUMN_FIELD_FIELD_SPEC;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.ericsson.oss.air.api.generated.model.ArdqAugmentationFieldDto;
import com.ericsson.oss.air.exception.AasDaoException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.RowMapper;

@Slf4j
public class ArdqAugmentationFieldDtoMapper implements RowMapper<ArdqAugmentationFieldDto> {

    private static final ObjectMapper MAPPER = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Override
    public ArdqAugmentationFieldDto mapRow(final ResultSet rs, final int rowNum) throws SQLException {
        final String jsonString = rs.getString(COLUMN_FIELD_FIELD_SPEC);

        try {
            return MAPPER.readValue(jsonString, ArdqAugmentationFieldDto.class);
        } catch (JsonProcessingException e) {
            log.error("Unable to map ArdqAugmentationField: ", e);
            throw new AasDaoException(e.getMessage());
        }
    }

}

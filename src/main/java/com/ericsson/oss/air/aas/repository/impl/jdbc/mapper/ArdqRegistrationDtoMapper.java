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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.ericsson.oss.air.aas.repository.impl.jdbc.ArdqRegistrationDaoJdbcImpl;
import com.ericsson.oss.air.api.generated.model.ArdqAugmentationRuleDto;
import com.ericsson.oss.air.api.generated.model.ArdqRegistrationDto;
import com.ericsson.oss.air.exception.AasDaoException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.RowMapper;

@Slf4j
public class ArdqRegistrationDtoMapper implements RowMapper<ArdqRegistrationDto> {

    private static final ObjectMapper MAPPER = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Override
    public ArdqRegistrationDto mapRow(final ResultSet rs, final int rowNum) throws SQLException {
        final String ardqId = rs.getString(ArdqRegistrationDaoJdbcImpl.COLUMN_ARDQ_ID);
        final String ardqUrl = rs.getString(ArdqRegistrationDaoJdbcImpl.COLUMN_ARDQ_URL);
        final String ardqType = rs.getString(ArdqRegistrationDaoJdbcImpl.COLUMN_ARDQ_TYPE);
        final String rulesString = rs.getString(ArdqRegistrationDaoJdbcImpl.COLUMN_ARDQ_RULES);

        List<ArdqAugmentationRuleDto> ardqAugmentationRuleDtos = null;
        try {
            ardqAugmentationRuleDtos = MAPPER.readValue(rulesString, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            log.error("Unable to map ArdqRegistration: " + e);
            throw new AasDaoException(e.getMessage());
        }

        return new ArdqRegistrationDto().ardqId(ardqId).ardqUrl(ardqUrl).rules(ardqAugmentationRuleDtos).ardqType(ardqType);
    }

}

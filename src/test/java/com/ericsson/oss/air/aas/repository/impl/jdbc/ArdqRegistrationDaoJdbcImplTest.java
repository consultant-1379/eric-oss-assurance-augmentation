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
import static com.ericsson.oss.air.aas.handler.RegistrationTestUtil.ARDQ_TYPE;
import static com.ericsson.oss.air.aas.handler.RegistrationTestUtil.ARDQ_URL;
import static com.ericsson.oss.air.aas.handler.RegistrationTestUtil.FIELDS;
import static com.ericsson.oss.air.aas.handler.RegistrationTestUtil.INPUT_SCHEMA1;
import static com.ericsson.oss.air.aas.handler.RegistrationTestUtil.INPUT_SCHEMA_REFERENCE;
import static com.ericsson.oss.air.aas.handler.RegistrationTestUtil.NO_FIELD_REGISTRATION_DTO;
import static com.ericsson.oss.air.aas.handler.RegistrationTestUtil.ONE_RULE_REGISTRATION_DTO;
import static com.ericsson.oss.air.aas.handler.RegistrationTestUtil.RULE_DTO4;
import static com.ericsson.oss.air.aas.handler.RegistrationTestUtil.RULE_DTO5;
import static com.ericsson.oss.air.aas.repository.impl.jdbc.ArdqRegistrationDaoJdbcImpl.GET_FIELD_SPEC;
import static com.ericsson.oss.air.aas.repository.impl.jdbc.ArdqRegistrationDaoJdbcImpl.INSERT_ARDQ_REG;
import static com.ericsson.oss.air.aas.repository.impl.jdbc.ArdqRegistrationDaoJdbcImpl.INSERT_FIELD;
import static com.ericsson.oss.air.aas.repository.impl.jdbc.ArdqRegistrationDaoJdbcImpl.SELECT_REG_BY_ARDQ_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.ericsson.oss.air.aas.repository.impl.jdbc.mapper.ArdqAugmentationFieldDtoMapper;
import com.ericsson.oss.air.aas.repository.impl.jdbc.mapper.ArdqRegistrationDtoMapper;
import com.ericsson.oss.air.api.generated.model.ArdqRegistrationDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

@ExtendWith(MockitoExtension.class)
public class ArdqRegistrationDaoJdbcImplTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Mock
    private ObjectMapper mapper = new ObjectMapper();

    private ArdqRegistrationDaoJdbcImpl ardqRegistrationDao;

    @BeforeEach
    void setup() {
        this.ardqRegistrationDao = new ArdqRegistrationDaoJdbcImpl(this.jdbcTemplate, this.namedParameterJdbcTemplate);
    }

    @Test
    void findByArdqId_nullReturned() {
        assertEquals(Optional.empty(),
                this.ardqRegistrationDao.findByArdqId("NON_EXISTED_ARDQ_ID"));
    }

    @Test
    void findByArdqId_jsonReturned() {
        ArdqRegistrationDto ardqRegistrationDto = new ArdqRegistrationDto().ardqId("ArdqId");

        when(this.jdbcTemplate.query(eq(SELECT_REG_BY_ARDQ_ID), any(ArdqRegistrationDtoMapper.class), anyString()))
                .thenReturn(List.of(ardqRegistrationDto));

        assertEquals(Optional.of(ardqRegistrationDto),
                this.ardqRegistrationDao.findByArdqId("ArdqId"));
    }

    @Test
    void getAllArdqRegistrations_notEmptyList() {
        when(this.jdbcTemplate.query(anyString(), any(ArdqRegistrationDtoMapper.class)))
                .thenReturn(List.of(new ArdqRegistrationDto().ardqId("ArdqId")));

        final Optional<List<ArdqRegistrationDto>> allArdqRegistrations = ardqRegistrationDao.getAllArdqRegistrations();

        assertTrue(allArdqRegistrations.isPresent());
        assertFalse(allArdqRegistrations.get().isEmpty());
    }

    @Test
    void getAllArdqRegistrations_withTwoDiffArdqIds_returnTwoArdqRegistrations() {
        when(this.jdbcTemplate.query(anyString(), any(ArdqRegistrationDtoMapper.class)))
                .thenReturn(List.of(new ArdqRegistrationDto().ardqId("ArdqId"),
                        new ArdqRegistrationDto().ardqId("another-ardqId")));

        final Optional<List<ArdqRegistrationDto>> allArdqRegistrations = this.ardqRegistrationDao.getAllArdqRegistrations();

        assertTrue(allArdqRegistrations.isPresent());
        assertEquals(2, allArdqRegistrations.get().size());
        assertEquals(List.of("ArdqId", "another-ardqId"),
                allArdqRegistrations.get().stream().map(ArdqRegistrationDto::getArdqId).collect(Collectors.toList()));
    }

    @Test
    void getAllArdqRegistrations_empty() {
        when(this.jdbcTemplate.query(anyString(), any(ArdqRegistrationDtoMapper.class)))
                .thenReturn(List.of());

        assertEquals(Optional.of(List.of()), this.ardqRegistrationDao.getAllArdqRegistrations());
    }

    @Test
    void saveArdqRegistration_PutRegistration() {
        this.ardqRegistrationDao.saveArdqRegistration(ONE_RULE_REGISTRATION_DTO);

        verify(this.jdbcTemplate, times(1)).update(eq(INSERT_ARDQ_REG), eq(ARDQ_ID), eq(ARDQ_URL), anyString(), eq(ARDQ_TYPE));
        verify(this.namedParameterJdbcTemplate, times(1)).batchUpdate(eq(INSERT_FIELD), any(SqlParameterSource[].class));
    }

    @Test
    void saveFields() {

        this.ardqRegistrationDao.saveFields(ARDQ_ID, List.of(RULE_DTO4));

        verify(this.namedParameterJdbcTemplate, times(1)).batchUpdate(eq(INSERT_FIELD), any(SqlParameterSource[].class));
    }

    @Test
    void saveFields_multipleFields() {

        this.ardqRegistrationDao.saveFields(ARDQ_ID, List.of(RULE_DTO5));

        verify(this.namedParameterJdbcTemplate, times(1)).batchUpdate(eq(INSERT_FIELD), any(SqlParameterSource[].class));
    }

    @Test
    void saveFields_multipleRules() {

        this.ardqRegistrationDao.saveFields(ARDQ_ID, List.of(RULE_DTO4, RULE_DTO5));

        verify(this.namedParameterJdbcTemplate, times(1)).batchUpdate(eq(INSERT_FIELD), any(SqlParameterSource[].class));
    }

    @Test
    void retrieveRegistrationsByInputSchemaRef_shouldReturnRegistrationList() {
        when(this.jdbcTemplate.query(anyString(), any(ArdqRegistrationDtoMapper.class), anyString()))
                .thenReturn(List.of(NO_FIELD_REGISTRATION_DTO));

        final Optional<List<ArdqRegistrationDto>> registrationDtoList = ardqRegistrationDao.retrieveRegistrationsByInputSchemaRef(INPUT_SCHEMA1);

        assertTrue(registrationDtoList.isPresent());
        assertEquals(1, registrationDtoList.get().size());
        assertEquals(NO_FIELD_REGISTRATION_DTO, registrationDtoList.get().get(0));
    }

    @Test
    void retrieveRegistrationsByInputSchemaRef_returnEmptyRegistrationList() {
        when(this.jdbcTemplate.query(anyString(), any(ArdqRegistrationDtoMapper.class), anyString()))
                .thenReturn(List.of());

        assertEquals(Optional.of(List.of()), ardqRegistrationDao.retrieveRegistrationsByInputSchemaRef("schemaRef3"));
    }

    @Test
    void getAugmentationFieldsFromOutputSchema() {
        when(this.jdbcTemplate.query(eq(GET_FIELD_SPEC), any(ArdqAugmentationFieldDtoMapper.class), eq(ARDQ_ID), eq(INPUT_SCHEMA_REFERENCE))).thenReturn(FIELDS);

        assertEquals(Optional.of(FIELDS), this.ardqRegistrationDao.getAugmentationFields(ARDQ_ID, INPUT_SCHEMA_REFERENCE));
    }

    @Test
    void getAugmentationFieldsFromOutputSchema_returnNull() {
        when(this.jdbcTemplate.query(eq(GET_FIELD_SPEC), any(ArdqAugmentationFieldDtoMapper.class), eq(ARDQ_ID), eq(INPUT_SCHEMA_REFERENCE))).thenReturn(List.of());

        assertEquals(Optional.of(List.of()), this.ardqRegistrationDao.getAugmentationFields(ARDQ_ID, INPUT_SCHEMA_REFERENCE));
    }

    @Test
    void deleteArdqRegistrations() {
        this.ardqRegistrationDao.deleteRegistrationByArdqId(ARDQ_ID);

        verify(this.jdbcTemplate, times(1)).update(anyString(), any(String.class));
    }

}

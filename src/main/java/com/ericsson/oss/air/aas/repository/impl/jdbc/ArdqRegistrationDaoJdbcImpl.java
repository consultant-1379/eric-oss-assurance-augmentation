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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.ericsson.oss.air.aas.config.JdbcConfig;
import com.ericsson.oss.air.aas.repository.ArdqRegistrationDao;
import com.ericsson.oss.air.aas.repository.impl.jdbc.mapper.ArdqAugmentationFieldDtoMapper;
import com.ericsson.oss.air.aas.repository.impl.jdbc.mapper.ArdqRegistrationDtoMapper;
import com.ericsson.oss.air.api.generated.model.ArdqAugmentationFieldDto;
import com.ericsson.oss.air.api.generated.model.ArdqAugmentationRuleDto;
import com.ericsson.oss.air.api.generated.model.ArdqRegistrationDto;
import com.ericsson.oss.air.util.LombokExtensions;
import io.github.resilience4j.retry.annotation.Retry;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.experimental.ExtensionMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

/**
 * ArdqRegistrationDao jdbc implementation to store for sharable resources.
 */
@Repository
@Slf4j
@Primary
@AllArgsConstructor
@ConditionalOnBean(JdbcConfig.class)
@ExtensionMethod(LombokExtensions.class)
public class ArdqRegistrationDaoJdbcImpl implements ArdqRegistrationDao {

    public static final String TABLE_REGISTRATION = "registration";
    public static final String TABLE_FIELD = "field";

    public static final String COLUMN_ARDQ_ID = "ardq_id";
    public static final String COLUMN_ARDQ_URL = "ardq_url";
    public static final String COLUMN_ARDQ_RULES = "rules";
    public static final String COLUMN_ARDQ_TYPE = "ardq_type";

    public static final String SELECT_REG_BY_ARDQ_ID = "SELECT * FROM " + TABLE_REGISTRATION
            + " WHERE ardq_id = ?";

    public static final String SELECT_ALL_REG = "SELECT * FROM " + TABLE_REGISTRATION;
    public static final String DELETE_ARDQ_REF = "DELETE FROM " + TABLE_REGISTRATION
            + " WHERE ardq_id = ?";

    public static final String INSERT_ARDQ_REG = "INSERT INTO " + TABLE_REGISTRATION
            + " (ardq_id, ardq_url, rules, ardq_type)"
            + " VALUES (?, ?, ?::json, ?)"
            + " ON CONFLICT (ardq_id) DO UPDATE"
            + " SET ardq_url = EXCLUDED.ardq_url,"
            + " rules = EXCLUDED.rules,"
            + " ardq_type = EXCLUDED.ardq_type";
    public static final String DELETE_FIELD_BY_ID = "DELETE FROM " + TABLE_FIELD
            + " WHERE ardq_id = ?";
    public static final String INSERT_FIELD = "INSERT INTO " + TABLE_FIELD
            + " (ardq_id, input_schema_ref, augment_field, field_spec)"
            + " VALUES (:ardq_id, :input_schema_ref, :augment_field, :field_spec::json)"
            + " ON CONFLICT (ardq_id, input_schema_ref, augment_field) DO UPDATE"
            + " SET field_spec = EXCLUDED.field_spec";
    public static final String GET_ARDQ_REGISTRATIONS_BY_SCHEMA_REFERENCE = "SELECT " + TABLE_REGISTRATION + ".*"
            + " FROM " + TABLE_REGISTRATION
            + " INNER JOIN (SELECT DISTINCT ardq_id FROM " + TABLE_FIELD + " WHERE input_schema_ref = ?) ids"
            + " ON " + TABLE_REGISTRATION + ".ardq_id = ids.ardq_id";

    public static final String GET_FIELD_SPEC = "SELECT field_spec"
            + " FROM " + TABLE_FIELD
            + " WHERE ardq_id = ?"
            + " AND input_schema_ref = ?";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @PostConstruct
    private void postConstruct() {
        log.info("ArdqRegistrationDao bean is created with JDBC support");
    }

    @Override
    @Retry(name = "jdbc")
    public Optional<ArdqRegistrationDto> findByArdqId(final String ardqId) {
        final List<ArdqRegistrationDto> regByArdqId = this.jdbcTemplate.query(SELECT_REG_BY_ARDQ_ID, new ArdqRegistrationDtoMapper(), ardqId);

        if (ObjectUtils.isEmpty(regByArdqId)) {
            return Optional.empty();
        }

        return Optional.of(regByArdqId.get(0));
    }

    @Override
    @Transactional
    @Retry(name = "jdbc")
    public void saveArdqRegistration(final ArdqRegistrationDto ardqRegistrationDto) {

        this.jdbcTemplate.update(INSERT_ARDQ_REG,
                ardqRegistrationDto.getArdqId(),
                ardqRegistrationDto.getArdqUrl(),
                ardqRegistrationDto.getRules().toJsonString(),
                ardqRegistrationDto.getArdqType());

        this.saveFields(ardqRegistrationDto.getArdqId(), ardqRegistrationDto.getRules());
        log.debug("Ardq registration: {} saved to DB successfully ", ardqRegistrationDto);
    }

    void saveFields(final String ardqId, final List<ArdqAugmentationRuleDto> ardqAugmentationRuleDto) {

        this.jdbcTemplate.update(DELETE_FIELD_BY_ID, ardqId);

        final ArrayList<MapSqlParameterSource> mapSqlParameterSourceList = new ArrayList<>();
        for (final ArdqAugmentationRuleDto rule : ardqAugmentationRuleDto) {
            final String inputSchemaRef = rule.getInputSchema();
            for (final ArdqAugmentationFieldDto fieldDto : rule.getFields()) {
                final MapSqlParameterSource mapSqlParameterSource = new MapSqlParameterSource();
                mapSqlParameterSource.addValue("ardq_id", ardqId);
                mapSqlParameterSource.addValue("input_schema_ref", inputSchemaRef);
                mapSqlParameterSource.addValue("augment_field", fieldDto.getOutput());
                mapSqlParameterSource.addValue("field_spec", fieldDto.toJsonString());

                mapSqlParameterSourceList.add(mapSqlParameterSource);
            }
        }

        final SqlParameterSource[] imParaBatch = mapSqlParameterSourceList.toArray(SqlParameterSource[]::new);

        this.namedParameterJdbcTemplate.batchUpdate(INSERT_FIELD, imParaBatch);
    }

    @Override
    @Retry(name = "jdbc")
    public Optional<List<ArdqRegistrationDto>> getAllArdqRegistrations() {
        final List<ArdqRegistrationDto> ardqRegistrationDtoList = jdbcTemplate.query(SELECT_ALL_REG, new ArdqRegistrationDtoMapper());

        if (ObjectUtils.isEmpty(ardqRegistrationDtoList)) {
            return Optional.of(List.of());
        }

        return Optional.of(ardqRegistrationDtoList);
    }

    @Override
    public Optional<Integer> getTotalRegistrations() {
        return this.getAllArdqRegistrations().map(List::size);
    }

    @Override
    @Retry(name = "jdbc")
    public Optional<List<ArdqRegistrationDto>> retrieveRegistrationsByInputSchemaRef(final String schemaRef) {
        List<ArdqRegistrationDto> ardqRegistrationDtoList = jdbcTemplate.query(GET_ARDQ_REGISTRATIONS_BY_SCHEMA_REFERENCE,
                new ArdqRegistrationDtoMapper(), schemaRef);

        return Optional.of(ardqRegistrationDtoList);
    }

    @Override
    @Retry(name = "jdbc")
    public Optional<List<ArdqAugmentationFieldDto>> getAugmentationFields(final String ardqRegId, final String inputSchemaRef) {
        return Optional.of(this.jdbcTemplate.query(GET_FIELD_SPEC, new ArdqAugmentationFieldDtoMapper(), ardqRegId, inputSchemaRef));
    }

    @Override
    @Retry(name = "jdbc")
    public Integer deleteRegistrationByArdqId(final String ardqId) {
        return this.jdbcTemplate.update(DELETE_ARDQ_REF, ardqId);
    }

}

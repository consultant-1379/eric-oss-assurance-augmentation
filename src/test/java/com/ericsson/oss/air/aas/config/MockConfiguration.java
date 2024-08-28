/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.aas.config;

import javax.sql.DataSource;

import com.ericsson.oss.air.aas.service.kafka.KafkaAdminService;
import org.flywaydb.core.Flyway;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

@Configuration
public class MockConfiguration {

    @MockBean
    Flyway flyway;

    @MockBean
    DataSource dataSource;

    @MockBean
    JdbcTemplate jdbcTemplate;

    @MockBean
    NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public KafkaAdminService actualKafkaAdminService;

    @Bean
    @Primary
    public KafkaAdminService mockKafkaAdminService() {
        return Mockito.mock(KafkaAdminService.class);
    }
}

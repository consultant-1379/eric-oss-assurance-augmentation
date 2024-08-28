/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.aas.repository.impl.jdbc;

import static com.ericsson.oss.air.aas.handler.RegistrationTestUtil.INPUT_SCHEMA_REFERENCE;
import static com.ericsson.oss.air.aas.handler.RegistrationTestUtil.OUTPUT_SCHEMA;
import static com.ericsson.oss.air.aas.handler.RegistrationTestUtil.OUTPUT_SCHEMA_NAME;
import static com.ericsson.oss.air.aas.repository.impl.jdbc.SchemaDaoJdbcImpl.INSERT_SCHEMA;
import static com.ericsson.oss.air.util.logging.TestLoggingUtils.EXCEPTION_MSG;
import static com.ericsson.oss.air.util.logging.TestLoggingUtils.assertAuditEvent;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.ericsson.oss.air.aas.config.RetryConfiguration;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.springboot3.retry.autoconfigure.RetryAutoConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = { SchemaDaoJdbcImpl.class, RetryAutoConfiguration.class, RetryConfiguration.class })
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class SchemaDaoJdbcImplRetryTest {

    @Autowired
    private RetryRegistry retryRegistry;

    @Mock
    private JdbcTemplate jdbcTemplate;

    private SchemaDaoJdbcImpl schemaDaoJdbc;

    private Retry retry;

    private Logger log;

    private ListAppender<ILoggingEvent> listAppender;

    @BeforeEach
    public void setUp() {
        this.retry = this.retryRegistry.retry("jdbc");
        this.schemaDaoJdbc = new SchemaDaoJdbcImpl(this.jdbcTemplate);

        this.log = (Logger) LoggerFactory.getLogger(RetryConfiguration.class);
        this.log.setLevel(Level.INFO);

        this.listAppender = new ListAppender<>();
        this.listAppender.start();

        this.log.addAppender(this.listAppender);
    }

    @AfterEach
    public void tearDown() throws Exception {
        this.listAppender.stop();
    }

    @Test
    void saveOutputSchemaWithRetry() {

        final Exception exception = new CannotGetJdbcConnectionException(EXCEPTION_MSG);

        when(this.jdbcTemplate.update(eq(INSERT_SCHEMA), eq(INPUT_SCHEMA_REFERENCE), eq(OUTPUT_SCHEMA_NAME), anyString()))
                .thenThrow(exception);

        assertThrows(CannotGetJdbcConnectionException.class,
                () -> this.retry.executeRunnable(() -> this.schemaDaoJdbc.saveSchema(INPUT_SCHEMA_REFERENCE, OUTPUT_SCHEMA)));

        verify(this.jdbcTemplate, times(3)).update(eq(INSERT_SCHEMA), eq(INPUT_SCHEMA_REFERENCE), eq(OUTPUT_SCHEMA_NAME), anyString());

        final List<ILoggingEvent> loggingEventList = this.listAppender.list;

        assertEquals(1, loggingEventList.size());
        assertAuditEvent(loggingEventList.get(0), Level.ERROR, "Cannot connect to database: ", exception);
    }

    @Test
    void saveOutputSchemaWithoutRetry() {

        final Exception exception = new DataIntegrityViolationException(EXCEPTION_MSG);

        when(this.jdbcTemplate.update(eq(INSERT_SCHEMA), eq(INPUT_SCHEMA_REFERENCE), eq(OUTPUT_SCHEMA_NAME), anyString()))
                .thenThrow(exception);

        assertThrows(DataIntegrityViolationException.class,
                () -> this.retry.executeRunnable(() -> this.schemaDaoJdbc.saveSchema(INPUT_SCHEMA_REFERENCE, OUTPUT_SCHEMA)));

        verify(this.jdbcTemplate, times(1)).update(eq(INSERT_SCHEMA), eq(INPUT_SCHEMA_REFERENCE), eq(OUTPUT_SCHEMA_NAME), anyString());

        assertTrue(this.listAppender.list.isEmpty());
    }

}
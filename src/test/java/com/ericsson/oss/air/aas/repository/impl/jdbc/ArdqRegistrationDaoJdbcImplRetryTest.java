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

import static com.ericsson.oss.air.aas.handler.RegistrationTestUtil.ARDQ_ID;
import static com.ericsson.oss.air.aas.handler.RegistrationTestUtil.ARDQ_TYPE;
import static com.ericsson.oss.air.aas.handler.RegistrationTestUtil.ARDQ_URL;
import static com.ericsson.oss.air.aas.handler.RegistrationTestUtil.ONE_RULE_REGISTRATION_DTO;
import static com.ericsson.oss.air.aas.repository.impl.jdbc.ArdqRegistrationDaoJdbcImpl.INSERT_ARDQ_REG;
import static com.ericsson.oss.air.aas.repository.impl.jdbc.ArdqRegistrationDaoJdbcImpl.SELECT_REG_BY_ARDQ_ID;
import static com.ericsson.oss.air.util.logging.TestLoggingUtils.EXCEPTION_MSG;
import static com.ericsson.oss.air.util.logging.TestLoggingUtils.assertAuditEvent;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.sql.SQLTransientConnectionException;
import java.util.List;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.ericsson.oss.air.aas.config.RetryConfiguration;
import com.ericsson.oss.air.aas.repository.impl.jdbc.mapper.ArdqRegistrationDtoMapper;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.springboot3.retry.autoconfigure.RetryAutoConfiguration;
import org.apache.logging.log4j.util.Strings;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.TransactionUsageException;

@SpringBootTest(classes = { SchemaDaoJdbcImpl.class, RetryAutoConfiguration.class, RetryConfiguration.class })
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class ArdqRegistrationDaoJdbcImplRetryTest {

    @Autowired
    private RetryRegistry retryRegistry;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private ArdqRegistrationDaoJdbcImpl ardqRegistrationDao;

    private Retry retry;

    private Logger log;

    private ListAppender<ILoggingEvent> listAppender;

    @BeforeEach
    public void setUp() {
        this.retry = this.retryRegistry.retry("jdbc");
        this.ardqRegistrationDao = new ArdqRegistrationDaoJdbcImpl(this.jdbcTemplate, this.namedParameterJdbcTemplate);

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
    void findByArdqId_failed_withRetry() {

        final Exception exception = new CannotGetJdbcConnectionException(EXCEPTION_MSG);

        when(this.jdbcTemplate.query(eq(SELECT_REG_BY_ARDQ_ID), any(ArdqRegistrationDtoMapper.class), anyString()))
                .thenThrow(exception);

        assertThrows(CannotGetJdbcConnectionException.class,
                () -> this.retry.executeRunnable(() -> this.ardqRegistrationDao.findByArdqId("NON_EXISTED_ARDQ_ID")));

        verify(this.jdbcTemplate, times(3)).query(eq(SELECT_REG_BY_ARDQ_ID), any(ArdqRegistrationDtoMapper.class), anyString());

        final List<ILoggingEvent> loggingEventList = this.listAppender.list;

        assertEquals(1, loggingEventList.size());
        assertAuditEvent(loggingEventList.get(0), Level.ERROR, "Cannot connect to database: ", exception);
    }

    @Test
    void saveArdqRegistration_failedTransaction_withRetry() {

        final DataAccessException nestedException = new DataAccessResourceFailureException(Strings.EMPTY);
        final TransactionSystemException firstExceptionThrown = new TransactionSystemException(EXCEPTION_MSG);
        firstExceptionThrown.initApplicationException(nestedException);

        final SQLException secondNestedException = new SQLTransientConnectionException(EXCEPTION_MSG);
        final TransactionException secondExceptionThrown = new CannotCreateTransactionException(EXCEPTION_MSG, secondNestedException);

        when(this.jdbcTemplate.update(eq(INSERT_ARDQ_REG), eq(ARDQ_ID), eq(ARDQ_URL), anyString(), eq(ARDQ_TYPE))).thenThrow(firstExceptionThrown,
                secondExceptionThrown, secondExceptionThrown);

        assertThrows(CannotCreateTransactionException.class,
                () -> this.retry.executeRunnable(() -> this.ardqRegistrationDao.saveArdqRegistration(ONE_RULE_REGISTRATION_DTO)));

        verify(this.jdbcTemplate, times(3)).update(eq(INSERT_ARDQ_REG), eq(ARDQ_ID), eq(ARDQ_URL), anyString(), eq(ARDQ_TYPE));

        final List<ILoggingEvent> loggingEventList = this.listAppender.list;

        assertEquals(1, loggingEventList.size());
        assertAuditEvent(loggingEventList.get(0), Level.ERROR, "Cannot connect to database: ", secondExceptionThrown);
    }

    @Test
    void saveArdqRegistration_failedTransaction_withoutRetry() {

        final Exception exception = new TransactionUsageException(EXCEPTION_MSG);

        when(this.jdbcTemplate.update(eq(INSERT_ARDQ_REG), eq(ARDQ_ID), eq(ARDQ_URL), anyString(), eq(ARDQ_TYPE))).thenThrow(exception);

        assertThrows(TransactionUsageException.class,
                () -> this.retry.executeRunnable(() -> this.ardqRegistrationDao.saveArdqRegistration(ONE_RULE_REGISTRATION_DTO)));

        verify(this.jdbcTemplate, times(1)).update(eq(INSERT_ARDQ_REG), eq(ARDQ_ID), eq(ARDQ_URL), anyString(), eq(ARDQ_TYPE));

        assertTrue(this.listAppender.list.isEmpty());
    }

}
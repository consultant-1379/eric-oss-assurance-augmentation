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

import static com.ericsson.oss.air.util.logging.TestLoggingUtils.FACILITY_KEY;
import static com.ericsson.oss.air.util.logging.TestLoggingUtils.FACILITY_VALUE;
import static com.ericsson.oss.air.util.logging.TestLoggingUtils.SUBJECT_KEY;
import static com.ericsson.oss.air.util.logging.TestLoggingUtils.SUBJECT_VALUE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.ErrorCode;
import org.flywaydb.core.api.FlywayException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

@ExtendWith(MockitoExtension.class)
public class FlywaySchemaMigrationTest {

    private static final String EXCEPTION_MESSAGE = "exception message";

    @Mock
    private Flyway flyway;

    @InjectMocks
    private FlywaySchemaMigration flywaySchemaMigration;

    private Logger log;

    private ListAppender<ILoggingEvent> listAppender;

    @BeforeEach
    void setUp() {
        this.log = (Logger) LoggerFactory.getLogger(FlywaySchemaMigration.class);
        this.log.setLevel(Level.INFO);

        this.listAppender = new ListAppender<>();
        this.listAppender.start();

        this.log.addAppender(this.listAppender);
    }

    @AfterEach
    void tearDown() {
        this.listAppender.stop();
    }

    @Test
    void migrate_success() {
        this.flywaySchemaMigration.migrate();

        verify(this.flyway, times(1)).migrate();

        final List<ILoggingEvent> loggingEventList = this.listAppender.list;

        assertFalse(loggingEventList.isEmpty());
        assertEquals(1, loggingEventList.size());

        final ILoggingEvent loggingEvent = this.listAppender.list.get(0);

        assertEquals(Level.INFO, loggingEvent.getLevel());
        assertTrue(loggingEvent.getMDCPropertyMap().isEmpty());

    }

    @Test
    void migrate_NotDBConnectionException() {
        when(this.flyway.migrate()).thenThrow(new FlywayException());

        this.flywaySchemaMigration.migrate();

        verify(this.flyway, times(1)).migrate();
        verify(this.flyway, times(1)).baseline();

        final List<ILoggingEvent> loggingEventList = this.listAppender.list;

        assertFalse(loggingEventList.isEmpty());
        assertEquals(2, loggingEventList.size());

        assertEquals(Level.INFO, this.listAppender.list.get(0).getLevel());
        assertTrue(this.listAppender.list.get(0).getMDCPropertyMap().isEmpty());

        assertEquals(Level.ERROR, this.listAppender.list.get(1).getLevel());
        assertTrue(this.listAppender.list.get(1).getMDCPropertyMap().isEmpty());
    }

    @Test
    void migrate_DBConnectionException() {
        when(this.flyway.migrate()).thenThrow(new FlywayException(EXCEPTION_MESSAGE, ErrorCode.DB_CONNECTION));

        assertThrows(FlywayException.class, () -> this.flywaySchemaMigration.migrate());

        verify(this.flyway, times(1)).migrate();
        verify(this.flyway, never()).baseline();

        final List<ILoggingEvent> loggingEventList = this.listAppender.list;

        assertFalse(loggingEventList.isEmpty());
        assertEquals(2, loggingEventList.size());

        assertEquals(Level.INFO, this.listAppender.list.get(0).getLevel());
        assertTrue(this.listAppender.list.get(0).getMDCPropertyMap().isEmpty());

        final ILoggingEvent auditEvent = this.listAppender.list.get(1);

        assertEquals(Level.ERROR, auditEvent.getLevel());
        assertEquals("Flyway migration failed: " + EXCEPTION_MESSAGE, auditEvent.getFormattedMessage());

        final Map<String, String> mdcPropMap = auditEvent.getMDCPropertyMap();

        assertFalse(mdcPropMap.isEmpty());
        assertEquals(2, mdcPropMap.size());
        assertEquals(FACILITY_VALUE, mdcPropMap.get(FACILITY_KEY));
        assertEquals(SUBJECT_VALUE, mdcPropMap.get(SUBJECT_KEY));
    }

    @Test
    void baseline_NotDBConnectionException() {
        when(this.flyway.migrate()).thenThrow(new FlywayException());
        when(this.flyway.baseline()).thenThrow(new FlywayException());

        assertThrows(FlywayException.class, () -> this.flywaySchemaMigration.migrate());

        verify(this.flyway, times(1)).migrate();
        verify(this.flyway, times(1)).baseline();

        final List<ILoggingEvent> loggingEventList = this.listAppender.list;

        assertFalse(loggingEventList.isEmpty());
        assertEquals(3, loggingEventList.size());

        assertEquals(Level.INFO, this.listAppender.list.get(0).getLevel());
        assertTrue(this.listAppender.list.get(0).getMDCPropertyMap().isEmpty());

        assertEquals(Level.ERROR, this.listAppender.list.get(1).getLevel());
        assertTrue(this.listAppender.list.get(1).getMDCPropertyMap().isEmpty());

        assertEquals(Level.ERROR, this.listAppender.list.get(2).getLevel());
        assertTrue(this.listAppender.list.get(2).getMDCPropertyMap().isEmpty());
    }

    @Test
    void baseline_DBConnectionException() {
        when(this.flyway.migrate()).thenThrow(new FlywayException());
        when(this.flyway.baseline()).thenThrow(new FlywayException(EXCEPTION_MESSAGE, ErrorCode.DB_CONNECTION));

        assertThrows(FlywayException.class, () -> this.flywaySchemaMigration.migrate());

        verify(this.flyway, times(1)).migrate();
        verify(this.flyway, times(1)).baseline();

        final List<ILoggingEvent> loggingEventList = this.listAppender.list;

        assertFalse(loggingEventList.isEmpty());
        assertEquals(3, loggingEventList.size());

        assertEquals(Level.INFO, this.listAppender.list.get(0).getLevel());
        assertTrue(this.listAppender.list.get(0).getMDCPropertyMap().isEmpty());

        assertEquals(Level.ERROR, this.listAppender.list.get(1).getLevel());
        assertTrue(this.listAppender.list.get(1).getMDCPropertyMap().isEmpty());

        final ILoggingEvent auditEvent = this.listAppender.list.get(2);

        assertEquals(Level.ERROR, auditEvent.getLevel());
        assertEquals("Baselining migrations failed: " + EXCEPTION_MESSAGE, auditEvent.getFormattedMessage());

        final Map<String, String> mdcPropMap = auditEvent.getMDCPropertyMap();

        assertFalse(mdcPropMap.isEmpty());
        assertEquals(2, mdcPropMap.size());
        assertEquals(FACILITY_VALUE, mdcPropMap.get(FACILITY_KEY));
        assertEquals(SUBJECT_VALUE, mdcPropMap.get(SUBJECT_KEY));
    }

}

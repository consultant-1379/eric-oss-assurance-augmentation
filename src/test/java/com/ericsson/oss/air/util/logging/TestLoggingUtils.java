/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.util.logging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;

public class TestLoggingUtils {

    public static final String FACILITY_KEY = "facility";

    public static final String SUBJECT_KEY = "subject";

    public static final String FACILITY_VALUE = "log audit";

    public static final String SUBJECT_VALUE = "N/A";

    public static final String EXCEPTION_MSG = "oops";

    public static void assertNonAuditEvent(final ILoggingEvent auditEvent, final Level level, final String message) {

        assertNotNull(auditEvent);
        assertEquals(level, auditEvent.getLevel());
        assertEquals(message, auditEvent.getFormattedMessage());

        assertTrue(auditEvent.getMDCPropertyMap().isEmpty());
    }

    public static void assertAuditEvent(final ILoggingEvent auditEvent, final Level level, final String message) {

        assertNotNull(auditEvent);
        assertEquals(level, auditEvent.getLevel());
        assertEquals(message, auditEvent.getFormattedMessage());

        final Map<String, String> mdcPropMap = auditEvent.getMDCPropertyMap();

        assertFalse(mdcPropMap.isEmpty());
        assertEquals(2, mdcPropMap.size());
        assertEquals(FACILITY_VALUE, mdcPropMap.get(FACILITY_KEY));
        assertEquals(SUBJECT_VALUE, mdcPropMap.get(SUBJECT_KEY));
    }

    public static void assertAuditEvent(final ILoggingEvent auditEvent, final Level level, final String message, final Exception exception) {
        TestLoggingUtils.assertAuditEvent(auditEvent, level, message, exception, EXCEPTION_MSG);
    }

    public static void assertAuditEvent(final ILoggingEvent auditEvent, final Level level, final String message, final Exception exception,
                                        final String exceptionMessage) {

        assertNotNull(auditEvent);
        assertEquals(level, auditEvent.getLevel());
        assertEquals(message, auditEvent.getFormattedMessage());

        final IThrowableProxy iThrowableProxy = auditEvent.getThrowableProxy();

        assertNotNull(iThrowableProxy);
        assertEquals(exceptionMessage, iThrowableProxy.getMessage());
        assertEquals(exception.getClass().getName(), iThrowableProxy.getClassName());

        final Map<String, String> mdcPropMap = auditEvent.getMDCPropertyMap();

        assertFalse(mdcPropMap.isEmpty());
        assertEquals(2, mdcPropMap.size());
        assertEquals(FACILITY_VALUE, mdcPropMap.get(FACILITY_KEY));
        assertEquals(SUBJECT_VALUE, mdcPropMap.get(SUBJECT_KEY));
    }
}
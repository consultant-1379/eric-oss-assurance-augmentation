/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.util.logging.audit;

import org.slf4j.MDC;
import org.slf4j.event.Level;

/**
 * Responsible for logging audit events. Standard logging methods are available for levels TRACE, DEBUG, INFO, WARN and ERROR.
 */
public class AuditLogger {

    private static final String FACILITY_KEY = "facility";

    private static final String SUBJECT_KEY = "subject";

    private static final String AUDIT_LOG = "log audit";

    private static final String NA_SUBJECT = "N/A";

    private final org.slf4j.Logger logger;

    /**
     * Constructs a {@code AuditLogger}.
     *
     * @param clazz the class that needs to emit audit log events
     */
    public AuditLogger(final Class<?> clazz) {
        this.logger = org.slf4j.LoggerFactory.getLogger(clazz);
    }

    /**
     * Logs an audit event at the TRACE level according to the specified format and arguments.
     *
     * @param message   the string to be logged
     * @param arguments optional arguments if the string is a formatted string
     */
    public void trace(final String message, final Object... arguments) {
        this.logAuditEvent(Level.TRACE, message, arguments);
    }

    /**
     * Logs an audit event for an exception (throwable) at the TRACE level with an accompanying message.
     *
     * @param message   the message accompanying the exception
     * @param throwable the exception (throwable) to log
     */
    public void trace(final String message, final Throwable throwable) {
        this.logAuditEvent(Level.TRACE, message, throwable);
    }

    /**
     * Logs an audit event at the DEBUG level according to the specified format and arguments.
     *
     * @param message   the string to be logged
     * @param arguments optional arguments if the string is a formatted string
     */
    public void debug(final String message, final Object... arguments) {
        this.logAuditEvent(Level.DEBUG, message, arguments);
    }

    /**
     * Logs an audit event for an exception (throwable) at the DEBUG level with an accompanying message.
     *
     * @param message   the message accompanying the exception
     * @param throwable the exception (throwable) to log
     */
    public void debug(final String message, final Throwable throwable) {
        this.logAuditEvent(Level.DEBUG, message, throwable);
    }

    /**
     * Logs an audit event at the INFO level according to the specified format and arguments.
     *
     * @param message   the string to be logged
     * @param arguments optional arguments if the string is a formatted string
     */
    public void info(final String message, final Object... arguments) {
        this.logAuditEvent(Level.INFO, message, arguments);
    }

    /**
     * Logs an audit event for an exception (throwable) at the INFO level with an accompanying message.
     *
     * @param message   the message accompanying the exception
     * @param throwable the exception (throwable) to log
     */
    public void info(final String message, final Throwable throwable) {
        this.logAuditEvent(Level.INFO, message, throwable);
    }

    /**
     * Logs an audit event at the WARN level according to the specified format and arguments.
     *
     * @param message   the string to be logged
     * @param arguments optional arguments if the string is a formatted string
     */
    public void warn(final String message, final Object... arguments) {
        this.logAuditEvent(Level.WARN, message, arguments);
    }

    /**
     * Logs an audit event for an exception (throwable) at the WARN level with an accompanying message.
     *
     * @param message   the message accompanying the exception
     * @param throwable the exception (throwable) to log
     */
    public void warn(final String message, final Throwable throwable) {
        this.logAuditEvent(Level.WARN, message, throwable);
    }

    /**
     * Logs an audit event at the ERROR level according to the specified format and arguments.
     *
     * @param message   the string to be logged
     * @param arguments optional arguments if the string is a formatted string
     */
    public void error(final String message, final Object... arguments) {
        this.logAuditEvent(Level.ERROR, message, arguments);
    }

    /**
     * Logs an audit event for an exception (throwable) at the ERROR level with an accompanying message.
     *
     * @param message   the message accompanying the exception
     * @param throwable the exception (throwable) to log
     */
    public void error(final String message, final Throwable throwable) {
        this.logAuditEvent(Level.ERROR, message, throwable);
    }

    private void logAuditEvent(final Level level, final String message, final Object... arguments) {

        MDC.put(FACILITY_KEY, AUDIT_LOG);
        MDC.put(SUBJECT_KEY, NA_SUBJECT);

        logger.makeLoggingEventBuilder(level).log(message, arguments);

        MDC.remove(FACILITY_KEY);
        MDC.remove(SUBJECT_KEY);
    }

    private void logAuditEvent(final Level level, final String message, final Throwable throwable) {

        MDC.put(FACILITY_KEY, AUDIT_LOG);
        MDC.put(SUBJECT_KEY, NA_SUBJECT);

        logger.makeLoggingEventBuilder(level).setMessage(message).setCause(throwable).log();

        MDC.remove(FACILITY_KEY);
        MDC.remove(SUBJECT_KEY);
    }
}


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

import static org.slf4j.event.Level.TRACE;

import org.slf4j.event.Level;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * This class handles all the faults that occur during execution.
 */
@Component
@Slf4j
public class FaultHandler {

    private static final String DEFAULT_PREPENDED_FATAL_MSG = "Fatal error. Service restart may be required. ";

    /**
     * Logs a fatal error.
     *
     * @param throwable the throwable to log about
     */
    public void fatal(final Throwable throwable) {
        this.logFault(Level.ERROR, DEFAULT_PREPENDED_FATAL_MSG, throwable);
    }

    /**
     * Logs a fatal error with a message starting with {@code prependedMessage}.
     *
     * @param prependedMessage the message that will be prepended to the outputted message
     * @param throwable        the throwable to log about
     */
    public void fatal(final String prependedMessage, final Throwable throwable) {
        this.logFault(Level.ERROR, prependedMessage, throwable);
    }

    /**
     * Logs an error.
     *
     * @param throwable the throwable to log about
     */
    public void error(final Throwable throwable) {
        logFault(Level.ERROR, throwable);
    }

    /**
     * Logs an error with a message starting with {@code prependedMessage}.
     *
     * @param prependedMessage the message that will be prepended to the outputted message
     * @param throwable        the throwable to log about
     */
    public void error(final String prependedMessage, final Throwable throwable) {
        this.logFault(Level.ERROR, prependedMessage, throwable);
    }

    /**
     * Logs a warning.
     *
     * @param throwable the throwable to warn about
     */
    public void warn(final Throwable throwable) {
        logFault(Level.WARN, throwable);
    }

    /**
     * Logs a warning with a message starting with {@code prependedMessage}.
     *
     * @param prependedMessage the message that will be prepended to the outputted message
     * @param throwable        the throwable to warn about
     */
    public void warn(final String prependedMessage, final Throwable throwable) {
        this.logFault(Level.WARN, prependedMessage, throwable);
    }

    private void logFault(final org.slf4j.event.Level level, final Throwable throwable) {
        this.logFault(level, "", throwable);
    }

    private void logFault(final Level level, final String prependedMessage, final Throwable throwable) {
        final String message = prependedMessage + throwable.getMessage();
        final Level updatedLevel = log.isTraceEnabled() ? TRACE : level;
        switch (updatedLevel) {
            case TRACE:
                log.trace(prependedMessage, throwable);
                break;
            case WARN:
                log.warn(message);
                break;
            default:
                log.error(message);
        }
    }
}

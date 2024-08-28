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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.air.exception.AasDaoException;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

public class FaultHandlerTest {

    private static final String TEST_PREPENDED_MESSAGE = "Test: ";
    private static final String EXCEPTION_MESSAGE = "Something happened.";

    private final FaultHandler faultHandler = new FaultHandler();

    private Logger log;
    private ListAppender<ILoggingEvent> listAppender;

    @BeforeEach
    void init() {
        log = (Logger) LoggerFactory.getLogger(FaultHandler.class);

        listAppender = new ListAppender<>();
        listAppender.start();

        log.addAppender(listAppender);
    }

    @Test
    void fatal_WithPrependedMessage_LogIsOutput() {
        this.faultHandler.fatal(TEST_PREPENDED_MESSAGE, new AasDaoException(EXCEPTION_MESSAGE));
        final ILoggingEvent element = listAppender.list.get(0);

        Assertions.assertNotNull(element);
        Assertions.assertEquals(Level.ERROR, element.getLevel());
        Assertions.assertTrue(StringUtils.isNotBlank(element.getMessage()));
    }

    @Test
    void fatal_WithDefaultPrependedMessage_LogIsOutput() {
        this.faultHandler.fatal(new AasDaoException(EXCEPTION_MESSAGE));
        final ILoggingEvent element = listAppender.list.get(0);

        Assertions.assertNotNull(element);
        Assertions.assertEquals(Level.ERROR, element.getLevel());
        Assertions.assertTrue(StringUtils.isNotBlank(element.getMessage()));
    }

    @Test
    void error_WithPrependedMessage_LogIsOutput() {
        this.faultHandler.error(TEST_PREPENDED_MESSAGE, new AasDaoException(EXCEPTION_MESSAGE));
        final ILoggingEvent element = listAppender.list.get(0);

        Assertions.assertNotNull(element);
        Assertions.assertEquals(Level.ERROR, element.getLevel());
        Assertions.assertTrue(StringUtils.isNotBlank(element.getMessage()));
    }

    @Test
    void error_WithNoPrependedMessage_LogIsOutput() {
        this.faultHandler.error(new AasDaoException(EXCEPTION_MESSAGE));
        final ILoggingEvent element = listAppender.list.get(0);

        Assertions.assertNotNull(element);
        Assertions.assertEquals(Level.ERROR, element.getLevel());
        Assertions.assertTrue(StringUtils.isNotBlank(element.getMessage()));
    }

    @Test
    void warning_WithPrependedMessage_LogIsOutput() {
        this.faultHandler.warn(TEST_PREPENDED_MESSAGE, new AasDaoException(EXCEPTION_MESSAGE));
        final ILoggingEvent element = listAppender.list.get(0);

        Assertions.assertNotNull(element);
        Assertions.assertEquals(Level.WARN, element.getLevel());
        Assertions.assertTrue(StringUtils.isNotBlank(element.getMessage()));
    }

    @Test
    void warning_WithNoPrependedMessage_LogIsOutput() {
        this.faultHandler.warn(new AasDaoException(EXCEPTION_MESSAGE));
        final ILoggingEvent element = listAppender.list.get(0);

        Assertions.assertNotNull(element);
        Assertions.assertEquals(Level.WARN, element.getLevel());
        Assertions.assertTrue(StringUtils.isNotBlank(element.getMessage()));
    }

    @Test
    void fatal_TraceEnabled_LogIsOutput() {
        log.setLevel(Level.TRACE);

        this.faultHandler.fatal(new AasDaoException(EXCEPTION_MESSAGE));
        final ILoggingEvent element = listAppender.list.get(0);

        Assertions.assertNotNull(element);
        Assertions.assertEquals(Level.TRACE, element.getLevel());
        Assertions.assertTrue(StringUtils.isNotBlank(element.getMessage()));
    }
}

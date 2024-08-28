/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air;

import static com.ericsson.oss.air.util.logging.TestLoggingUtils.assertAuditEvent;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.List;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

class CoreApplicationLoggingTest {

    private Logger log;

    private ListAppender<ILoggingEvent> listAppender;

    @BeforeEach
    public void setUp() {
        this.log = (Logger) LoggerFactory.getLogger(CoreApplication.class);
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
    void init_shutdown_AuditEventsLogged() {

        final AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();

        context.register(CoreApplication.class);

        try {
            context.refresh();
        } catch (final Exception e) {
            /*
             * Throws an exception because cannot wire in beans properly. For the purpose of this test, fixing this
             * exception is not required since this test is verifying the AAS start-up and shut-down logs.
             */
        }

        context.close();

        final List<ILoggingEvent> loggingEventList = listAppender.list;

        assertFalse(loggingEventList.isEmpty());

        final ILoggingEvent startEvent = this.listAppender.list.get(0);
        final ILoggingEvent shutDownEvent = this.listAppender.list.get(1);

        assertAuditEvent(this.listAppender.list.get(0), Level.INFO, "AAS starting");
        assertAuditEvent(this.listAppender.list.get(1), Level.INFO, "AAS shutting down");
    }
}
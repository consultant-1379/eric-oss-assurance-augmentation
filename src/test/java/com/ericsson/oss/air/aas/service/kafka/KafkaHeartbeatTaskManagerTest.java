/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.aas.service.kafka;

import static com.ericsson.oss.air.util.logging.TestLoggingUtils.assertNonAuditEvent;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.ericsson.oss.air.aas.config.kafka.property.KafkaPropertyCustomizer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@ExtendWith(MockitoExtension.class)
class KafkaHeartbeatTaskManagerTest {

    @Mock
    private ScheduledFuture<?> scheduledFuture;

    @Mock
    private ThreadPoolTaskScheduler kafkaHeartbeatTaskScheduler;

    @Mock
    private KafkaPropertyCustomizer kafkaPropertyCustomizer;

    @InjectMocks
    private KafkaHeartbeatTaskManager taskManager;

    private Logger log;

    private ListAppender<ILoggingEvent> listAppender;

    @BeforeEach
    public void setUp() {
        this.log = (Logger) LoggerFactory.getLogger(KafkaHeartbeatTaskManager.class);
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
    void startHeartbeat() {

        when(this.kafkaPropertyCustomizer.kafkaAdminProperties()).thenReturn(Map.of());

        this.taskManager.startHeartbeat();

        verify(this.kafkaHeartbeatTaskScheduler, times(1)).scheduleWithFixedDelay(any(KafkaHeartbeatTask.class),
                any(Instant.class), any(Duration.class));

        final List<ILoggingEvent> loggingEventList = this.listAppender.list;

        assertFalse(loggingEventList.isEmpty());
        assertEquals(1, loggingEventList.size());
        assertNonAuditEvent(this.listAppender.list.get(0), Level.INFO, "Starting scheduler to send heartbeats to Kafka");
    }

    @Test
    void shutdown_NoFuture() {

        this.taskManager.shutdown();

        final List<ILoggingEvent> loggingEventList = this.listAppender.list;

        assertFalse(loggingEventList.isEmpty());
        assertEquals(1, loggingEventList.size());
        assertNonAuditEvent(this.listAppender.list.get(0), Level.INFO, "Stopping heartbeats between AAS and Kafka");
    }

    @Test
    void shutdown_WithFuture() {

        this.taskManager.setHeartbeatFuture(this.scheduledFuture);
        this.taskManager.shutdown();

        verify(this.scheduledFuture, times(1)).cancel(true);

        final List<ILoggingEvent> loggingEventList = this.listAppender.list;

        assertFalse(loggingEventList.isEmpty());
        assertEquals(1, loggingEventList.size());
        assertNonAuditEvent(this.listAppender.list.get(0), Level.INFO, "Stopping heartbeats between AAS and Kafka");
    }
}
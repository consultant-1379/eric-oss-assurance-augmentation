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

import static com.ericsson.oss.air.util.logging.TestLoggingUtils.EXCEPTION_MSG;
import static com.ericsson.oss.air.util.logging.TestLoggingUtils.assertAuditEvent;
import static com.ericsson.oss.air.util.logging.TestLoggingUtils.assertNonAuditEvent;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.DescribeClusterResult;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.errors.TimeoutException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

@ExtendWith(MockitoExtension.class)
class KafkaHeartbeatTaskTest {

    private Map<String, Object> kafkaAdminProperties;

    private KafkaHeartbeatTask heartbeatTask;

    private static MockedStatic<Admin> client;

    private Logger log;

    private ListAppender<ILoggingEvent> listAppender;

    @Mock
    private Admin kafkaAdminClient;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private DescribeClusterResult describeClusterResult;

    @BeforeEach
    public void setUp() {
        this.log = (Logger) LoggerFactory.getLogger(KafkaHeartbeatTask.class);
        this.log.setLevel(Level.INFO);

        this.listAppender = new ListAppender<>();
        this.listAppender.start();

        this.log.addAppender(this.listAppender);

        client = Mockito.mockStatic(Admin.class);
        client.when(() -> Admin.create(Map.of())).thenReturn(this.kafkaAdminClient);

        this.heartbeatTask = new KafkaHeartbeatTask(Map.of());
    }

    @AfterEach
    public void tearDown() throws Exception {
        this.listAppender.stop();
        client.close();
    }

    @Test
    void run() throws ExecutionException, InterruptedException {

        when(this.kafkaAdminClient.describeCluster()).thenReturn(this.describeClusterResult);
        when(this.describeClusterResult.nodes().get()).thenReturn(List.of(new Node(1, "localhost", 9092)));

        this.heartbeatTask.run();

        final List<ILoggingEvent> loggingEventList = this.listAppender.list;

        assertFalse(loggingEventList.isEmpty());
        assertEquals(1, loggingEventList.size());
        assertNonAuditEvent(this.listAppender.list.get(0), Level.INFO, "Sending heartbeat to Kafka");
    }

    @Test
    void run_InterruptedException() throws ExecutionException, InterruptedException {

        final Exception exception = new InterruptedException(EXCEPTION_MSG);

        when(this.kafkaAdminClient.describeCluster()).thenReturn(this.describeClusterResult);
        when(this.describeClusterResult.nodes().get()).thenThrow(exception);

        this.heartbeatTask.run();

        final List<ILoggingEvent> loggingEventList = this.listAppender.list;

        assertFalse(loggingEventList.isEmpty());
        assertEquals(2, loggingEventList.size());
        assertNonAuditEvent(this.listAppender.list.get(0), Level.INFO, "Sending heartbeat to Kafka");
        assertAuditEvent(this.listAppender.list.get(1), Level.ERROR, "Cannot connect to Kafka: ", exception);
    }

    @Test
    void run_OtherException() throws ExecutionException, InterruptedException {

        final Exception exception = new TimeoutException(EXCEPTION_MSG);

        when(this.kafkaAdminClient.describeCluster()).thenReturn(this.describeClusterResult);
        when(this.describeClusterResult.nodes().get()).thenThrow(exception);

        this.heartbeatTask.run();

        final List<ILoggingEvent> loggingEventList = this.listAppender.list;

        assertFalse(loggingEventList.isEmpty());
        assertEquals(2, loggingEventList.size());
        assertNonAuditEvent(this.listAppender.list.get(0), Level.INFO, "Sending heartbeat to Kafka");
        assertAuditEvent(this.listAppender.list.get(1), Level.ERROR, "Cannot connect to Kafka: ", exception);
    }
}
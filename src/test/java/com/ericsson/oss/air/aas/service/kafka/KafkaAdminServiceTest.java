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
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.ericsson.oss.air.aas.config.kafka.property.KafkaPropertyCustomizer;
import com.ericsson.oss.air.exception.KafkaRuntimeException;
import com.ericsson.oss.air.exception.TopicCreationFailedException;
import com.ericsson.oss.air.exception.UnsatisfiedExternalDependencyException;
import com.ericsson.oss.air.util.logging.FaultHandler;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.collections.SetUtils;
import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.DescribeClusterResult;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.errors.TopicExistsException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.config.TopicBuilder;

@ExtendWith(MockitoExtension.class)
public class KafkaAdminServiceTest {

    private static final String TEST_TOPIC1_NAME = "testTopic1";

    private static final String TEST_TOPIC2_NAME = "testTopic2";

    private static final String TEST_TOPIC3_NAME = "testTopic3";

    private static final NewTopic TEST_TOPIC1 = TopicBuilder.name(TEST_TOPIC1_NAME).build();

    private static final NewTopic TEST_TOPIC2 = TopicBuilder.name(TEST_TOPIC2_NAME).build();

    private static final NewTopic TEST_TOPIC3 = TopicBuilder.name(TEST_TOPIC3_NAME).build();

    private static final String SUCCESS_LOG_MSG_TOPIC1 = "Topic: [" + TEST_TOPIC1_NAME + "] created successfully";

    private static final String SUCCESS_LOG_MSG_TOPIC2 = "Topic: [" + TEST_TOPIC2_NAME + "] created successfully";

    private static final String SUCCESS_LOG_MSG_TOPIC3 = "Topic: [" + TEST_TOPIC3_NAME + "] created successfully";

    @Mock
    private FaultHandler faultHandler;

    @Mock
    private KafkaPropertyCustomizer kafkaConfiguration;

    @InjectMocks
    private KafkaAdminService kafkaAdminService;

    @Mock
    private Admin kafkaAdminClient;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ListTopicsResult listTopicsResult;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private CreateTopicsResult createTopicsResult;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private DescribeClusterResult describeClusterResult;

    @Mock
    private KafkaFuture<Void> kafkaFuture;

    @Mock
    private KafkaFuture<Set<String>> kafkaFutureString;

    private static MockedStatic<Admin> client;

    private Logger log;

    private ListAppender<ILoggingEvent> listAppender;

    @BeforeEach
    void setUp() {
        client = Mockito.mockStatic(Admin.class);
        client.when(() -> Admin.create(this.kafkaConfiguration.kafkaAdminProperties())).thenReturn(this.kafkaAdminClient);

        this.log = (Logger) LoggerFactory.getLogger(KafkaAdminService.class);

        this.listAppender = new ListAppender<>();
        this.listAppender.start();

        this.log.addAppender(this.listAppender);
    }

    @AfterEach
    void tearDown() {
        client.close();
    }

    @Test
    public void createKafkaTopics_OneTopic() throws ExecutionException, InterruptedException {

        when(this.kafkaAdminClient.listTopics()).thenReturn(this.listTopicsResult);
        when(this.listTopicsResult.names().get()).thenReturn(SetUtils.EMPTY_SET);

        when(this.kafkaAdminClient.createTopics(Collections.singleton(TEST_TOPIC1))).thenReturn(this.createTopicsResult);

        this.kafkaAdminService.createKafkaTopics(TEST_TOPIC1);

        assertFalse(this.listAppender.list.isEmpty());
        final List<String> infoMessages = this.listAppender.list.stream()
                .filter(log -> log.getLevel() == Level.INFO)
                .map(ILoggingEvent::getFormattedMessage)
                .collect(Collectors.toList());
        assertTrue(infoMessages.contains(SUCCESS_LOG_MSG_TOPIC1));
    }

    @Test
    void createKafkaTopics_NoTopics() throws ExecutionException, InterruptedException {

        when(this.kafkaAdminClient.listTopics()).thenReturn(this.listTopicsResult);
        when(this.listTopicsResult.names().get()).thenReturn(SetUtils.EMPTY_SET);

        assertDoesNotThrow(() -> this.kafkaAdminService.createKafkaTopics());
    }

    @Test
    void createKafkaTopics_MultipleTopics() throws ExecutionException, InterruptedException {

        when(this.kafkaAdminClient.listTopics()).thenReturn(this.listTopicsResult);
        when(this.listTopicsResult.names().get()).thenReturn(SetUtils.EMPTY_SET);

        when(this.kafkaAdminClient.createTopics(Collections.singleton(TEST_TOPIC1))).thenReturn(this.createTopicsResult);
        when(this.kafkaAdminClient.createTopics(Collections.singleton(TEST_TOPIC2))).thenReturn(this.createTopicsResult);
        when(this.kafkaAdminClient.createTopics(Collections.singleton(TEST_TOPIC3))).thenReturn(this.createTopicsResult);

        this.kafkaAdminService.createKafkaTopics(TEST_TOPIC1, TEST_TOPIC2, TEST_TOPIC3);

        assertFalse(this.listAppender.list.isEmpty());
        final List<String> infoMessages = this.listAppender.list.stream()
                .filter(log -> log.getLevel() == Level.INFO)
                .map(ILoggingEvent::getFormattedMessage)
                .collect(Collectors.toList());
        assertTrue(infoMessages.contains(SUCCESS_LOG_MSG_TOPIC1));
        assertTrue(infoMessages.contains(SUCCESS_LOG_MSG_TOPIC2));
        assertTrue(infoMessages.contains(SUCCESS_LOG_MSG_TOPIC3));
    }

    @Test
    void createKafkaTopics_OneTopicAlreadyCreated() throws ExecutionException, InterruptedException {

        when(this.kafkaAdminClient.listTopics()).thenReturn(this.listTopicsResult);
        when(this.listTopicsResult.names().get()).thenReturn(Set.of(TEST_TOPIC1_NAME));

        when(this.kafkaAdminClient.createTopics(Collections.singleton(TEST_TOPIC2))).thenReturn(this.createTopicsResult);

        this.kafkaAdminService.createKafkaTopics(TEST_TOPIC1, TEST_TOPIC2);

        assertFalse(this.listAppender.list.isEmpty());
        final List<String> infoMessages = this.listAppender.list.stream()
                .filter(log -> log.getLevel() == Level.INFO)
                .map(ILoggingEvent::getFormattedMessage)
                .collect(Collectors.toList());
        assertTrue(infoMessages.contains("Topic: [testTopic1] already exists"));
        assertTrue(infoMessages.contains(SUCCESS_LOG_MSG_TOPIC2));
    }

    @Test
    void createKafkaTopics_getTopicsThrowsInterruptedException_ThrowsException() throws ExecutionException, InterruptedException {

        when(this.listTopicsResult.names().get()).thenThrow(InterruptedException.class);
        when(this.kafkaAdminClient.listTopics())
                .thenReturn(this.listTopicsResult)
                .thenThrow(KafkaException.class);

        assertThrows(TopicCreationFailedException.class, () -> this.kafkaAdminService.createKafkaTopics(TEST_TOPIC1));
        assertThrows(TopicCreationFailedException.class, () -> this.kafkaAdminService.createKafkaTopics(TEST_TOPIC1));
    }

    @Test
    void createKafkaTopics_getTopicsThrowsExecutionException_ThrowsException() throws ExecutionException, InterruptedException {

        when(this.kafkaAdminClient.listTopics()).thenReturn(this.listTopicsResult);
        when(this.listTopicsResult.names()).thenReturn(this.kafkaFutureString);
        when(this.kafkaFutureString.get()).thenThrow(new ExecutionException(new KafkaException("Kafka Exception")));

        assertThrows(TopicCreationFailedException.class, () -> this.kafkaAdminService.createKafkaTopics(TEST_TOPIC1));
    }

    @Test
    void createKafkaTopics_createTopicsFutureThrowsInterruptedException_ThrowsException() throws ExecutionException, InterruptedException {

        when(this.kafkaAdminClient.listTopics()).thenReturn(this.listTopicsResult);
        when(this.listTopicsResult.names().get()).thenReturn(SetUtils.EMPTY_SET);

        when(this.kafkaAdminClient.createTopics(Collections.singleton(TEST_TOPIC1))).thenReturn(this.createTopicsResult);
        when(this.createTopicsResult.values().get(TEST_TOPIC1_NAME)).thenReturn(this.kafkaFuture);
        when(this.kafkaFuture.get()).thenThrow(InterruptedException.class);

        assertThrows(TopicCreationFailedException.class, () -> this.kafkaAdminService.createKafkaTopics(TEST_TOPIC1));
    }

    @Test
    void createKafkaTopics_createTopicsFutureThrowsExecutionException_ThrowsException() throws ExecutionException, InterruptedException {

        when(this.kafkaAdminClient.listTopics()).thenReturn(this.listTopicsResult);
        when(this.listTopicsResult.names().get()).thenReturn(SetUtils.EMPTY_SET);

        when(this.kafkaAdminClient.createTopics(Collections.singleton(TEST_TOPIC1))).thenReturn(this.createTopicsResult);
        when(this.createTopicsResult.values().get(TEST_TOPIC1_NAME)).thenReturn(this.kafkaFuture);
        when(this.kafkaFuture.get()).thenThrow(new ExecutionException(new KafkaException("Kafka Exception")));

        assertThrows(TopicCreationFailedException.class, () -> this.kafkaAdminService.createKafkaTopics(TEST_TOPIC1));
    }

    @Test
    void createKafkaTopics_getTopicsThrowsExecutionExceptionWithTopicExistsException_ThrowsException()
            throws ExecutionException, InterruptedException {

        when(this.kafkaAdminClient.listTopics()).thenReturn(this.listTopicsResult);
        when(this.listTopicsResult.names().get()).thenReturn(SetUtils.EMPTY_SET);

        final ExecutionException exception = new ExecutionException(new TopicExistsException("Failed to create message"));
        when(this.kafkaAdminClient.createTopics(Collections.singleton(TEST_TOPIC1))).thenReturn(this.createTopicsResult);
        when(this.createTopicsResult.values().get(TEST_TOPIC1_NAME)).thenReturn(this.kafkaFuture);
        when(this.kafkaFuture.get()).thenThrow(exception);

        assertDoesNotThrow(() -> this.kafkaAdminService.createKafkaTopics(TEST_TOPIC1));
    }

    @Test
    void isServiceAvailable() throws ExecutionException, InterruptedException {

        when(this.kafkaAdminClient.describeCluster()).thenReturn(this.describeClusterResult);
        when(this.describeClusterResult.nodes().get()).thenReturn(List.of(new Node(1, "localhost", 9092)));

        assertTrue(this.kafkaAdminService.verifyConnection());
    }

    @Test
    void isServiceAvailable_NoNodes_ThrowsException() throws ExecutionException, InterruptedException {

        when(this.kafkaAdminClient.describeCluster()).thenReturn(this.describeClusterResult);
        when(this.describeClusterResult.nodes().get()).thenReturn(ListUtils.EMPTY_LIST);

        assertThrows(UnsatisfiedExternalDependencyException.class, () -> this.kafkaAdminService.verifyConnection());
    }

    @Test
    void isServiceAvailable_verifyConnectionThrowsInterruptedException_ThrowsException() throws ExecutionException, InterruptedException {

        when(this.kafkaAdminClient.describeCluster()).thenReturn(this.describeClusterResult);
        when(this.describeClusterResult.nodes().get()).thenThrow(InterruptedException.class);

        assertThrows(UnsatisfiedExternalDependencyException.class, () -> this.kafkaAdminService.verifyConnection());
    }

    @Test
    void isServiceAvailable_verifyConnectionThrowsExecutionException_ThrowsException() throws ExecutionException, InterruptedException {

        when(this.kafkaAdminClient.describeCluster()).thenReturn(this.describeClusterResult);
        when(this.describeClusterResult.nodes().get()).thenThrow(ExecutionException.class);

        assertThrows(UnsatisfiedExternalDependencyException.class, () -> this.kafkaAdminService.verifyConnection());
    }

    @Test
    void recoverCreateKafkaTopics() {
        final Exception exception = new UnsatisfiedExternalDependencyException(EXCEPTION_MSG);

        assertThrows(KafkaRuntimeException.class, () -> this.kafkaAdminService.recoverCreateKafkaTopics(exception));

        final List<ILoggingEvent> loggingEventList = listAppender.list;

        assertFalse(loggingEventList.isEmpty());
        assertEquals(1, loggingEventList.size());
        assertAuditEvent(this.listAppender.list.get(0), Level.ERROR, "Cannot connect to Kafka: ", exception);

    }

    @Test
    void recoverVerifyConnection() {
        final Exception exception = new UnsatisfiedExternalDependencyException(EXCEPTION_MSG);

        assertThrows(KafkaRuntimeException.class, () -> this.kafkaAdminService.recoverVerifyConnection(exception));

        final List<ILoggingEvent> loggingEventList = listAppender.list;

        assertFalse(loggingEventList.isEmpty());
        assertEquals(1, loggingEventList.size());
        assertAuditEvent(this.listAppender.list.get(0), Level.ERROR, "Cannot connect to Kafka: ", exception);
    }
}

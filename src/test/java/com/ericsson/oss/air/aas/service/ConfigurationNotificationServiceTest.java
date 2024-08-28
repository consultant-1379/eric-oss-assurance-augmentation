/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.aas.service;

import static com.ericsson.oss.air.aas.handler.RegistrationTestUtil.VALID_CREATE_ARDQ_REGISTRATION_NOTIFICATION;
import static com.ericsson.oss.air.util.logging.TestLoggingUtils.EXCEPTION_MSG;
import static com.ericsson.oss.air.util.logging.TestLoggingUtils.assertAuditEvent;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.ericsson.oss.air.aas.generated.model.notification.ArdqNotificationType;
import com.ericsson.oss.air.aas.generated.model.notification.ArdqRegistrationNotification;
import com.ericsson.oss.air.aas.service.kafka.KafkaAdminService;
import com.ericsson.oss.air.aas.service.kafka.KafkaHeartbeatTaskManager;
import com.ericsson.oss.air.exception.KafkaRuntimeException;
import org.apache.kafka.common.errors.TimeoutException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.messaging.MessageHeaders;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

@EmbeddedKafka
@SpringBootTest(properties = { "spring.kafka.bootstrapServers=${spring.embedded.kafka.brokers}",
        "spring.kafka.registrationNotification.topic=test" })
@DirtiesContext
@ActiveProfiles("test")
class ConfigurationNotificationServiceTest {

    @SpyBean
    private KafkaAdminService kafkaAdminService;

    @SpyBean
    private KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

    @SpyBean
    private KafkaHeartbeatTaskManager heartbeatTaskManager;

    @SpyBean
    @Qualifier("ArdqRegistrationNotificationKafkaTemplate")
    private KafkaTemplate<String, ArdqRegistrationNotification> kafkaTemplate;

    @Autowired
    private ConfigurationNotificationService configurationNotificationService;

    private Logger log;

    private ListAppender<ILoggingEvent> listAppender;

    @BeforeEach
    public void setUp() {
        this.log = (Logger) LoggerFactory.getLogger(ConfigurationNotificationService.class);
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
    void notify_CreateRegistrationNotification() {

        final ArdqRegistrationNotification notification = ArdqRegistrationNotification.newBuilder()
                .setArdqId("someId").setArdqNotificationType(ArdqNotificationType.CREATE).setDeprecatedInputSchemas(List.of("someSchemaId")).build();
        configurationNotificationService.notify(notification);
    }

    @Test
    void notify_CreateRegistrationNotification_NonConnectivityFailure() {

        final ArdqRegistrationNotification notification = ArdqRegistrationNotification.newBuilder()
                .setArdqId("someId").setArdqNotificationType(ArdqNotificationType.CREATE).setDeprecatedInputSchemas(List.of("someSchemaId")).build();

        doThrow(RuntimeException.class).when(this.kafkaTemplate).send(anyString(), any());

        assertThrows(RuntimeException.class, () -> this.configurationNotificationService.notify(notification));
        assertTrue(this.listAppender.list.isEmpty());
    }

    @Test
    void notify_CreateRegistrationNotification_ConnectivityFailure() {

        final Exception exception = new KafkaException(EXCEPTION_MSG, new TimeoutException());

        final ArdqRegistrationNotification notification = ArdqRegistrationNotification.newBuilder()
                .setArdqId("someId").setArdqNotificationType(ArdqNotificationType.CREATE).setDeprecatedInputSchemas(List.of("someSchemaId")).build();

        doThrow(exception).when(this.kafkaTemplate).send(anyString(), any());

        assertThrows(KafkaException.class, () -> configurationNotificationService.notify(notification));

        final List<ILoggingEvent> loggingEventList = listAppender.list;

        assertFalse(loggingEventList.isEmpty());
        assertEquals(1, loggingEventList.size());
        assertAuditEvent(this.listAppender.list.get(0), Level.ERROR, "Cannot connect to Kafka: ", exception);
    }

    @Test
    void listen_CreateRegistrationNotification() {
        configurationNotificationService.listen(VALID_CREATE_ARDQ_REGISTRATION_NOTIFICATION,
                new MessageHeaders(Collections.singletonMap("test", "test")));
    }

    @Test
    void startKafkaListener() {

        this.configurationNotificationService.startKafkaListener();

        verify(this.heartbeatTaskManager, times(1)).startHeartbeat();
    }

    @Disabled("This junit is not stable and needs to be fixed, commented out to test DPaaS! ESOA-12659")
    @Test
    void startKafkaListener_listenerNotCreated() throws ExecutionException, InterruptedException {

        doReturn(null).when(this.kafkaListenerEndpointRegistry).getListenerContainer(anyString());

        assertThrows(KafkaRuntimeException.class, () -> this.configurationNotificationService.startKafkaListener());
    }

    @Test
    void isReady_True() {
        assertTrue(this.configurationNotificationService.isReady());
    }

    @Test
    void isReady_False() {

        final ConfigurationNotificationService mockNotificationService = mock(ConfigurationNotificationService.class);

        assertFalse(mockNotificationService.isReady());
    }

}
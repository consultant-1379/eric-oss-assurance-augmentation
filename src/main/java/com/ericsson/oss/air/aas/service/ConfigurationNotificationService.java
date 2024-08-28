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

import static com.ericsson.oss.air.aas.service.kafka.KafkaAdminService.CANNOT_CONNECT_KAFKA_MSG;
import static com.ericsson.oss.air.util.ExceptionUtils.isKafkaConnectivityException;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import com.ericsson.oss.air.aas.config.NotificationHandlerConfiguration;
import com.ericsson.oss.air.aas.config.kafka.KafkaConfiguration;
import com.ericsson.oss.air.aas.config.kafka.KafkaProperties;
import com.ericsson.oss.air.aas.generated.model.notification.ArdqRegistrationNotification;
import com.ericsson.oss.air.aas.service.kafka.KafkaAdminService;
import com.ericsson.oss.air.aas.service.kafka.KafkaHeartbeatTaskManager;
import com.ericsson.oss.air.exception.KafkaRuntimeException;
import com.ericsson.oss.air.util.logging.audit.AuditLogFactory;
import com.ericsson.oss.air.util.logging.audit.AuditLogger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

/**
 * Service intended for accepting and responding to registration notification messages
 */
@Service
@Slf4j
public class ConfigurationNotificationService {

    private static final AuditLogger AUDIT_LOGGER = AuditLogFactory.getLogger(ConfigurationNotificationService.class);

    public static final String LISTENER_ID = "ConfigurationNotificationConsumer-" + UUID.randomUUID();

    @Autowired
    private KafkaConfiguration kafkaConfiguration;

    @Autowired
    private KafkaProperties kafkaProperties;

    @Autowired
    private KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

    @Autowired
    private KafkaAdminService kafkaAdminService;

    @Autowired
    private NotificationHandlerConfiguration notificationHandlerConfiguration;

    @Autowired
    @Qualifier("ArdqRegistrationNotificationKafkaTemplate")
    private KafkaTemplate<String, ArdqRegistrationNotification> kafkaTemplate;

    @Autowired
    private KafkaHeartbeatTaskManager heartbeatTaskManager;

    private final AtomicBoolean isServiceReady = new AtomicBoolean();

    /**
     * Notifies listeners of the provided {@link ArdqRegistrationNotification} so that they can take appropriate actions.
     *
     * @param ardqRegistrationNotification the notification message containing registration details
     */
    public void notify(final ArdqRegistrationNotification ardqRegistrationNotification) {

        try {
            this.kafkaTemplate.send(this.kafkaProperties.getRegistrationNotification().getTopic(), ardqRegistrationNotification);
            log.info("[Notification] Notified listeners of message: {}", ardqRegistrationNotification);
        } catch (final Exception e) {

            if (isKafkaConnectivityException(e)) {
                AUDIT_LOGGER.error(CANNOT_CONNECT_KAFKA_MSG, e);
            }

            throw e;
        }

    }

    /**
     * Listens on the {@link ArdqRegistrationNotification}.
     *
     * @param registrationNotification the payload content of the registration notification
     * @param messageHeaders           the message headers
     */
    @KafkaListener(id = "#{__listener.LISTENER_ID}",
                   topics = "${spring.kafka.registrationNotification.topic}",
                   autoStartup = "false",
                   containerFactory = "registrationKafkaListenerContainerFactory")
    public void listen(
            @Payload
            final ArdqRegistrationNotification registrationNotification,
            @Headers
            final MessageHeaders messageHeaders) {

        log.info("[Notification] Received message: {}", registrationNotification);
        log.debug("[Notification] Received message header: {}", messageHeaders);

        this.notificationHandlerConfiguration.getHandler(registrationNotification.getArdqNotificationType()).apply(registrationNotification);
    }

    /**
     * This method do the following:
     * <ol>
     * <li>Checks for Kafka availability.</li>
     * <ol>
     * <li>Creates a topic to produce/listen registration notifications if, the topic do not exist.</li>
     * <li>Starts a listener for the topic to listen registration notifications.</li>
     * </ol>
     * </ol>
     * <p>
     * Kafka listener for registration notification topic has to be created first for restart scenario.
     */
    public void startKafkaListener() {

        this.kafkaAdminService.createKafkaTopics(this.kafkaConfiguration.getNotificationTopic());

        final MessageListenerContainer listenerContainer = this.kafkaListenerEndpointRegistry.getListenerContainer(LISTENER_ID);

        if (ObjectUtils.isEmpty(listenerContainer)) {
            throw new KafkaRuntimeException("Error creating listener for registration notifications.");
        }

        listenerContainer.start();
        log.info("Started listener for topic: [{}]  to listen registration notifications.",
                this.kafkaProperties.getAutoConfigTopics().getNotification().getName());

        this.isServiceReady.compareAndSet(false, true);

        this.heartbeatTaskManager.startHeartbeat();
    }

    /**
     * Returns true if this service is ready to send configuration notification events.
     *
     * @return true if service is ready
     */
    public boolean isReady() {
        return this.isServiceReady.get();
    }
}

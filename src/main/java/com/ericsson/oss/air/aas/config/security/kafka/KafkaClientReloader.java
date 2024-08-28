/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.aas.config.security.kafka;

import java.util.Set;
import java.util.stream.Collectors;

import com.ericsson.oss.air.aas.config.security.TlsConfiguration;
import com.ericsson.oss.air.aas.service.kafka.KafkaAdminService;
import com.ericsson.oss.air.util.logging.audit.AuditLogFactory;
import com.ericsson.oss.air.util.logging.audit.AuditLogger;
import lombok.AccessLevel;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.Lifecycle;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.MessageListenerContainer;

/**
 * This class will reload Kafka clients to use new security configuration.
 */
@Configuration
@Slf4j
@ConditionalOnBean(TlsConfiguration.class)
@Setter(AccessLevel.PACKAGE)
public class KafkaClientReloader {

    private static final AuditLogger AUDIT_LOGGER = AuditLogFactory.getLogger(KafkaClientReloader.class);

    @Autowired
    @Qualifier("ArdqRegistrationNotificationKafkaTemplate")
    private KafkaTemplate ardqKafkaTemplate;

    @Autowired
    @Qualifier("apKafkaTemplate")
    private KafkaTemplate apKafkaTemplate;

    @Autowired
    private KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

    /**
     * Reloads Kafka client configuration whenever the certificates are rotated. Notably, Kafka admin clients are
     * created and closed dynamically at start-up in the {@link KafkaAdminService} so there are no admin clients to reload.
     */
    public void reload() {

        AUDIT_LOGGER.warn("Reloading Kafka configuration");

        this.reloadProducerConfig();
        this.reloadConsumerConfig();

        AUDIT_LOGGER.warn("Reloaded Kafka configuration");
    }

    /*
     * (non-javadoc)
     *
     * Reloads all of AAS' Kafka producer clients.
     */
    private void reloadProducerConfig() {

        AUDIT_LOGGER.warn("Reloading Kafka producers configuration");

        this.ardqKafkaTemplate.getProducerFactory().reset();
        this.apKafkaTemplate.getProducerFactory().reset();

        AUDIT_LOGGER.warn("Reloaded Kafka producers configuration");
    }

    /*
     * (non-javadoc)
     *
     * Reloads all of AAS' Kafka consumer clients.
     */
    private void reloadConsumerConfig() {

        try {

            AUDIT_LOGGER.warn("Reloading Kafka consumers configuration");

            final Set<String> listenerContainerIds = this.kafkaListenerEndpointRegistry.getListenerContainerIds();
            log.info("Stopping kafkaListenerEndpointRegistry, {} containers: [{}]", listenerContainerIds.size(),
                    String.join(",", listenerContainerIds));
            this.kafkaListenerEndpointRegistry.stop();

            log.info("Start kafkaListenerEndpointRegistry");
            this.kafkaListenerEndpointRegistry.start();

            AUDIT_LOGGER.warn("Reloaded Kafka consumers configuration");

            final Set<String> runningContainers = this.kafkaListenerEndpointRegistry.getListenerContainers().stream()
                    .filter(Lifecycle::isRunning)
                    .map(MessageListenerContainer::getListenerId)
                    .collect(Collectors.toSet());
            log.info("kafkaListenerEndpointRegistry has {} running containers: [{}]", runningContainers.size(), String.join(",", runningContainers));
        } catch (final Exception exception) {
            AUDIT_LOGGER.warn("Cannot reload Kafka consumers: ", exception);
        }
    }
}

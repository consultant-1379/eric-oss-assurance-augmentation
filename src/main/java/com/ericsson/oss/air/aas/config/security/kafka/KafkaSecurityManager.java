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

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import com.ericsson.oss.air.aas.config.kafka.KafkaConfiguredEvent;
import com.ericsson.oss.air.aas.config.security.TlsConfiguration;
import com.ericsson.oss.air.aas.model.security.KafkaSecurityMaterial;
import com.ericsson.oss.air.aas.service.kafka.KafkaAdminService;
import com.ericsson.oss.air.util.logging.audit.AuditLogFactory;
import com.ericsson.oss.air.util.logging.audit.AuditLogger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import reactor.core.publisher.Flux;

/**
 * This class will manage mTLS Kafka communication upon the first loading of the certificates and upon certificate rotation.
 */
@Configuration
@ConditionalOnBean(TlsConfiguration.class)
@Slf4j
@RequiredArgsConstructor
public class KafkaSecurityManager {

    private static final AuditLogger AUDIT_LOGGER = AuditLogFactory.getLogger(KafkaSecurityManager.class);

    private final Flux<Optional<KafkaSecurityMaterial>> kafkaTlsContextPublisher;

    private final ApplicationEventPublisher eventPublisher;

    private final KafkaClientReloader kafkaClientReloader;

    private final KafkaAdminService kafkaAdminService;

    private final AtomicBoolean isInitialMaterialLoaded = new AtomicBoolean(false);

    /**
     * Initializes the {@code KafkaSecurityManager} with subscriptions to the required certificates to set up secure
     * Kafka communication.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void init() {

        log.info("Initializing Kafka security configuration");

        this.kafkaTlsContextPublisher
                .subscribe(kafkaSecurityMaterial -> {
                            if (this.isInitialMaterialLoaded.get()) {
                                AUDIT_LOGGER.warn(
                                        "Changes detected in Kafka-related TLS Contexts. After verifying the Kafka connection, the Kafka Client "
                                                + "configuration will be reloaded");

                                this.kafkaAdminService.verifyConnection();
                                this.kafkaClientReloader.reload();

                                return;
                            }
                            if (kafkaSecurityMaterial.isEmpty()) {
                                AUDIT_LOGGER.warn("Missing context to configure secure Kafka communication");
                            } else {
                                this.isInitialMaterialLoaded.set(true);
                                AUDIT_LOGGER.warn("Successfully configured secure Kafka communication");
                                this.eventPublisher.publishEvent(new KafkaConfiguredEvent(this));
                            }
                        },
                        throwable -> AUDIT_LOGGER.warn("Error during processing kafka certificates: ", throwable),
                        () -> AUDIT_LOGGER.warn("The monitor for the Kafka Certificates has unexpectedly shut down")
                );
    }
}
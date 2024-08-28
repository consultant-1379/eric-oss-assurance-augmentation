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

import static com.ericsson.oss.air.util.logging.TestLoggingUtils.EXCEPTION_MSG;
import static com.ericsson.oss.air.util.logging.TestLoggingUtils.assertAuditEvent;
import static com.ericsson.oss.air.util.logging.TestLoggingUtils.assertNonAuditEvent;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.ericsson.adp.security.certm.certificatewatcher.KeyStoreItem;
import com.ericsson.adp.security.certm.certificatewatcher.TrustStoreItem;
import com.ericsson.oss.air.aas.config.kafka.KafkaConfiguredEvent;
import com.ericsson.oss.air.aas.model.security.KafkaSecurityMaterial;
import com.ericsson.oss.air.aas.service.kafka.KafkaAdminService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import reactor.core.publisher.Flux;

@ExtendWith(MockitoExtension.class)
class KafkaSecurityManagerTest {

    private static final String INIT_MSG = "Initializing Kafka security configuration";

    private static final String SUCCESS_CONFIG_MSG = "Successfully configured secure Kafka communication";

    private static final String MONITOR_SHUT_DOWN = "The monitor for the Kafka Certificates has unexpectedly shut down";

    private static final String ERROR_PROCESSING_CERTS = "Error during processing kafka certificates: ";

    private static final String MISSING_CONTEXT_MSG = "Missing context to configure secure Kafka communication";

    private static final String CHANGES_DETECTED_MSG = "Changes detected in Kafka-related TLS Contexts. After verifying the Kafka connection, the "
            + "Kafka Client configuration will be reloaded";

    private static final KeyStoreItem TEST_KEYSTORE_ITEM = KeyStoreItem.builder().build();

    private static final TrustStoreItem TEST_TRUSTSTORE_ITEM = TrustStoreItem.builder().build();

    private static final KafkaSecurityMaterial TEST_KAFKA_SECURITY_MATERIAL = KafkaSecurityMaterial.builder()
            .kafkaKeyStoreItem(TEST_KEYSTORE_ITEM)
            .serverKeyStoreItem(TEST_KEYSTORE_ITEM)
            .trustStoreItem(TEST_TRUSTSTORE_ITEM)
            .build();

    @Mock
    private ApplicationEventPublisher publisher;

    @Mock
    private KafkaClientReloader kafkaClientReloader;

    @Mock
    private KafkaAdminService kafkaAdminService;

    @Mock
    private Optional<KafkaSecurityMaterial> kafkaSecurityMaterial;

    private KafkaSecurityManager kafkaSecurityManager;

    private ListAppender<ILoggingEvent> listAppender;

    @BeforeEach
    void setup() {
        final Logger logger = (Logger) LoggerFactory.getLogger(KafkaSecurityManager.class);
        logger.setLevel(Level.INFO);
        this.listAppender = new ListAppender<>();
        this.listAppender.start();
        logger.addAppender(listAppender);
    }

    @Test
    void init_FirstContextValid() {

        final Flux<Optional<KafkaSecurityMaterial>> testFlux = Flux.just(Optional.of(TEST_KAFKA_SECURITY_MATERIAL));

        this.kafkaSecurityManager = new KafkaSecurityManager(testFlux, this.publisher, this.kafkaClientReloader, this.kafkaAdminService);

        this.kafkaSecurityManager.init();

        verify(this.publisher, times(1)).publishEvent(any(KafkaConfiguredEvent.class));
        verify(this.kafkaClientReloader, never()).reload();

        final List<ILoggingEvent> loggingEventList = this.listAppender.list;

        assertEquals(3, loggingEventList.size());
        assertNonAuditEvent(loggingEventList.get(0), Level.INFO, INIT_MSG);
        assertAuditEvent(loggingEventList.get(1), Level.WARN, SUCCESS_CONFIG_MSG);
        assertAuditEvent(loggingEventList.get(2), Level.WARN, MONITOR_SHUT_DOWN);
    }

    @Test
    void init_ExceptionCatch() {

        final Flux<Optional<KafkaSecurityMaterial>> testFlux = Flux.just(this.kafkaSecurityMaterial);
        final Exception exception = new RuntimeException(EXCEPTION_MSG);

        when(this.kafkaSecurityMaterial.isEmpty()).thenThrow(exception);

        this.kafkaSecurityManager = new KafkaSecurityManager(testFlux, this.publisher, this.kafkaClientReloader, this.kafkaAdminService);

        this.kafkaSecurityManager.init();

        verify(this.publisher, never()).publishEvent(any(KafkaConfiguredEvent.class));
        verify(this.kafkaClientReloader, never()).reload();

        final List<ILoggingEvent> loggingEventList = this.listAppender.list;

        assertEquals(2, loggingEventList.size());
        assertNonAuditEvent(loggingEventList.get(0), Level.INFO, INIT_MSG);
        assertAuditEvent(loggingEventList.get(1), Level.WARN, ERROR_PROCESSING_CERTS, exception);
    }

    @Test
    void init_FirstContextEmpty_SecondContextValid() {

        final Flux<Optional<KafkaSecurityMaterial>> testFlux = Flux.just(Optional.empty(), Optional.of(TEST_KAFKA_SECURITY_MATERIAL));

        this.kafkaSecurityManager = new KafkaSecurityManager(testFlux, this.publisher, this.kafkaClientReloader, this.kafkaAdminService);

        this.kafkaSecurityManager.init();

        verify(this.publisher, times(1)).publishEvent(any(KafkaConfiguredEvent.class));
        verify(this.kafkaClientReloader, never()).reload();

        final List<ILoggingEvent> loggingEventList = this.listAppender.list;

        assertEquals(4, loggingEventList.size());
        assertNonAuditEvent(loggingEventList.get(0), Level.INFO, INIT_MSG);
        assertAuditEvent(loggingEventList.get(1), Level.WARN, MISSING_CONTEXT_MSG);
        assertAuditEvent(loggingEventList.get(2), Level.WARN, SUCCESS_CONFIG_MSG);
        assertAuditEvent(loggingEventList.get(3), Level.WARN, MONITOR_SHUT_DOWN);
    }

    @Test
    void init_Reload_NewSecurityMaterial() {

        final KafkaSecurityMaterial securityMaterialB = KafkaSecurityMaterial.builder()
                .kafkaKeyStoreItem(KeyStoreItem.builder().keyPassword("test").build())
                .serverKeyStoreItem(TEST_KEYSTORE_ITEM)
                .trustStoreItem(TEST_TRUSTSTORE_ITEM)
                .build();

        final Flux<Optional<KafkaSecurityMaterial>> testFlux = Flux.just(Optional.of(TEST_KAFKA_SECURITY_MATERIAL), Optional.of(securityMaterialB));

        this.kafkaSecurityManager = new KafkaSecurityManager(testFlux, this.publisher, this.kafkaClientReloader, this.kafkaAdminService);
        this.kafkaSecurityManager.init();

        verify(this.publisher, times(1)).publishEvent(any(KafkaConfiguredEvent.class));
        verify(this.kafkaAdminService, times(1)).verifyConnection();
        verify(this.kafkaClientReloader, times(1)).reload();

        final List<ILoggingEvent> loggingEventList = this.listAppender.list;

        assertEquals(4, loggingEventList.size());
        assertNonAuditEvent(loggingEventList.get(0), Level.INFO, INIT_MSG);
        assertAuditEvent(loggingEventList.get(1), Level.WARN, SUCCESS_CONFIG_MSG);
        assertAuditEvent(loggingEventList.get(2), Level.WARN, CHANGES_DETECTED_MSG);
        assertAuditEvent(loggingEventList.get(3), Level.WARN, MONITOR_SHUT_DOWN);
    }

    @Test
    void init_EventListenerAnnotation() throws NoSuchMethodException {

        final Class resourceClass = KafkaSecurityManager.class;
        final Method initMethod = resourceClass.getMethod("init");

        final EventListener eventAnnotation = initMethod.getAnnotation(EventListener.class);

        assertNotNull(eventAnnotation);
        assertEquals(ApplicationReadyEvent.class, eventAnnotation.value()[0]);
    }
}
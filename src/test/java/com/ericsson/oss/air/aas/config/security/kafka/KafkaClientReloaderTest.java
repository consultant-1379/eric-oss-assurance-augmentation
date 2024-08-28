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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.apache.kafka.common.errors.SslAuthenticationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

@ExtendWith(MockitoExtension.class)
class KafkaClientReloaderTest {

    private static final String RELOADING = "Reloading ";

    private static final String RELOADED = "Reloaded ";

    private static final String KAFKA_CONFIG = "Kafka configuration";

    private static final String KAFKA_PRODUCERS_CONFIG = "Kafka producers configuration";

    private static final String KAFKA_CONSUMERS_CONFIG = "Kafka consumers configuration";

    private static final String RELOADING_KAFKA_CONFIG = RELOADING + KAFKA_CONFIG;

    private static final String RELOADED_KAFKA_CONFIG = RELOADED + KAFKA_CONFIG;

    private static final String RELOADING_KAFKA_PRODUCERS_CONFIG = RELOADING + KAFKA_PRODUCERS_CONFIG;

    private static final String RELOADED_KAFKA_PRODUCERS_CONFIG = RELOADED + KAFKA_PRODUCERS_CONFIG;

    private static final String RELOADING_KAFKA_CONSUMERS_CONFIG = RELOADING + KAFKA_CONSUMERS_CONFIG;

    private static final String RELOADED_KAFKA_CONSUMERS_CONFIG = RELOADED + KAFKA_CONSUMERS_CONFIG;

    private static final String CANNOT_RELOAD_MSG = "Cannot reload Kafka consumers: ";

    private KafkaTemplate ardqKafkaTemplate;

    private KafkaTemplate apKafkaTemplate;

    @Mock
    private KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

    private KafkaClientReloader kafkaClientReloader;

    private ProducerFactory ardqProducerFactory;

    private ProducerFactory apProducerFactory;

    private Logger log;

    private ListAppender<ILoggingEvent> listAppender;

    @BeforeEach
    void setUp() {
        this.ardqProducerFactory = mock(ProducerFactory.class);
        this.apProducerFactory = mock(ProducerFactory.class);

        this.ardqKafkaTemplate = mock(KafkaTemplate.class);
        this.apKafkaTemplate = mock(KafkaTemplate.class);

        // NOTE: Using Lombok's AllArgsConstructor or Builder causes the application to crash because Spring has trouble
        // distinguishing between the two KafkaTemplate's when injecting them
        this.kafkaClientReloader = new KafkaClientReloader();
        this.kafkaClientReloader.setArdqKafkaTemplate(this.ardqKafkaTemplate);
        this.kafkaClientReloader.setApKafkaTemplate(this.apKafkaTemplate);
        this.kafkaClientReloader.setKafkaListenerEndpointRegistry(this.kafkaListenerEndpointRegistry);

        this.log = (Logger) LoggerFactory.getLogger(KafkaClientReloader.class);
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
    void reload_Successful() {

        when(this.ardqKafkaTemplate.getProducerFactory()).thenReturn(this.ardqProducerFactory);
        when(this.apKafkaTemplate.getProducerFactory()).thenReturn(this.apProducerFactory);

        this.kafkaClientReloader.reload();

        verify(this.ardqProducerFactory, times(1)).reset();
        verify(this.apProducerFactory, times(1)).reset();

        final InOrder inOrder = Mockito.inOrder(this.kafkaListenerEndpointRegistry);
        inOrder.verify(this.kafkaListenerEndpointRegistry, times(1)).stop();
        inOrder.verify(this.kafkaListenerEndpointRegistry, times(1)).start();

        final List<ILoggingEvent> loggingEventList = this.listAppender.list;

        assertFalse(loggingEventList.isEmpty());
        assertEquals(9, loggingEventList.size());

        // Verify auditable events
        assertAuditEvent(loggingEventList.get(0), Level.WARN, RELOADING_KAFKA_CONFIG);
        assertAuditEvent(loggingEventList.get(1), Level.WARN, RELOADING_KAFKA_PRODUCERS_CONFIG);
        assertAuditEvent(loggingEventList.get(2), Level.WARN, RELOADED_KAFKA_PRODUCERS_CONFIG);
        assertAuditEvent(loggingEventList.get(3), Level.WARN, RELOADING_KAFKA_CONSUMERS_CONFIG);
        assertAuditEvent(loggingEventList.get(6), Level.WARN, RELOADED_KAFKA_CONSUMERS_CONFIG);
        assertAuditEvent(loggingEventList.get(8), Level.WARN, RELOADED_KAFKA_CONFIG);

        //Verify other events are not auditable
        for (final ILoggingEvent loggingEvent : List.of(loggingEventList.get(4), loggingEventList.get(5), loggingEventList.get(7))) {
            assertEquals(Level.INFO, loggingEvent.getLevel());
            assertTrue(loggingEvent.getMDCPropertyMap().isEmpty());
        }
    }

    @Test
    void reload_ConsumerConfigFailsToReload() {

        when(this.ardqKafkaTemplate.getProducerFactory()).thenReturn(this.ardqProducerFactory);
        when(this.apKafkaTemplate.getProducerFactory()).thenReturn(this.apProducerFactory);

        final Exception exception = new SslAuthenticationException(EXCEPTION_MSG);
        doThrow(exception).when(this.kafkaListenerEndpointRegistry).start();

        this.kafkaClientReloader.reload();

        final List<ILoggingEvent> loggingEventList = this.listAppender.list;

        assertFalse(loggingEventList.isEmpty());
        assertEquals(8, loggingEventList.size());

        // Verify auditable events
        assertAuditEvent(loggingEventList.get(0), Level.WARN, RELOADING_KAFKA_CONFIG);
        assertAuditEvent(loggingEventList.get(1), Level.WARN, RELOADING_KAFKA_PRODUCERS_CONFIG);
        assertAuditEvent(loggingEventList.get(2), Level.WARN, RELOADED_KAFKA_PRODUCERS_CONFIG);
        assertAuditEvent(loggingEventList.get(3), Level.WARN, RELOADING_KAFKA_CONSUMERS_CONFIG);
        assertAuditEvent(loggingEventList.get(6), Level.WARN, CANNOT_RELOAD_MSG, exception);
        assertAuditEvent(loggingEventList.get(7), Level.WARN, RELOADED_KAFKA_CONFIG);

        //Verify other events are not auditable
        for (final ILoggingEvent loggingEvent : loggingEventList.subList(4, 5)) {
            assertEquals(Level.INFO, loggingEvent.getLevel());
            assertTrue(loggingEvent.getMDCPropertyMap().isEmpty());
        }
    }
}
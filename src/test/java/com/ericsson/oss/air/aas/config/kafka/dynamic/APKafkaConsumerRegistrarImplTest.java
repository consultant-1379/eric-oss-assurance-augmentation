/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 *
 *
 * The copyright to the computer program(s) herein is the property of
 *
 * Ericsson Inc. The programs may be used and/or copied only with written
 *
 * permission from Ericsson Inc. or in accordance with the terms and
 *
 * conditions stipulated in the agreement/contract under which the
 *
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.aas.config.kafka.dynamic;

import static com.ericsson.oss.air.aas.handler.RegistrationTestUtil.OUTPUT_SCHEMA;
import static com.ericsson.oss.air.aas.handler.RegistrationTestUtil.SCHEMA_SUBJECT_OBJ;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.ericsson.oss.air.aas.handler.AugmentationProcessor;
import com.ericsson.oss.air.aas.model.record.SchemaSubject;
import com.ericsson.oss.air.aas.service.AugmentationProcessingService;
import com.ericsson.oss.air.aas.service.ardq.ArdqService;
import com.ericsson.oss.air.exception.KafkaRuntimeException;
import io.micrometer.core.instrument.Counter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.MessageListenerContainer;

@ExtendWith(MockitoExtension.class)
class APKafkaConsumerRegistrarImplTest {

    @Mock
    private KafkaListenerEndpointRegistry registry;

    @Mock
    private MessageListenerContainer container;

    @Mock
    private APMessageListenerEndpointFactory apMessageListenerEndpointFactory;

    @Mock
    private AugmentationProcessingService augProcService;

    @Mock
    private ArdqService ardqService;

    @Mock
    private Counter augmentationErrorsCounter;

    @InjectMocks
    private APKafkaConsumerRegistrarImpl apKafkaConsumerRegistrar;

    @Test
    void getConsumerIds() {
        final Set<String> idList = Set.of("ID1", "ID2", "ID3");
        when(this.registry.getListenerContainerIds()).thenReturn(idList);
        assertEquals(new ArrayList<>(idList), this.apKafkaConsumerRegistrar.getConsumerIds());

    }

    @Test
    void test_createConsumer() {
        this.apKafkaConsumerRegistrar.createConsumer("ConsumerId", "topic",
                SCHEMA_SUBJECT_OBJ,
                List.of(AugmentationProcessor.builder()
                        .ardqUrl("ardq_id")
                        .outputSchema(OUTPUT_SCHEMA)
                        .fields(new ArrayList<>())
                        .ardqService(this.ardqService)
                        .augProcService(this.augProcService)
                        .augmentationErrorsCounter(this.augmentationErrorsCounter)
                        .build()));

        verify(this.apMessageListenerEndpointFactory, times(1)).createKafkaListenerEndpoint(anyString(), anyString(), any(SchemaSubject.class),
                anyList());
    }

    @Test
    void test_pauseConsumer_containerIsFound() {
        when(this.registry.getListenerContainer("consumerId")).thenReturn(null);
        assertThrows(KafkaRuntimeException.class, () -> this.apKafkaConsumerRegistrar.pauseConsumer("consumerId"));
    }

    @Test
    void test_pauseConsumer_containerNotRunning() {
        when(this.registry.getListenerContainer("consumerId")).thenReturn(this.container);
        when(this.container.isRunning()).thenReturn(false);
        assertThrows(KafkaRuntimeException.class, () -> this.apKafkaConsumerRegistrar.pauseConsumer("consumerId"));
    }

    @Test
    void test_pauseConsumer_containerIsPaused() {
        when(this.registry.getListenerContainer("consumerId")).thenReturn(this.container);
        when(this.container.isRunning()).thenReturn(true);
        when(this.container.isContainerPaused()).thenReturn(true);
        assertThrows(KafkaRuntimeException.class, () -> this.apKafkaConsumerRegistrar.pauseConsumer("consumerId"));
    }

    @Test
    void test_pauseConsumer_containerIsRequestedToBePaused() {
        when(this.registry.getListenerContainer("consumerId")).thenReturn(this.container);
        when(this.container.isRunning()).thenReturn(true);
        when(this.container.isContainerPaused()).thenReturn(false);
        when(this.container.isPauseRequested()).thenReturn(true);
        assertThrows(KafkaRuntimeException.class, () -> this.apKafkaConsumerRegistrar.pauseConsumer("consumerId"));
    }

    @Test
    void test_pauseConsumer() {
        when(this.registry.getListenerContainer("consumerId")).thenReturn(this.container);
        when(this.container.isRunning()).thenReturn(true);
        when(this.container.isContainerPaused()).thenReturn(false);
        when(this.container.isPauseRequested()).thenReturn(false);

        this.apKafkaConsumerRegistrar.pauseConsumer("consumerId");
        verify(this.container, times(1)).pause();
    }

    @Test
    void test_resumeConsumer_containerIsFound() {
        when(this.registry.getListenerContainer("consumerId")).thenReturn(null);
        assertThrows(KafkaRuntimeException.class, () -> this.apKafkaConsumerRegistrar.resumeConsumer("consumerId"));
    }

    @Test
    void test_resumeConsumer_containerIsNotRunning() {
        when(this.registry.getListenerContainer("consumerId")).thenReturn(this.container);
        when(this.container.isRunning()).thenReturn(false);
        assertThrows(KafkaRuntimeException.class, () -> this.apKafkaConsumerRegistrar.resumeConsumer("consumerId"));
    }

    @Test
    void test_resumeConsumer_containerIsNotPaused() {
        when(this.registry.getListenerContainer("consumerId")).thenReturn(this.container);
        when(this.container.isRunning()).thenReturn(true);
        when(this.container.isContainerPaused()).thenReturn(false);
        assertThrows(KafkaRuntimeException.class, () -> this.apKafkaConsumerRegistrar.resumeConsumer("consumerId"));
    }

    @Test
    void test_resumeConsumer() {
        when(this.registry.getListenerContainer("consumerId")).thenReturn(this.container);
        when(this.container.isRunning()).thenReturn(true);
        when(this.container.isContainerPaused()).thenReturn(true);
        this.apKafkaConsumerRegistrar.resumeConsumer("consumerId");
        verify(this.container, times(1)).resume();
    }

    @Test
    void test_deactivateConsumer_containerIsFound() {
        when(this.registry.getListenerContainer("consumerId")).thenReturn(null);
        assertThrows(KafkaRuntimeException.class, () -> this.apKafkaConsumerRegistrar.deactivateConsumer("consumerId"));
    }

    @Test
    void test_deactivateConsumer_containerIsAlreadyStop() {
        when(this.registry.getListenerContainer("consumerId")).thenReturn(this.container);
        when(this.container.isRunning()).thenReturn(false);
        assertThrows(KafkaRuntimeException.class, () -> this.apKafkaConsumerRegistrar.deactivateConsumer("consumerId"));
    }

    @Test
    void test_deactivateConsumer() {
        when(this.registry.getListenerContainer("consumerId")).thenReturn(this.container);
        when(this.container.isRunning()).thenReturn(true);

        this.apKafkaConsumerRegistrar.deactivateConsumer("consumerId");
        verify(this.container, times(1)).destroy();
    }

    @Test
    void isCreated_containerIsCreated_true() {
        final Set<String> idList = Set.of("ID1", "ID2", "ID3");
        when(this.registry.getListenerContainerIds()).thenReturn(idList);
        assertTrue(this.apKafkaConsumerRegistrar.isCreated("ID1"));
    }

    @Test
    void isCreated_containerIsNotCreated_false() {
        when(this.registry.getListenerContainerIds()).thenReturn(new HashSet<>());
        assertFalse(this.apKafkaConsumerRegistrar.isCreated("ID1"));
    }

    @Test
    void isRunning_containerIsNotCreated_false() {
        when(this.registry.getListenerContainerIds()).thenReturn(new HashSet<>());
        assertFalse(this.apKafkaConsumerRegistrar.isRunning("consumerId"));
    }

    @Test
    void isRunning_noContainerFound_false() {
        when(this.registry.getListenerContainerIds()).thenReturn(Set.of("consumerId"));
        when(this.registry.getListenerContainer("consumerId")).thenReturn(null);
        assertFalse(this.apKafkaConsumerRegistrar.isRunning("consumerId"));
    }

    @Test
    void isRunning_containerIsRunning_true() {
        when(this.registry.getListenerContainerIds()).thenReturn(Set.of("consumerId"));
        when(this.registry.getListenerContainer("consumerId")).thenReturn(this.container);
        when(this.container.isRunning()).thenReturn(true);
        assertTrue(this.apKafkaConsumerRegistrar.isRunning("consumerId"));
    }

    @Test
    void isRunning_containerIsNotRunning_false() {
        when(this.registry.getListenerContainerIds()).thenReturn(Set.of("consumerId"));
        when(this.registry.getListenerContainer("consumerId")).thenReturn(this.container);
        when(this.container.isRunning()).thenReturn(false);
        assertFalse(this.apKafkaConsumerRegistrar.isRunning("consumerId"));
    }
}

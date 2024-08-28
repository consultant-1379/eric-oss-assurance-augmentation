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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.ericsson.oss.air.aas.handler.AugmentationProcessor;
import com.ericsson.oss.air.aas.model.record.SchemaSubject;
import com.ericsson.oss.air.exception.KafkaRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerEndpoint;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

/**
 * a helper bean for manage the Kafka consumer by using {@link KafkaListenerEndpointRegistry}
 */
@Service
@Slf4j
public class APKafkaConsumerRegistrarImpl implements APKafkaConsumerRegistrar {

    private final KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

    private final ConcurrentKafkaListenerContainerFactory kafkaListenerContainerFactory;

    private final APMessageListenerEndpointFactory apMessageListenerEndpointFactory;

    /**
     * Instantiates a new Ap kafka consumer registrar.
     *
     * @param kafkaListenerEndpointRegistry    the kafka listener endpoint registry
     * @param kafkaListenerContainerFactory    the kafka listener container factory
     * @param apMessageListenerEndpointFactory the ap message listener endpoint factory
     */
    @Autowired
    public APKafkaConsumerRegistrarImpl(final KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry,
                                        @Qualifier("apKafkaListenerContainerFactory")
                                        final ConcurrentKafkaListenerContainerFactory kafkaListenerContainerFactory,
                                        final APMessageListenerEndpointFactory apMessageListenerEndpointFactory) {
        this.kafkaListenerEndpointRegistry = kafkaListenerEndpointRegistry;
        this.kafkaListenerContainerFactory = kafkaListenerContainerFactory;
        this.apMessageListenerEndpointFactory = apMessageListenerEndpointFactory;
    }

    @Override
    public List<String> getConsumerIds() {
        return new ArrayList<>(this.kafkaListenerEndpointRegistry.getListenerContainerIds());
    }

    @Override
    public void createConsumer(final String consumerId, final String topic,
                               final SchemaSubject schemaSubject,
                               final List<AugmentationProcessor> augmentationProcessorList) {

        final KafkaListenerEndpoint kafkaListenerEndpoint = this.apMessageListenerEndpointFactory.createKafkaListenerEndpoint(consumerId, topic,
                schemaSubject, augmentationProcessorList);
        this.kafkaListenerEndpointRegistry.registerListenerContainer(kafkaListenerEndpoint, this.kafkaListenerContainerFactory, true);
        log.info("Consumer with id: [{}] for topic: [{}] is created ", consumerId, topic);
    }

    @Override
    public void pauseConsumer(final String consumerId) {
        final MessageListenerContainer listenerContainer = this.kafkaListenerEndpointRegistry.getListenerContainer(consumerId);

        if (Objects.isNull(listenerContainer)) {
            throw new KafkaRuntimeException(String.format("Consumer with id: [%s] is not found", consumerId));
        } else if (!listenerContainer.isRunning()) {
            throw new KafkaRuntimeException(String.format("Consumer with id: [%s] is not running", consumerId));
        } else if (listenerContainer.isContainerPaused()) {
            throw new KafkaRuntimeException(String.format("Consumer with id: [%s] is already paused", consumerId));
        } else if (listenerContainer.isPauseRequested()) {
            throw new KafkaRuntimeException(String.format("Consumer with id: [%s] is already requested to be paused", consumerId));
        }

        listenerContainer.pause();
        log.info("Consumer with id: [{}] paused successfully ", consumerId);
    }

    @Override
    public void resumeConsumer(final String consumerId) {
        final MessageListenerContainer listenerContainer = this.kafkaListenerEndpointRegistry.getListenerContainer(consumerId);

        if (Objects.isNull(listenerContainer)) {
            throw new KafkaRuntimeException(String.format("Consumer with id: [%s] not found", consumerId));
        } else if (!listenerContainer.isRunning()) {
            throw new KafkaRuntimeException(String.format("Consumer with id: [%s] is not running", consumerId));
        } else if (!listenerContainer.isContainerPaused() && !listenerContainer.isPauseRequested()) {
            throw new KafkaRuntimeException(String.format("Consumer with id: [%s] is not paused", consumerId));
        }

        listenerContainer.resume();
        log.info("Consumer with id: [{}] resumed successfully ", consumerId);
    }

    @Override
    public void deactivateConsumer(final String consumerId) {
        final MessageListenerContainer listenerContainer = this.kafkaListenerEndpointRegistry.getListenerContainer(consumerId);

        if (Objects.isNull(listenerContainer)) {
            throw new KafkaRuntimeException(String.format("Consumer with id: [%s] is not found", consumerId));
        } else if (!listenerContainer.isRunning()) {
            throw new KafkaRuntimeException(String.format("Consumer with id: [%s] is already stop", consumerId));
        }

        listenerContainer.destroy();
        log.info("Consumer with id: [{}] stopped successfully", consumerId);
    }

    @Override
    public boolean isCreated(final String consumerId) {
        final Set<String> listenerContainerIds = this.kafkaListenerEndpointRegistry.getListenerContainerIds();
        final boolean isCreated = listenerContainerIds.contains(consumerId);
        log.debug("Consumer with id: [{}] is created: [{}]", consumerId, isCreated);
        return isCreated;
    }

    @Override
    public boolean isRunning(final String consumerId) {
        if (!this.isCreated(consumerId)) {
            log.debug("Consumer with id: [{}] is not running", consumerId);
            return false;
        }

        final MessageListenerContainer listenerContainer = this.kafkaListenerEndpointRegistry.getListenerContainer(consumerId);

        if (ObjectUtils.isEmpty(listenerContainer)) {
            log.debug("Consumer with id: [{}] is not running", consumerId);
            return false;
        }

        final boolean isRunning = listenerContainer.isRunning() && !(listenerContainer.isContainerPaused() || listenerContainer.isPauseRequested());
        log.debug("Consumer with id: [{}] running status: [{}]", consumerId, isRunning);
        return isRunning;
    }

}

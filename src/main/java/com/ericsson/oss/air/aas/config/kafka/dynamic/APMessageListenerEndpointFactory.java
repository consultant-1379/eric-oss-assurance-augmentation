/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.aas.config.kafka.dynamic;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.config.KafkaListenerEndpoint;
import org.springframework.kafka.config.MethodKafkaListenerEndpoint;
import org.springframework.messaging.handler.annotation.support.DefaultMessageHandlerMethodFactory;
import org.springframework.stereotype.Component;

import com.ericsson.oss.air.aas.config.kafka.property.KafkaPropertyCustomizer;
import com.ericsson.oss.air.aas.handler.AugmentationProcessor;
import com.ericsson.oss.air.aas.model.record.SchemaSubject;

import io.micrometer.core.instrument.Counter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.SneakyThrows;

/**
 * The Message Listener Endpoint factory for augmentation processing
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Component
public class APMessageListenerEndpointFactory {

    @Autowired
    private KafkaPropertyCustomizer kafkaPropertyCustomizer;

    @Autowired
    private Counter augmentedInputRecordsCounter;

    @Autowired
    private Counter nonAugmentedInputRecordsCounter;

    private final ConcurrentHashMap<String, APMessageListener> messageListenerMap = new ConcurrentHashMap<>();

    /**
     * Create a kafka Listener endpoint with default config
     *
     * @param consumerId the consumer id
     * @param topic      the topic
     * @return MethodKafkaListenerEndpoint
     */
    private static MethodKafkaListenerEndpoint<String, String> createDefaultKLE(
            @NonNull final String consumerId,
            @NonNull final String topic) {
        final MethodKafkaListenerEndpoint<String, String> kafkaListenerEndpoint = new MethodKafkaListenerEndpoint<>();
        kafkaListenerEndpoint.setId(consumerId);
        kafkaListenerEndpoint.setGroupId(consumerId);
        kafkaListenerEndpoint.setAutoStartup(true);
        kafkaListenerEndpoint.setTopics(topic);
        kafkaListenerEndpoint.setMessageHandlerMethodFactory(new DefaultMessageHandlerMethodFactory());
        return kafkaListenerEndpoint;
    }

    /**
     * Create kafka listener endpoint kafka listener endpoint.
     *
     * @param consumerId                the consumer id
     * @param topic                     the topic
     * @param schemaSubject             the schema subject
     * @param augmentationProcessorList the augmentation processor list
     * @return the kafka listener endpoint
     */
    @SneakyThrows
    public KafkaListenerEndpoint createKafkaListenerEndpoint(final String consumerId, final String topic,
                                                             final SchemaSubject schemaSubject,
                                                             final List<AugmentationProcessor> augmentationProcessorList) {

        final APMessageListener messageListener = new APMessageListener(augmentationProcessorList, schemaSubject,
                                                                        this.augmentedInputRecordsCounter, this.nonAugmentedInputRecordsCounter);

        final Method onMessageMethod = messageListener.getClass().getMethod("onMessage", ConsumerRecord.class);

        final MethodKafkaListenerEndpoint<String, String> kafkaListenerEndpoint = APMessageListenerEndpointFactory.createDefaultKLE(consumerId,
                                                                                                                                    topic);
        kafkaListenerEndpoint.setBean(messageListener);
        kafkaListenerEndpoint.setMethod(onMessageMethod);

        final Properties properties = new Properties();
        properties.putAll(this.kafkaPropertyCustomizer.apKafkaConsumerProperties());

        kafkaListenerEndpoint.setConsumerProperties(properties);

        this.messageListenerMap.put(consumerId, messageListener);

        return kafkaListenerEndpoint;
    }

    /**
     * Gets message listener by consumerId
     *
     * @param consumerId the consumer id
     * @return the message listener
     */
    public APMessageListener getMessageListener(final String consumerId) {
        return this.messageListenerMap.get(consumerId);
    }
}

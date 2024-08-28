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

package com.ericsson.oss.air.aas.config.kafka;

import java.time.Duration;

import com.ericsson.oss.air.aas.config.kafka.property.KafkaPropertyCustomizer;
import com.ericsson.oss.air.aas.generated.model.notification.ArdqRegistrationNotification;
import com.ericsson.oss.air.util.CustomAvroDeserializer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerConfigUtils;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.retry.annotation.EnableRetry;

/**
 * Sets up Kafka configuration for AAS' notification service and Kafka admin service
 */
@Configuration
@EnableKafka
@EnableRetry
@EnableConfigurationProperties({ KafkaProperties.class })
@RequiredArgsConstructor
@Slf4j
public class KafkaConfiguration {

    private final KafkaProperties kafkaProperties;

    private final KafkaPropertyCustomizer clientConfiguration;

    /**
     * Creates the default Kafka Listener Endpoint Registry.
     *
     * @return Kafka Listener Endpoint Registry
     */
    @Bean(name = KafkaListenerConfigUtils.KAFKA_LISTENER_ENDPOINT_REGISTRY_BEAN_NAME)
    public KafkaListenerEndpointRegistry defaultKafkaListenerEndpointRegistry() {
        return new KafkaListenerEndpointRegistry();
    }

    /**
     * Producer kafka template bean to use for publishing messages
     *
     * @return kafka template
     */
    @Bean(name = "ArdqRegistrationNotificationKafkaTemplate")
    public KafkaTemplate<String, ArdqRegistrationNotification> kafkaTemplate() {

        log.debug("Creating registration notification KafkaTemplate");

        return new KafkaTemplate<>(this.producerFactory());
    }

    /**
     * Producer factory bean to initialize kafka producer factory with defined properties
     *
     * @return producer factory
     */
    private ProducerFactory<String, ArdqRegistrationNotification> producerFactory() {
        return new DefaultKafkaProducerFactory<>(this.clientConfiguration.kafkaProducerProperties());
    }

    /**
     * Kafka listener bean for registration notification
     *
     * @return the custom container factory
     */
    @Bean
    @Primary
    public ConcurrentKafkaListenerContainerFactory<String, ArdqRegistrationNotification> registrationKafkaListenerContainerFactory() {
        final ConcurrentKafkaListenerContainerFactory<String, ArdqRegistrationNotification> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(registrationListenerFactory());
        factory.setContainerCustomizer(
                // Set authExceptionRetryInterval to enable retry
                container -> container.getContainerProperties().setAuthExceptionRetryInterval(Duration.ofSeconds(10L)));
        return factory;
    }

    /**
     * default kafka consumer factory bean with custom properties
     *
     * @return the default kafka consumer factory
     */
    @Bean
    public DefaultKafkaConsumerFactory<String, ArdqRegistrationNotification> registrationListenerFactory() {

        log.debug("Creating RegistrationListenerFactory");

        return new DefaultKafkaConsumerFactory<>(this.clientConfiguration.kafkaConsumerProperties(), new StringDeserializer(),
                                                 new ErrorHandlingDeserializer<>(new CustomAvroDeserializer<>(ArdqRegistrationNotification.class)));
    }

    /**
     * Gets registration notification topic
     *
     * @return a new topic
     */
    public NewTopic getNotificationTopic() {
        final var registrationNotification = this.kafkaProperties.getAutoConfigTopics().getNotification();
        return TopicBuilder.name(registrationNotification.getName())
                .partitions(registrationNotification.getPartitions())
                .replicas(registrationNotification.getReplicas())
                .config(TopicConfig.RETENTION_MS_CONFIG, registrationNotification.getRetention())
                .config(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG, String.valueOf(registrationNotification.getMinInSyncReplicas()))
                .build();
    }
}

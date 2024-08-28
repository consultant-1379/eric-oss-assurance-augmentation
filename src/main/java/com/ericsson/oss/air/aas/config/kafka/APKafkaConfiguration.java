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
import com.ericsson.oss.air.util.CustomApKafkaAvroSerializer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

/**
 * Configures kafka for Augmentation Processing. The configuration for the augmentation producers and consumers must
 * be configured differently compared to the notification producers and consumers because:
 *
 * <ul>
 *     <li>The value deserializer/serializer classes are different and require interaction with the Schema Registry</li>
 *     <li>The producer and consumer properties need to be more finely tuned due to different performance requirements</li>
 * </ul>
 */
@Configuration
@EnableConfigurationProperties({ KafkaProperties.class, APKafkaProperties.class })
@RequiredArgsConstructor
@Slf4j
public class APKafkaConfiguration {

    private final KafkaProperties kafkaProperties;

    private final KafkaPropertyCustomizer clientConfiguration;

    /**
     * Producer factory bean to initialize augmentation processing kafka producer template with defined properties
     *
     * @return producer factory
     */
    private ProducerFactory<String, Object> apProducerFactory() {
        return new DefaultKafkaProducerFactory<>(this.clientConfiguration.apKafkaProducerProperties(),
                                                 new StringSerializer(),
                                                 new CustomApKafkaAvroSerializer(),
                                                 true);
    }

    /**
     * A Kafka listener container factory for data augmentation processing
     *
     * @return the concurrent kafka listener container factory
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> apKafkaListenerContainerFactory() {

        log.debug("Creating augmentation processing ConcurrentKafkaListenerContainerFactory");

        final ConcurrentKafkaListenerContainerFactory<String, String> containerFactory = new ConcurrentKafkaListenerContainerFactory<>();
        final DefaultKafkaConsumerFactory<String, String> factory =
                new DefaultKafkaConsumerFactory<>(this.clientConfiguration.apKafkaConsumerProperties());
        containerFactory.setConsumerFactory(factory);
        containerFactory.setContainerCustomizer(
                // Set authExceptionRetryInterval to enable retry
                container -> container.getContainerProperties().setAuthExceptionRetryInterval(Duration.ofSeconds(10L)));
        return containerFactory;
    }

    /**
     * Augmentation processing producer Kafka template bean to publish messages
     *
     * @return kafka template
     */
    @Bean(name = "apKafkaTemplate")
    public KafkaTemplate<String, Object> apKafkaTemplate() {

        log.debug("Creating augmentation processing KafkaTemplate");

        return new KafkaTemplate<>(this.apProducerFactory());
    }

    /**
     * Get the augmentation processing kafka topic
     *
     * @return kakfa topic
     */
    public NewTopic getAugmentationProcessingTopic() {
        final var augmentationProcessing = this.kafkaProperties.getAutoConfigTopics().getAugmentationProcessing();
        return TopicBuilder.name(augmentationProcessing.getName())
                .partitions(augmentationProcessing.getPartitions())
                .replicas(augmentationProcessing.getReplicas())
                .config(TopicConfig.COMPRESSION_TYPE_CONFIG, augmentationProcessing.getCompression())
                .config(TopicConfig.RETENTION_MS_CONFIG, augmentationProcessing.getRetention())
                .config(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG, String.valueOf(augmentationProcessing.getMinInSyncReplicas()))
                .build();
    }
}

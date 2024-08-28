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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;

import com.ericsson.oss.air.aas.config.kafka.property.CommonKafkaCustomProperties;
import com.ericsson.oss.air.aas.config.kafka.property.KafkaPropertyCustomizer;
import com.ericsson.oss.air.aas.config.kafka.property.UnsecureKafkaPropertyCustomizer;
import com.ericsson.oss.air.aas.config.schemaregistry.SchemaRegistryConfiguration;

@SpringBootTest(classes = { APKafkaConfiguration.class, SchemaRegistryConfiguration.class, UnsecureKafkaPropertyCustomizer.class,
        CommonKafkaCustomProperties.class })
@ActiveProfiles("test")
class APKafkaConfigurationTest {

    @Autowired
    private SchemaRegistryConfiguration mockSchemaRegistryConfiguration;

    @Autowired
    private KafkaPropertyCustomizer clientConfiguration;

    @Autowired
    private APKafkaConfiguration apKafkaConfiguration;

    @Autowired
    private ApplicationContext appContext;

    @Test
    void concurrentKafkaListenerContainerFactory_bean_created_success() {
        assertDoesNotThrow(() -> this.appContext.getBean(ConcurrentKafkaListenerContainerFactory.class));
    }

    @Test
    void apKafkaTemplate_bean_created_success() {
        assertDoesNotThrow(() -> this.appContext.getBean(KafkaTemplate.class));
    }

    @Test
    void getAugmentationOutputTopic() {

        final NewTopic augmentationProcessingTopic = this.apKafkaConfiguration.getAugmentationProcessingTopic();

        assertEquals("eric-oss-assurance-augmentation-processing", augmentationProcessingTopic.name());
        assertEquals(1, augmentationProcessingTopic.replicationFactor());
        assertEquals(3, augmentationProcessingTopic.numPartitions());
        assertEquals("lz4", augmentationProcessingTopic.configs().get(TopicConfig.COMPRESSION_TYPE_CONFIG));
        assertEquals("7200000", augmentationProcessingTopic.configs().get(TopicConfig.RETENTION_MS_CONFIG));
    }
}
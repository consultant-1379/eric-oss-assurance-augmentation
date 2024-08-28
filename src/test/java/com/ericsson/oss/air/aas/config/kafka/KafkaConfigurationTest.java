/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;

import com.ericsson.oss.air.aas.config.kafka.property.CommonKafkaCustomProperties;
import com.ericsson.oss.air.aas.config.kafka.property.KafkaPropertyCustomizer;
import com.ericsson.oss.air.aas.config.kafka.property.UnsecureKafkaPropertyCustomizer;

@SpringBootTest(classes = { UnsecureKafkaPropertyCustomizer.class, KafkaConfiguration.class, CommonKafkaCustomProperties.class })
@ActiveProfiles("test")
class KafkaConfigurationTest {

    @MockBean
    private APKafkaProperties apKafkaProperties;

    @Autowired
    private KafkaPropertyCustomizer clientConfiguration;

    @Autowired
    private KafkaConfiguration kafkaConfiguration;

    @Autowired
    private ApplicationContext appContext;

    @Test
    void defaultKafkaListenerEndpointRegistry() {
        assertDoesNotThrow(() -> this.appContext.getBean(KafkaListenerEndpointRegistry.class));
    }

    @Test
    void kafkaTemplate() {
        assertDoesNotThrow(() -> this.appContext.getBean(KafkaTemplate.class));
    }

    @Test
    void registrationKafkaListenerContainerFactory() {
        assertDoesNotThrow(() -> this.appContext.getBean(ConcurrentKafkaListenerContainerFactory.class));
    }

    @Test
    void registrationListenerFactory() {
        assertDoesNotThrow(() -> this.appContext.getBean(DefaultKafkaConsumerFactory.class));
    }

    @Test
    void getNotificationTopic() {

        final NewTopic notificationTopic = this.kafkaConfiguration.getNotificationTopic();

        assertEquals("eric-oss-assurance-augmentation-notification", notificationTopic.name());
        assertEquals(1, notificationTopic.replicationFactor());
        assertEquals(1, notificationTopic.numPartitions());
        assertEquals("1", notificationTopic.configs().get(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG));
        assertEquals("600000", notificationTopic.configs().get(TopicConfig.RETENTION_MS_CONFIG));
    }
}
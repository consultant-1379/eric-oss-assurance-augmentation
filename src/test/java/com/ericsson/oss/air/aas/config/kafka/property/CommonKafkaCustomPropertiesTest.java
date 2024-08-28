/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.aas.config.kafka.property;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ericsson.oss.air.aas.config.kafka.APKafkaProperties;
import com.ericsson.oss.air.aas.config.kafka.KafkaProperties;
import com.ericsson.oss.air.util.CustomAvroDeserializer;
import com.ericsson.oss.air.util.CustomAvroSerializer;

import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;

@ExtendWith(MockitoExtension.class)
class CommonKafkaCustomPropertiesTest {

    private static final int ADMIN_RETRY = 2;

    private static final int ADMIN_RETRY_BACKOFF = 100;

    private static final int ADMIN_RECONNECT_BACKOFF = 50;

    private static final int ADMIN_RECONNECT_BACKOFF_MAX = 30000;

    private static final int ADMIN_REQUEST_TIMEOUT = 30000;

    private static final int ADMIN_RETRY_INTERVAL = 10;

    private static final String BOOTSTRAP_SERVERS = "localhost:9092";

    private static final String NOTIFICATION_TOPIC = "eric-oss-assurance-augmentation-notification";

    private static final int NOTIFICATION_PARTITIONS = 1;

    private static final short NOTIFICATION_REPLICAS = 1;

    private static final String NOTIFICATION_RETENTION = "600000";

    private static final int NOTIFICATION_MIN_INSYNC_REPLICAS = 1;

    private static final String PROCESSING_TOPIC = "eric-oss-assurance-augmentation-processing";

    private static final int PROCESSING_PARTITIONS = 1;

    private static final short PROCESSING_REPLICAS = 1;

    private static final String PROCESSING_COMPRESSION = "lz4";

    private static final String PROCESSING_RETENTION = "7200000";

    private static final int PROCESSING_MIN_INSYNC_REPLICAS = 1;

    private static final int CONSUMER_MAX_POLL_RECORDS = 200;

    private static final int CONSUMER_MAX_POLL_INTERVAL = 200;

    private static final int APCONSUMER_MAX_POLL_RECORDS = 100;

    private static final int APCONSUMER_MAX_POLL_INTERVAL = 100;

    private static final int PRODUCER_REQUEST_TIMEOUT = 30000;

    private static final int APPRODUCER_REQUEST_TIMEOUT = 10000;

    private static final int PRODUCER_RETRY_BACKOFF = 100;

    private static final int PRODUCER_RECONNECT_BACKOFF = 50;

    private static final int PRODUCER_RECONNECT_BACKOFF_MAX = 30000;

    private static final int PRODUCER_BATCH_SIZE = 163840;

    private static final int PRODUCER_BUFFER_MEMORY = 32000000;

    private static final int PRODUCER_MAX_REQUEST_SIZE = 2000;

    private static final int PRODUCER_LINGER = 2000;

    private static final KafkaProperties.Admin ADMIN = new KafkaProperties.Admin(ADMIN_RETRY,
                                                                                 ADMIN_RETRY_BACKOFF,
                                                                                 ADMIN_RECONNECT_BACKOFF,
                                                                                 ADMIN_RECONNECT_BACKOFF_MAX,
                                                                                 ADMIN_REQUEST_TIMEOUT,
                                                                                 ADMIN_RETRY_INTERVAL);

    private static final KafkaProperties.Consumer CONSUMER = new KafkaProperties.Consumer(CONSUMER_MAX_POLL_RECORDS, CONSUMER_MAX_POLL_RECORDS);

    private static final KafkaProperties.Producer PRODUCER = new KafkaProperties.Producer(PRODUCER_REQUEST_TIMEOUT);

    private static final KafkaProperties.RegistrationNotification REGISTRATION_NOTIFICATION = new KafkaProperties.RegistrationNotification(
            NOTIFICATION_TOPIC,
            CONSUMER,
            PRODUCER);

    private static final KafkaProperties.Notification NOTIFICATION = new KafkaProperties.Notification(NOTIFICATION_TOPIC,
                                                                                                      NOTIFICATION_PARTITIONS,
                                                                                                      NOTIFICATION_REPLICAS,
                                                                                                      NOTIFICATION_RETENTION,
                                                                                                      NOTIFICATION_MIN_INSYNC_REPLICAS);

    private static final KafkaProperties.AugmentationProcessing AUGMENTATION_PROCESSING = new KafkaProperties.AugmentationProcessing(PROCESSING_TOPIC,
                                                                                                                                     PROCESSING_PARTITIONS,
                                                                                                                                     PROCESSING_REPLICAS,
                                                                                                                                     PROCESSING_COMPRESSION,
                                                                                                                                     PROCESSING_RETENTION,
                                                                                                                                     PROCESSING_MIN_INSYNC_REPLICAS);

    private static final KafkaProperties.AutoConfigTopics AUTO_CONFIG_TOPICS = new KafkaProperties.AutoConfigTopics(NOTIFICATION,
                                                                                                                    AUGMENTATION_PROCESSING);

    private static final KafkaProperties KAFKA_PROPERTIES = new KafkaProperties(ADMIN,
                                                                                BOOTSTRAP_SERVERS,
                                                                                AUTO_CONFIG_TOPICS,
                                                                                REGISTRATION_NOTIFICATION);

    private static final APKafkaProperties.Consumer AP_CONSUMER = new APKafkaProperties.Consumer(APCONSUMER_MAX_POLL_RECORDS,
                                                                                                 APCONSUMER_MAX_POLL_INTERVAL);

    private static final APKafkaProperties.Producer AP_PRODUCER = new APKafkaProperties.Producer(APPRODUCER_REQUEST_TIMEOUT,
                                                                                                 PRODUCER_RETRY_BACKOFF,
                                                                                                 PRODUCER_RECONNECT_BACKOFF,
                                                                                                 PRODUCER_RECONNECT_BACKOFF_MAX,
                                                                                                 PRODUCER_BATCH_SIZE,
                                                                                                 PRODUCER_BUFFER_MEMORY,
                                                                                                 PRODUCER_MAX_REQUEST_SIZE,
                                                                                                 PRODUCER_LINGER);

    private static final APKafkaProperties.AugmentationProcessing AP_AUGMENTATION_PROCESSING = new APKafkaProperties.AugmentationProcessing(
            PROCESSING_TOPIC,
            AP_CONSUMER,
            AP_PRODUCER);

    private static final APKafkaProperties AP_KAFKA_PROPERTIES = new APKafkaProperties(AP_AUGMENTATION_PROCESSING);

    private static final String TEST_URL = "http://localhost:8081";

    private CommonKafkaCustomProperties commonKafkaCustomProperties = new CommonKafkaCustomProperties(KAFKA_PROPERTIES,
                                                                                                      AP_KAFKA_PROPERTIES);

    @BeforeEach
    void setUp() {

        this.commonKafkaCustomProperties = new CommonKafkaCustomProperties(KAFKA_PROPERTIES, AP_KAFKA_PROPERTIES);
        this.commonKafkaCustomProperties.setSchemaRegistryUrl(TEST_URL);
    }

    @Test
    void getConsumerProperties() {

        final Map<String, Object> consumerMap = this.commonKafkaCustomProperties.getConsumerProperties();

        assertEquals(5, consumerMap.size());
        assertEquals(BOOTSTRAP_SERVERS, consumerMap.get(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG));
        assertEquals(StringDeserializer.class, consumerMap.get(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG));
        assertEquals(CustomAvroDeserializer.class, consumerMap.get(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG));
        assertEquals(CONSUMER_MAX_POLL_RECORDS, consumerMap.get(ConsumerConfig.MAX_POLL_RECORDS_CONFIG));
        assertEquals(CONSUMER_MAX_POLL_INTERVAL, consumerMap.get(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG));
    }

    @Test
    void getProducerProperties() {

        final Map<String, Object> producerMap = this.commonKafkaCustomProperties.getProducerProperties();

        assertEquals(4, producerMap.size());
        assertEquals(BOOTSTRAP_SERVERS, producerMap.get(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG));
        assertEquals(StringSerializer.class, producerMap.get(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG));
        assertEquals(CustomAvroSerializer.class, producerMap.get(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG));
        assertEquals(PRODUCER_REQUEST_TIMEOUT, producerMap.get(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG));
    }

    @Test
    void getAdminProperties() {

        final Map<String, Object> adminMap = this.commonKafkaCustomProperties.getAdminProperties();

        assertEquals(6, adminMap.size());
        assertEquals(BOOTSTRAP_SERVERS, adminMap.get(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG));
        assertEquals(ADMIN_RETRY, adminMap.get(AdminClientConfig.RETRIES_CONFIG));
        assertEquals(ADMIN_RETRY_BACKOFF, adminMap.get(AdminClientConfig.RETRY_BACKOFF_MS_CONFIG));
        assertEquals(ADMIN_RECONNECT_BACKOFF, adminMap.get(AdminClientConfig.RECONNECT_BACKOFF_MS_CONFIG));
        assertEquals(ADMIN_RECONNECT_BACKOFF_MAX, adminMap.get(AdminClientConfig.RECONNECT_BACKOFF_MAX_MS_CONFIG));
        assertEquals(ADMIN_REQUEST_TIMEOUT, adminMap.get(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG));
    }

    @Test
    void getApProducerProperties() {

        final Map<String, Object> producerMap = this.commonKafkaCustomProperties.getApProducerProperties();

        assertEquals(13, producerMap.size());
        assertEquals(BOOTSTRAP_SERVERS, producerMap.get(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG));
        assertEquals(true, producerMap.get(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG));
        assertEquals(1, producerMap.get(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION));
        assertEquals(PRODUCER_BATCH_SIZE, producerMap.get(ProducerConfig.BATCH_SIZE_CONFIG));
        assertEquals(PRODUCER_LINGER, producerMap.get(ProducerConfig.LINGER_MS_CONFIG));
        assertEquals(PRODUCER_BUFFER_MEMORY, producerMap.get(ProducerConfig.BUFFER_MEMORY_CONFIG));
        assertEquals(ADMIN_RETRY, producerMap.get(ProducerConfig.RETRIES_CONFIG));
        assertEquals(PRODUCER_RETRY_BACKOFF, producerMap.get(ProducerConfig.RETRY_BACKOFF_MS_CONFIG));
        assertEquals(PRODUCER_RECONNECT_BACKOFF, producerMap.get(ProducerConfig.RECONNECT_BACKOFF_MS_CONFIG));
        assertEquals(PRODUCER_RECONNECT_BACKOFF_MAX, producerMap.get(ProducerConfig.RECONNECT_BACKOFF_MAX_MS_CONFIG));
        assertEquals(APPRODUCER_REQUEST_TIMEOUT, producerMap.get(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG));
        assertEquals(false, producerMap.get(AbstractKafkaSchemaSerDeConfig.AUTO_REGISTER_SCHEMAS));
        assertEquals(TEST_URL, producerMap.get(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG));
    }

    @Test
    void getApConsumerProperties() {

        final Map<String, Object> consumerMap = this.commonKafkaCustomProperties.getApConsumerProperties();

        assertEquals(7, consumerMap.size());
        assertEquals(BOOTSTRAP_SERVERS, consumerMap.get(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG));
        assertEquals(false, consumerMap.get(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG));
        assertEquals(APCONSUMER_MAX_POLL_RECORDS, consumerMap.get(ConsumerConfig.MAX_POLL_RECORDS_CONFIG));
        assertEquals(APCONSUMER_MAX_POLL_INTERVAL, consumerMap.get(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG));
        assertEquals(TEST_URL, consumerMap.get(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG));
        assertEquals(StringDeserializer.class, consumerMap.get(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG));
        assertEquals(KafkaAvroDeserializer.class, consumerMap.get(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG));
    }
}
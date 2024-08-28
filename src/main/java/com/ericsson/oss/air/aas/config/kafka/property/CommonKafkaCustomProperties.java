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

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.ericsson.oss.air.aas.config.kafka.APKafkaProperties;
import com.ericsson.oss.air.aas.config.kafka.KafkaProperties;
import com.ericsson.oss.air.util.CustomAvroDeserializer;
import com.ericsson.oss.air.util.CustomAvroSerializer;

import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * Contains common customization for Kafka client properties in AAS
 */
@Configuration
@EnableConfigurationProperties({ KafkaProperties.class, APKafkaProperties.class })
@RequiredArgsConstructor
public class CommonKafkaCustomProperties {

    private final KafkaProperties kafkaProperties;

    private final APKafkaProperties apKafkaProperties;

    @Setter(AccessLevel.PACKAGE) // intended only for unit tests
    @Value("${dmm.schemaRegistry.url}")
    private String schemaRegistryUrl;

    /**
     * Returns a map to configure the Kafka consumer clients for configuration notifications
     *
     * @return a map with consumer properties
     */
    public Map<String, Object> getConsumerProperties() {

        final Map<String, Object> props = new HashMap<>(13);
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, CustomAvroDeserializer.class);
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, kafkaProperties.getRegistrationNotification().getConsumer().getMaxPollRecords());
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, kafkaProperties.getRegistrationNotification().getConsumer().getMaxPollIntervalMs());

        return props;
    }

    /**
     * Returns a map to configure the Kafka producer clients for configuration notifications
     *
     * @return a map with producer properties
     */
    public Map<String, Object> getProducerProperties() {

        final Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, CustomAvroSerializer.class);
        config.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, kafkaProperties.getRegistrationNotification().getProducer().getRequestTimeoutMs());

        return config;
    }

    /**
     * Returns a map to configure the Kafka admin client
     *
     * @return a map with kafka admin properties
     */
    public Map<String, Object> getAdminProperties() {

        final Map<String, Object> properties = new HashMap<>();
        properties.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        properties.put(AdminClientConfig.RETRIES_CONFIG, kafkaProperties.getAdmin().getRetry());
        properties.put(AdminClientConfig.RETRY_BACKOFF_MS_CONFIG, kafkaProperties.getAdmin().getRetryBackoffMs());
        properties.put(AdminClientConfig.RECONNECT_BACKOFF_MS_CONFIG, kafkaProperties.getAdmin().getReconnectBackoffMs());
        properties.put(AdminClientConfig.RECONNECT_BACKOFF_MAX_MS_CONFIG, kafkaProperties.getAdmin().getReconnectBackoffMaxMs());
        properties.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, kafkaProperties.getAdmin().getRequestTimeoutMs());

        return properties;
    }

    /**
     * Returns a map to configure augmentation processing kafka producers
     *
     * @return a map with producer properties
     */
    public Map<String, Object> getApProducerProperties() {

        final Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        config.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        // max in flight requests per connection set to one prevents pipelining (and message re-ordering),
        config.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 1); // transaction rollbacks will also cause message re-ordering too.
        // batch.size, linger.ms & buffer.memory can be configured to 'batch' messages together for sending.
        config.put(ProducerConfig.BATCH_SIZE_CONFIG, apKafkaProperties.getAugmentationProcessing().getProducer().getBatchSize()); //default 16384
        config.put(ProducerConfig.LINGER_MS_CONFIG, apKafkaProperties.getAugmentationProcessing().getProducer().getLinger()); //default 0
        config.put(ProducerConfig.BUFFER_MEMORY_CONFIG,
                   apKafkaProperties.getAugmentationProcessing().getProducer().getBufferMemory()); //default 33554432
        //retries must be non-zero for transactions to work. Defaults to 2147483647 in logs when transactions enabled.
        config.put(ProducerConfig.RETRIES_CONFIG, kafkaProperties.getAdmin().getRetry()); // default to 0 (in kafka non transactions).
        config.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, apKafkaProperties.getAugmentationProcessing().getProducer().getRetryBackoffMs());
        config.put(ProducerConfig.RECONNECT_BACKOFF_MS_CONFIG, apKafkaProperties.getAugmentationProcessing().getProducer().getReconnectBackoffMs());
        config.put(ProducerConfig.RECONNECT_BACKOFF_MAX_MS_CONFIG,
                   apKafkaProperties.getAugmentationProcessing().getProducer().getReconnectBackoffMaxMs());
        config.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, apKafkaProperties.getAugmentationProcessing().getProducer().getRequestTimeoutMs());
        config.put(AbstractKafkaSchemaSerDeConfig.AUTO_REGISTER_SCHEMAS, false);
        config.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, this.schemaRegistryUrl);
        return config;
    }

    /**
     * Returns a map to configure augmentation processing kafka consumers
     *
     * @return a map with consumer properties
     */
    public Map<String, Object> getApConsumerProperties() {

        final Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, this.kafkaProperties.getBootstrapServers());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);
        props.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, false);
        props.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, this.schemaRegistryUrl);
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, this.apKafkaProperties.getAugmentationProcessing().getConsumer().getMaxPollRecords());
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG,
                  this.apKafkaProperties.getAugmentationProcessing().getConsumer().getMaxPollIntervalMs());
        return props;
    }
}

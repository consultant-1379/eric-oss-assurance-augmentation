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

package com.ericsson.oss.air.producer;

import java.util.HashMap;
import java.util.Map;

import com.ericsson.oss.air.util.CustomApKafkaAvroSerializer;
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;

/**
 * Configures kafka for Augmentation Processing
 */
@Configuration
@EnableConfigurationProperties({ KafkaProperties.class, SchemaRegistryProperties.class })
public class KafkaConfiguration {

    @Autowired
    private DynamicConfig dynamicConfig;

    @Autowired
    private KafkaProperties kafkaProperties;

    @Autowired
    private SchemaRegistryProperties schemaRegistryProperties;

    /**
     * Augmentation processing producer Kafka template bean to publish messages
     *
     * @return kafka template
     */
    @Bean(name = "kfTemplate")
    public KafkaTemplate<String, Object> apKafkaTemplate() {

        final Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, this.dynamicConfig.getKafkaServer());

        config.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, this.dynamicConfig.getSchemaRegistryUrl());
        config.put(AbstractKafkaSchemaSerDeConfig.AUTO_REGISTER_SCHEMAS, false);

        if (this.kafkaProperties.getTls().getEnabled()) {
            this.addSslConfig(config);
        }

        if (this.schemaRegistryProperties.getTls().getEnabled()) {
            this.addSslConfig("schema.registry.", config);
        }

        final DefaultKafkaProducerFactory<String, Object> factory = new DefaultKafkaProducerFactory<>(config, new StringSerializer(),
                new CustomApKafkaAvroSerializer(), true);
        return new KafkaTemplate<>(factory);
    }

    private void addSslConfig(final Map<String, Object> clientConfig) {
        this.addSslConfig("", clientConfig);
    }

    /*
     * (non-javadoc)
     *
     * Adds the necessary SSL configuration parameters and values to the provided Kafka client configuration.
     */
    private void addSslConfig(final String prefix, final Map<String, Object> clientConfig) {

        clientConfig.put(prefix + SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG, this.kafkaProperties.getTls().getTrustStoreType());
        clientConfig.put(prefix + SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, this.kafkaProperties.getTls().getTrustStoreLocation());
        clientConfig.put(prefix + SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, this.kafkaProperties.getTls().getTrustStorePassword());

        clientConfig.put(prefix + SslConfigs.SSL_KEYSTORE_TYPE_CONFIG, this.kafkaProperties.getTls().getKeyStoreType());
        clientConfig.put(prefix + SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, this.kafkaProperties.getTls().getKeyStoreLocation());
        clientConfig.put(prefix + SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, this.kafkaProperties.getTls().getKeyStorePassword());
        clientConfig.put(prefix + SslConfigs.SSL_KEY_PASSWORD_CONFIG, this.kafkaProperties.getTls().getKeyStorePassword());

        clientConfig.put(prefix + AdminClientConfig.SECURITY_PROTOCOL_CONFIG, "SSL");
    }

}

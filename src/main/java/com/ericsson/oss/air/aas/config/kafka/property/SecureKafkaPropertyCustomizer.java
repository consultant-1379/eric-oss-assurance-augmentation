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

import static com.ericsson.oss.air.aas.config.security.CertificateIdEnum.KAFKA;
import static com.ericsson.oss.air.aas.config.security.CertificateIdEnum.ROOTCA;
import static com.ericsson.oss.air.aas.config.security.CertificateIdEnum.SERVER;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.logging.log4j.util.Strings;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Configuration;

import com.ericsson.oss.air.aas.config.security.AdpCertificateDiscoveryProperties;
import com.ericsson.oss.air.aas.config.security.TlsConfiguration;

import lombok.RequiredArgsConstructor;

/**
 * Secure implementation of {@link KafkaPropertyCustomizer}. It sets up the required SSL properties for Kafka.
 */
@Configuration
@RequiredArgsConstructor
@ConditionalOnBean(TlsConfiguration.class)
public class SecureKafkaPropertyCustomizer implements KafkaPropertyCustomizer {

    private final CommonKafkaCustomProperties commonKafkaCustomProperties;

    private final AdpCertificateDiscoveryProperties discoveryProperties;

    private static final String KEYSTORE_TYPE = "PKCS12";

    private static final String KEYSTORE_FILE = "keystore.p12";

    private static final String TRUSTSTORE_FILE = "truststore.p12";

    private static final String SECURITY_PROTOCOL = "SSL";

    private static final String SCHEMA_REGISTRY_PREFIX = "schema.registry.";

    @Override
    public Map<String, Object> kafkaConsumerProperties() {
        return this.updateKafkaClientConfigWithSsl(this.commonKafkaCustomProperties.getConsumerProperties());
    }

    @Override
    public Map<String, Object> kafkaProducerProperties() {
        return this.updateKafkaClientConfigWithSsl(this.commonKafkaCustomProperties.getProducerProperties());
    }

    @Override
    public Map<String, Object> kafkaAdminProperties() {
        return this.updateKafkaClientConfigWithSsl(this.commonKafkaCustomProperties.getAdminProperties());
    }

    @Override
    public Map<String, Object> apKafkaProducerProperties() {

        final Map<String, Object> kafkaProducerConfig = this.commonKafkaCustomProperties.getApProducerProperties();
        kafkaProducerConfig.putAll(this.getSchemaRegistrySslConfig());

        return this.updateKafkaClientConfigWithSsl(kafkaProducerConfig);
    }

    @Override
    public Map<String, Object> apKafkaConsumerProperties() {

        final Map<String, Object> kafkaConsumerConfig = this.commonKafkaCustomProperties.getApConsumerProperties();
        kafkaConsumerConfig.putAll(this.getSchemaRegistrySslConfig());

        return this.updateKafkaClientConfigWithSsl(kafkaConsumerConfig);
    }

    /*
     * Gets the required SSL config for connecting to the Schema Registry for augmentation processing Kafka clients.
     */
    private Map<String, Object> getSchemaRegistrySslConfig() {

        final Map<String, Object> schemaRegistrySslConfig = new HashMap<>();

        final String keyStorePath = Paths.get(discoveryProperties.getRootWritePath(),
                                              SERVER.getId(), KEYSTORE_FILE)
                .normalize().toAbsolutePath().toString();

        return updateClientConfigWithSsl(schemaRegistrySslConfig, SCHEMA_REGISTRY_PREFIX, keyStorePath);
    }

    /*
     * Updates the provided map with the required SSL config for connecting to the Kafka securely.
     */
    private Map<String, Object> updateKafkaClientConfigWithSsl(final Map<String, Object> clientConfig) {

        final String kafkaKeyStorePath = Paths.get(discoveryProperties.getRootWritePath(),
                                                   KAFKA.getId(), KEYSTORE_FILE)
                .normalize().toAbsolutePath().toString();

        return updateClientConfigWithSsl(clientConfig, Strings.EMPTY, kafkaKeyStorePath);
    }

    /*
     * Updates existing client configuration in order to communicate over mTLS and returns updated configuration.
     */
    private Map<String, Object> updateClientConfigWithSsl(final Map<String, Object> clientConfig,
                                                          final String prefix,
                                                          final String keyStorePath) {

        final String trustStorePath = Paths.get(discoveryProperties.getRootWritePath(),
                                                ROOTCA.getId(), TRUSTSTORE_FILE)
                .normalize().toAbsolutePath().toString();

        clientConfig.put(prefix + SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG, KEYSTORE_TYPE);
        clientConfig.put(prefix + SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, trustStorePath);
        clientConfig.put(prefix + SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, this.discoveryProperties.getPassword());

        clientConfig.put(prefix + SslConfigs.SSL_KEYSTORE_TYPE_CONFIG, KEYSTORE_TYPE);
        clientConfig.put(prefix + SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, keyStorePath);
        clientConfig.put(prefix + SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, this.discoveryProperties.getPassword());
        clientConfig.put(prefix + SslConfigs.SSL_KEY_PASSWORD_CONFIG, this.discoveryProperties.getKeyPassword());

        clientConfig.put(AdminClientConfig.SECURITY_PROTOCOL_CONFIG, SECURITY_PROTOCOL);

        return clientConfig;
    }
}

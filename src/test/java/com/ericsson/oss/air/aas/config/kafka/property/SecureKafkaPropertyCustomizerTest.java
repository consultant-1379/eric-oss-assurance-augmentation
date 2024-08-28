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
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.logging.log4j.util.Strings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ericsson.oss.air.aas.config.security.AdpCertificateDiscoveryProperties;

@ExtendWith(MockitoExtension.class)
class SecureKafkaPropertyCustomizerTest {

    private static final String ROOT_READ_PATH = "/run/secrets";

    private static final String KEYSTORE_DIR = "keystore";

    private static final String TRUSTSTORE_DIR = "truststore";

    private static final String ROOT_WRITE_PATH = "/tmp";

    private static final String KEY_PASSWORD = "password1";

    private static final String PASSWORD = "password2";

    private static final String KEYSTORE_TYPE = "PKCS12";

    private static final String KAFKA_KEYSTORE_PATH = ROOT_WRITE_PATH + "/kafka/keystore.p12";

    private static final String SCHEMA_REGISTRY_KEYSTORE_PATH = ROOT_WRITE_PATH + "/server/keystore.p12";

    private static final String TRUSTSTORE_PATH = ROOT_WRITE_PATH + "/rootca/truststore.p12";

    private static final AdpCertificateDiscoveryProperties DISCOVERY_PROPERTIES = new AdpCertificateDiscoveryProperties(ROOT_READ_PATH,
                                                                                                                        KEYSTORE_DIR,
                                                                                                                        TRUSTSTORE_DIR,
                                                                                                                        ROOT_WRITE_PATH,
                                                                                                                        KEY_PASSWORD,
                                                                                                                        PASSWORD);

    private static final String TEST_KEY = "key1";

    private static final String TEST_VALUE = "value1";

    @Mock
    private CommonKafkaCustomProperties commonConfiguration;

    private SecureKafkaPropertyCustomizer clientConfiguration;

    private Map<String, Object> testConfig;

    @BeforeEach
    void setup() {
        this.clientConfiguration = new SecureKafkaPropertyCustomizer(this.commonConfiguration, DISCOVERY_PROPERTIES);
        this.testConfig = new HashMap<>();
        this.testConfig.put(TEST_KEY, TEST_VALUE);
    }

    @Test
    void kafkaConsumerProperties() {
        when(this.commonConfiguration.getConsumerProperties()).thenReturn(this.testConfig);

        final Map<String, Object> actualMap = this.clientConfiguration.kafkaConsumerProperties();

        assertUpdatedConfig(actualMap);
    }

    @Test
    void kafkaProducerProperties() {
        when(this.commonConfiguration.getProducerProperties()).thenReturn(this.testConfig);

        final Map<String, Object> actualMap = this.clientConfiguration.kafkaProducerProperties();

        assertUpdatedConfig(actualMap);
    }

    @Test
    void kafkaAdminProperties() {
        when(this.commonConfiguration.getAdminProperties()).thenReturn(this.testConfig);

        final Map<String, Object> actualMap = this.clientConfiguration.kafkaAdminProperties();

        assertUpdatedConfig(actualMap);
    }

    @Test
    void apKafkaProducerProperties() {
        when(this.commonConfiguration.getApProducerProperties()).thenReturn(this.testConfig);

        final Map<String, Object> actualMap = this.clientConfiguration.apKafkaProducerProperties();

        assertApUpdatedConfig(actualMap);
    }

    @Test
    void apKafkaConsumerProperties() {
        when(this.commonConfiguration.getApConsumerProperties()).thenReturn(this.testConfig);

        final Map<String, Object> actualMap = this.clientConfiguration.apKafkaConsumerProperties();

        assertApUpdatedConfig(actualMap);
    }

    private static void assertUpdatedConfig(final Map<String, Object> actualMap) {

        assertEquals(9, actualMap.size());
        assertSslConfig(Strings.EMPTY, actualMap);
    }

    private static void assertApUpdatedConfig(final Map<String, Object> actualMap) {

        assertEquals(16, actualMap.size());
        assertSslConfig(Strings.EMPTY, actualMap);
        assertSslConfig("schema.registry.", actualMap);
    }

    private static void assertSslConfig(final String prefix, final Map<String, Object> actualMap) {
        assertEquals(KEYSTORE_TYPE, actualMap.get(prefix + SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG));
        assertEquals(TRUSTSTORE_PATH, actualMap.get(prefix + SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG));
        assertEquals(PASSWORD, actualMap.get(prefix + SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG));
        assertEquals(KEYSTORE_TYPE, actualMap.get(prefix + SslConfigs.SSL_KEYSTORE_TYPE_CONFIG));
        assertEquals(ObjectUtils.isEmpty(prefix) ? KAFKA_KEYSTORE_PATH : SCHEMA_REGISTRY_KEYSTORE_PATH,
                     actualMap.get(prefix + SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG));
        assertEquals(PASSWORD, actualMap.get(prefix + SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG));
        assertEquals(KEY_PASSWORD, actualMap.get(prefix + SslConfigs.SSL_KEY_PASSWORD_CONFIG));
        assertEquals("SSL", actualMap.get(AdminClientConfig.SECURITY_PROTOCOL_CONFIG));
        assertEquals(TEST_VALUE, actualMap.get(TEST_KEY));
    }
}
/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.aas.config.security.kafka;

import static com.ericsson.oss.air.util.logging.TestLoggingUtils.assertAuditEvent;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.ericsson.adp.security.certm.certificatewatcher.KeyStoreItem;
import com.ericsson.adp.security.certm.certificatewatcher.TlsContext;
import com.ericsson.adp.security.certm.certificatewatcher.TrustStoreItem;
import com.ericsson.oss.air.aas.config.security.AdpCertificateDiscoveryProperties;
import com.ericsson.oss.air.aas.config.security.CertificateIdEnum;
import com.ericsson.oss.air.aas.model.security.KafkaSecurityMaterial;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

@ExtendWith(MockitoExtension.class)
class KafkaSecurityArtifactCombinatorTest {

    private static final String ROOT_READ_PATH = "/opt/test/path";

    private static final String KEYSTORE_REL_DIR = "keystore";

    private static final String TRUSTSTORE_REL_DIR = "truststore";

    private static final String READ_KAFKA_KEYSTORE_PATH = String.join("/", ROOT_READ_PATH, CertificateIdEnum.KAFKA.getId(), KEYSTORE_REL_DIR);

    private static final String READ_SERVER_KEYSTORE_PATH = String.join("/", ROOT_READ_PATH, CertificateIdEnum.SERVER.getId(), KEYSTORE_REL_DIR);

    private static final String READ_TRUSTSTORE_PATH = String.join("/", ROOT_READ_PATH, CertificateIdEnum.ROOTCA.getId(), TRUSTSTORE_REL_DIR);

    private static final String LOG_MSG_PREFIX = "Missing/invalid certificates in ";

    private static final KeyStoreItem TEST_KEYSTORE_ITEM = KeyStoreItem.builder().build();

    private static final TrustStoreItem TEST_TRUSTSTORE_ITEM = TrustStoreItem.builder().build();

    private static final KafkaSecurityMaterial TEST_KAFKA_SECURITY_MATERIAL = KafkaSecurityMaterial.builder()
            .kafkaKeyStoreItem(TEST_KEYSTORE_ITEM)
            .serverKeyStoreItem(TEST_KEYSTORE_ITEM)
            .trustStoreItem(TEST_TRUSTSTORE_ITEM)
            .build();

    private static final TlsContext EMPTY_TLS_CONTEXT = TlsContext.builder().build();

    @InjectMocks
    private KafkaSecurityArtifactCombinator kafkaSecurityArtifactCombinator;

    @Mock
    private KeyStoreItem mockKeyStoreItem;

    @Mock
    private TrustStoreItem mockTrustStoreItem;

    @Mock
    private AdpCertificateDiscoveryProperties adpCertificateDiscoveryProperties;

    private ListAppender<ILoggingEvent> listAppender;

    private TlsContext kafkaKeyStoreTlsContext;

    private TlsContext serverKeyStoreTlsContext;

    private TlsContext trustStoreTlsContext;

    @BeforeEach
    void setup() {
        final Logger logger = (Logger) LoggerFactory.getLogger(KafkaSecurityArtifactCombinator.class);
        logger.setLevel(Level.INFO);
        this.listAppender = new ListAppender<>();
        this.listAppender.start();
        logger.addAppender(listAppender);

        this.kafkaKeyStoreTlsContext = TlsContext.builder().name("kafka").keyStore(this.mockKeyStoreItem).build();
        this.serverKeyStoreTlsContext = TlsContext.builder().name("server").keyStore(TEST_KEYSTORE_ITEM).build();
        this.trustStoreTlsContext = TlsContext.builder().name("rootca").trustStore(this.mockTrustStoreItem).build();
    }

    @Test
    void combine_RequiredTlsContextsPresent() {

        final KafkaSecurityMaterial expectedKafkaSecurityMaterial = KafkaSecurityMaterial.builder()
                .kafkaKeyStoreItem(this.mockKeyStoreItem)
                .serverKeyStoreItem(TEST_KEYSTORE_ITEM)
                .trustStoreItem(this.mockTrustStoreItem)
                .build();

        final Optional<KafkaSecurityMaterial> actualKafkaSecurityMaterial = this.kafkaSecurityArtifactCombinator.combine(this.kafkaKeyStoreTlsContext,
                this.trustStoreTlsContext,
                this.serverKeyStoreTlsContext);

        assertTrue(actualKafkaSecurityMaterial.isPresent());
        assertEquals(expectedKafkaSecurityMaterial, actualKafkaSecurityMaterial.get());
    }

    @Test
    void combine_NotPopulatedThreeTlsContexts() {

        assertEquals(Optional.empty(), this.kafkaSecurityArtifactCombinator.combine(this.kafkaKeyStoreTlsContext));
        assertEquals(Optional.empty(), this.kafkaSecurityArtifactCombinator.combine(this.kafkaKeyStoreTlsContext, this.trustStoreTlsContext));
        assertEquals(Optional.empty(), this.kafkaSecurityArtifactCombinator.combine(null));
    }

    @Test
    void combine_NoKafkaKeyStore() {

        when(this.adpCertificateDiscoveryProperties.getRootReadPath()).thenReturn(ROOT_READ_PATH);
        when(this.adpCertificateDiscoveryProperties.getKeystoreRelativeDir()).thenReturn(KEYSTORE_REL_DIR);

        final Optional<KafkaSecurityMaterial> material = this.kafkaSecurityArtifactCombinator.combine(EMPTY_TLS_CONTEXT,
                this.trustStoreTlsContext,
                this.serverKeyStoreTlsContext);

        assertTrue(material.isEmpty());

        final List<ILoggingEvent> loggingEventList = this.listAppender.list;

        assertEquals(1, loggingEventList.size());
        assertAuditEvent(loggingEventList.get(0), Level.WARN, LOG_MSG_PREFIX + READ_KAFKA_KEYSTORE_PATH);
    }

    @Test
    void combine_NoTrustStore() {

        when(this.adpCertificateDiscoveryProperties.getRootReadPath()).thenReturn(ROOT_READ_PATH);
        when(this.adpCertificateDiscoveryProperties.getTruststoreRelativeDir()).thenReturn(TRUSTSTORE_REL_DIR);

        final Optional<KafkaSecurityMaterial> material = this.kafkaSecurityArtifactCombinator.combine(this.kafkaKeyStoreTlsContext,
                EMPTY_TLS_CONTEXT,
                this.serverKeyStoreTlsContext);

        assertTrue(material.isEmpty());

        final List<ILoggingEvent> loggingEventList = this.listAppender.list;

        assertEquals(1, loggingEventList.size());
        assertAuditEvent(loggingEventList.get(0), Level.WARN, LOG_MSG_PREFIX + READ_TRUSTSTORE_PATH);
    }

    @Test
    void combine_NoServerKeyStore() {

        when(this.adpCertificateDiscoveryProperties.getRootReadPath()).thenReturn(ROOT_READ_PATH);
        when(this.adpCertificateDiscoveryProperties.getKeystoreRelativeDir()).thenReturn(KEYSTORE_REL_DIR);

        final Optional<KafkaSecurityMaterial> material = this.kafkaSecurityArtifactCombinator.combine(this.kafkaKeyStoreTlsContext,
                this.trustStoreTlsContext,
                EMPTY_TLS_CONTEXT);

        assertTrue(material.isEmpty());

        final List<ILoggingEvent> loggingEventList = this.listAppender.list;

        assertEquals(1, loggingEventList.size());
        assertAuditEvent(loggingEventList.get(0), Level.WARN, LOG_MSG_PREFIX + READ_SERVER_KEYSTORE_PATH);
    }

    @Test
    void combine_MissingAllStores() {

        when(this.adpCertificateDiscoveryProperties.getRootReadPath()).thenReturn(ROOT_READ_PATH);
        when(this.adpCertificateDiscoveryProperties.getKeystoreRelativeDir()).thenReturn(KEYSTORE_REL_DIR);
        when(this.adpCertificateDiscoveryProperties.getTruststoreRelativeDir()).thenReturn(TRUSTSTORE_REL_DIR);

        final Optional<KafkaSecurityMaterial> material = this.kafkaSecurityArtifactCombinator.combine(EMPTY_TLS_CONTEXT,
                EMPTY_TLS_CONTEXT,
                EMPTY_TLS_CONTEXT);

        assertTrue(material.isEmpty());

        final List<ILoggingEvent> loggingEventList = this.listAppender.list;

        assertEquals(1, loggingEventList.size());
        assertAuditEvent(loggingEventList.get(0), Level.WARN, LOG_MSG_PREFIX + String.join(",", READ_KAFKA_KEYSTORE_PATH, READ_SERVER_KEYSTORE_PATH,
                READ_TRUSTSTORE_PATH));
    }
}
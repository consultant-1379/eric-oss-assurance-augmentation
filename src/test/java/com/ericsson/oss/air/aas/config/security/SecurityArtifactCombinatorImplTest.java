/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.aas.config.security;

import static com.ericsson.oss.air.util.logging.TestLoggingUtils.EXCEPTION_MSG;
import static com.ericsson.oss.air.util.logging.TestLoggingUtils.assertAuditEvent;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.net.ssl.SSLContext;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.ericsson.adp.security.certm.certificatewatcher.KeyStoreItem;
import com.ericsson.adp.security.certm.certificatewatcher.TlsContext;
import com.ericsson.adp.security.certm.certificatewatcher.TrustStoreItem;
import lombok.SneakyThrows;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

@ExtendWith(MockitoExtension.class)
class SecurityArtifactCombinatorImplTest {

    @Mock
    private KeyStoreItem mockKeyStoreItem;

    @Mock
    private TrustStoreItem mockTrustStoreItem;

    @Mock
    private KeyStore mockKeyStore;

    @Mock
    private X509Certificate mockX509Certificate;

    @Mock
    private SSLContextBuilder mockSslContextBuilder;

    @InjectMocks
    private SecurityArtifactCombinatorImpl securityArtifactCombinator;

    private Logger log;

    private ListAppender<ILoggingEvent> listAppender;

    @BeforeEach
    public void setUp() {

        this.log = (Logger) LoggerFactory.getLogger(SecurityArtifactCombinatorImpl.class);
        this.log.setLevel(Level.INFO);

        this.listAppender = new ListAppender<>();
        this.listAppender.start();

        this.log.addAppender(this.listAppender);
    }

    @AfterEach
    public void tearDown() throws Exception {
        this.listAppender.stop();
    }

    @Test
    void combine_OneTlsContext_ReturnsSslContext() {

        final KeyStore keyStore = createKeyStore();
        when(this.mockKeyStoreItem.getKeyStore()).thenReturn(keyStore);

        final KeyStore trustStore = createKeyStore();
        when(this.mockTrustStoreItem.getTrustStore()).thenReturn(trustStore);

        final TlsContext fullTlsContext = new TlsContext("testName", this.mockKeyStoreItem, this.mockTrustStoreItem);

        final Optional<SSLContext> sslContextOptional = this.securityArtifactCombinator.combine(fullTlsContext);

        assertTrue(sslContextOptional.isPresent());
    }

    @Test
    void combine_MultipleTlsContextsIncludingANull_ReturnsSslContext() {

        final KeyStore keyStore = createKeyStore();
        when(this.mockKeyStoreItem.getKeyStore()).thenReturn(keyStore);

        final KeyStore trustStore = createKeyStore();
        when(this.mockTrustStoreItem.getTrustStore()).thenReturn(trustStore);

        final TlsContext emptyTlsContext = TlsContext.builder().build();
        final TlsContext keyStoreTlsContext = new TlsContext("testNameA", this.mockKeyStoreItem, null);
        final TlsContext trustStoreTlsContext = new TlsContext("testNameB", null, this.mockTrustStoreItem);

        final Optional<SSLContext> sslContextOptional = this.securityArtifactCombinator.combine(emptyTlsContext, keyStoreTlsContext,
                trustStoreTlsContext, null);

        assertTrue(sslContextOptional.isPresent());
    }

    @Test
    void combine_NullTlsContexts_ReturnsEmptySslContext() {
        assertTrue(this.securityArtifactCombinator.combine(null).isEmpty());
    }

    @Test
    void combine_EmptyTlsContexts_ReturnsEmptySslContext() {
        assertTrue(this.securityArtifactCombinator.combine(new TlsContext[] {}).isEmpty());
    }

    @Test
    void combine_NoContentTlsContexts_ReturnsEmptySslContext() {
        assertTrue(this.securityArtifactCombinator.combine(new TlsContext[] { TlsContext.builder().build() }).isEmpty());
    }

    @Test
    void combine_CannotCreateCombinedKeyStore_ReturnsEmptySslContext() {
        final SecurityArtifactCombinatorImpl testCombinator = new SecurityArtifactCombinatorImpl();
        testCombinator.setKeyStoreType("foobar");

        final Optional<SSLContext> sslContextOptional = testCombinator.combine(TlsContext.builder().keyStore(this.mockKeyStoreItem).build());

        assertTrue(sslContextOptional.isEmpty());

        final List<ILoggingEvent> loggingEventList = this.listAppender.list;

        assertFalse(loggingEventList.isEmpty());
        assertEquals(2, loggingEventList.size());

        // There are 2 audit events. One for failure to create AAS truststore and another for the keystore
        for (final ILoggingEvent loggingEvent : loggingEventList) {
            assertAuditEvent(loggingEvent, Level.WARN, "Cannot create AAS' keystore/truststore: ");
            assertEquals(KeyStoreException.class.getName(), loggingEvent.getThrowableProxy().getClassName());
        }
    }

    @Test
    void combine_CannotCreateSslContext_ReturnsEmptySslContext() throws NoSuchAlgorithmException, KeyStoreException {

        final KeyStoreException exception = new KeyStoreException(EXCEPTION_MSG);

        when(this.mockSslContextBuilder.loadTrustMaterial((KeyStore) any(), any())).thenThrow(exception);
        final SecurityArtifactCombinatorImpl testCombinator = new SecurityArtifactCombinatorImpl() {

            @Override
            SSLContextBuilder getSslContextBuilder() {
                return mockSslContextBuilder;
            }
        };

        final Optional<SSLContext> sslContextOptional = testCombinator.combine(TlsContext.builder().keyStore(this.mockKeyStoreItem).build());

        assertTrue(sslContextOptional.isEmpty());

        final List<ILoggingEvent> loggingEventList = this.listAppender.list;

        assertFalse(loggingEventList.isEmpty());
        assertEquals(1, loggingEventList.size());

        assertAuditEvent(loggingEventList.get(0), Level.WARN, "Cannot create SSL context: ", exception);
    }

    @Test
    void combine_addTrustCertToCombinedTrustStore_KeyStoreException_CatchesException() throws KeyStoreException {

        final KeyStoreException exception = new KeyStoreException(EXCEPTION_MSG);

        when(this.mockKeyStore.aliases()).thenReturn(Collections.enumeration(List.of("testAlias")));
        when(this.mockKeyStore.getCertificate("testAlias")).thenThrow(exception);
        when(this.mockTrustStoreItem.getTrustStore()).thenReturn(this.mockKeyStore);

        final TlsContext tlsContext = new TlsContext("testName", null, this.mockTrustStoreItem);

        final Optional<SSLContext> sslContextOptional = this.securityArtifactCombinator.combine(tlsContext);

        assertTrue(sslContextOptional.isEmpty());

        final List<ILoggingEvent> loggingEventList = this.listAppender.list;

        assertFalse(loggingEventList.isEmpty());
        assertEquals(1, loggingEventList.size());

        assertAuditEvent(loggingEventList.get(0), Level.WARN, "Cannot create SSL context: ", exception);
    }

    @Test
    void combine_addClientCertToCombinedKeyStore_KeyStoreException_CatchesException()
            throws KeyStoreException, UnrecoverableKeyException, NoSuchAlgorithmException {

        final KeyStoreException exception = new KeyStoreException(EXCEPTION_MSG);

        when(this.mockKeyStoreItem.getKeyStore()).thenReturn(this.mockKeyStore);
        when(this.mockKeyStoreItem.getKeyPassword()).thenReturn("password");
        when(this.mockKeyStore.aliases()).thenReturn(Collections.enumeration(List.of("testAlias")));
        when(this.mockKeyStore.getKey(any(), any())).thenThrow(exception);

        final TlsContext tlsContext = new TlsContext("testName", this.mockKeyStoreItem, null);

        final Optional<SSLContext> sslContextOptional = this.securityArtifactCombinator.combine(tlsContext);

        assertTrue(sslContextOptional.isEmpty());

        final List<ILoggingEvent> loggingEventList = this.listAppender.list;

        assertFalse(loggingEventList.isEmpty());
        assertEquals(1, loggingEventList.size());

        assertAuditEvent(loggingEventList.get(0), Level.WARN, "Cannot create SSL context: ", exception);
    }

    @Test
    void addTrustCertToCombinedTrustStore_Valid() throws KeyStoreException {

        final KeyStore combinedTrustStore = createKeyStore();
        final KeyStore trustStore = createKeyStore();
        trustStore.setCertificateEntry("testAlias", this.mockX509Certificate);
        trustStore.setCertificateEntry("testAlias2", this.mockX509Certificate);

        this.securityArtifactCombinator.addTrustCertToCombinedTrustStore(combinedTrustStore, trustStore);

        assertNotNull(combinedTrustStore);
        assertEquals(2, Collections.list(combinedTrustStore.aliases()).size());
    }

    @Test
    void addTrustCertToCombinedTrustStore_NullCombinedTrustStore_Valid() throws KeyStoreException {

        final KeyStore combinedTrustStore = createKeyStore();

        this.securityArtifactCombinator.addTrustCertToCombinedTrustStore(null, null);

        assertNotNull(combinedTrustStore);
        assertTrue(Collections.list(combinedTrustStore.aliases()).isEmpty());
    }

    @Test
    void addTrustCertToCombinedTrustStore_NullCaTrustStore_Valid() throws KeyStoreException {

        final KeyStore combinedTrustStore = createKeyStore();

        this.securityArtifactCombinator.addTrustCertToCombinedTrustStore(combinedTrustStore, null);

        assertNotNull(combinedTrustStore);
        assertTrue(Collections.list(combinedTrustStore.aliases()).isEmpty());
    }

    @Test
    void addClientCertToCombinedKeyStore_Valid() throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException {

        final KeyStore combinedKeyStore = createKeyStore();
        final KeyStore clientKeyStore = createKeyStore();
        final KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        clientKeyStore.setKeyEntry("testAlias", keyPairGenerator.generateKeyPair().getPrivate(), "password" .toCharArray(),
                new Certificate[] { this.mockX509Certificate });
        when(this.mockKeyStoreItem.getKeyStore()).thenReturn(clientKeyStore);
        when(this.mockKeyStoreItem.getKeyPassword()).thenReturn("password");
        when(this.mockKeyStoreItem.getPassword()).thenReturn("password");

        this.securityArtifactCombinator.addClientCertToCombinedKeyStore(combinedKeyStore, this.mockKeyStoreItem);

        assertNotNull(combinedKeyStore);
        assertEquals(1, Collections.list(combinedKeyStore.aliases()).size());
    }

    @Test
    void addClientCertToCombinedKeyStore_NullCombinedKeyStore_Valid() throws UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException {
        this.securityArtifactCombinator.addClientCertToCombinedKeyStore(null, this.mockKeyStoreItem);
    }

    @Test
    void addClientCertToCombinedKeyStore_NullKeyStore_Valid() throws KeyStoreException, UnrecoverableKeyException, NoSuchAlgorithmException {

        final KeyStore combinedKeyStore = createKeyStore();
        when(this.mockKeyStoreItem.getKeyStore()).thenReturn(null);

        this.securityArtifactCombinator.addClientCertToCombinedKeyStore(combinedKeyStore, this.mockKeyStoreItem);

        assertNotNull(combinedKeyStore);
        assertTrue(Collections.list(combinedKeyStore.aliases()).isEmpty());
    }

    @SneakyThrows
    private static KeyStore createKeyStore() {
        final KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(null);
        return keyStore;
    }
}
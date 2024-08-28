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

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.net.ssl.SSLContext;

import com.ericsson.adp.security.certm.certificatewatcher.KeyStoreItem;
import com.ericsson.adp.security.certm.certificatewatcher.TlsContext;
import com.ericsson.adp.security.certm.certificatewatcher.TrustStoreItem;
import com.ericsson.oss.air.util.logging.audit.AuditLogFactory;
import com.ericsson.oss.air.util.logging.audit.AuditLogger;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

/**
 * Default implementation of {@link SecurityArtifactCombinator}.
 */
@Component
@Slf4j
@NoArgsConstructor
@ConditionalOnBean(TlsConfiguration.class)
public class SecurityArtifactCombinatorImpl implements SecurityArtifactCombinator<TlsContext, SSLContext> {

    private static final AuditLogger AUDIT_LOGGER = AuditLogFactory.getLogger(SecurityArtifactCombinatorImpl.class);

    @Setter(AccessLevel.PACKAGE) // Setter used for unit testing
    private String keyStoreType = KeyStore.getDefaultType();

    // Used to create SSL Context
    private final AtomicReference<char[]> keyPassword = new AtomicReference<>();

    /**
     * Gets the SSL Context builder.
     *
     * @return the SSL Context builder.
     */
    SSLContextBuilder getSslContextBuilder() {
        return SSLContextBuilder.create();
    }

    /**
     * Combines the provided {@code TlsContext}'s into a {@code SSLContext}. If any error case is encountered or if all
     * the {@code TlsContext}'s are empty, then an empty {@code Optional} is returned.
     *
     * @param tlsContexts the security material to be combined into the resulting {@code SSLContext}
     * @return an {@code Optional} of {@code SSLContext}
     */
    @Override
    public Optional<SSLContext> combine(final TlsContext... tlsContexts) {

        final Predicate<TlsContext> hasNoCertificates = tlsContext ->
                tlsContext.getTrustStore().isEmpty() && tlsContext.getKeyStore().isEmpty();

        if (ObjectUtils.isEmpty(tlsContexts) || Arrays.stream(tlsContexts).allMatch(hasNoCertificates)) {
            log.debug("No certificates to put into the SSLContext");
            return Optional.empty();
        }

        final Optional<KeyStore> trustStoreOptional = this.createKeyStore();
        final Optional<KeyStore> keyStoreOptional = this.createKeyStore();

        if (trustStoreOptional.isEmpty() || keyStoreOptional.isEmpty()) {
            return Optional.empty();
        }

        try {
            this.aggregateSecurityMaterial(trustStoreOptional.get(), keyStoreOptional.get(), tlsContexts);

            final SSLContext generatedSslContext = this.getSslContextBuilder()
                    .loadTrustMaterial(trustStoreOptional.get(), null)
                    .loadKeyMaterial(keyStoreOptional.get(), this.keyPassword.get())
                    .build();

            // Reset password for next SSL Context creation
            this.keyPassword.set(null);

            return Optional.of(generatedSslContext);
        } catch (final NoSuchAlgorithmException | KeyStoreException | UnrecoverableKeyException | KeyManagementException e) {
            AUDIT_LOGGER.warn("Cannot create SSL context: ", e);
            return Optional.empty();
        }
    }

    private Optional<KeyStore> createKeyStore() {

        try {
            final KeyStore combinedKeyStore = KeyStore.getInstance(this.keyStoreType);
            combinedKeyStore.load(null);
            return Optional.of(combinedKeyStore);
        } catch (final KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException e) {
            AUDIT_LOGGER.warn("Cannot create AAS' keystore/truststore: ", e);
            return Optional.empty();
        }
    }

    private void aggregateSecurityMaterial(final KeyStore combinedTrustStore, final KeyStore combinedKeyStore, final TlsContext... tlsContexts)
            throws KeyStoreException, UnrecoverableKeyException, NoSuchAlgorithmException {

        for (final TlsContext tlsContext : Arrays.asList(tlsContexts).stream().filter(Objects::nonNull).collect(Collectors.toList())) {

            final Optional<TrustStoreItem> optionalTrustStoreItem = tlsContext.getTrustStore();
            if (optionalTrustStoreItem.isPresent()) {
                final TrustStoreItem trustStoreItem = optionalTrustStoreItem.get();
                addTrustCertToCombinedTrustStore(combinedTrustStore, trustStoreItem.getTrustStore());
            }

            final Optional<KeyStoreItem> optionalKeyStoreItem = tlsContext.getKeyStore();
            if (optionalKeyStoreItem.isPresent()) {
                final KeyStoreItem keyStoreItem = optionalKeyStoreItem.get();
                addClientCertToCombinedKeyStore(combinedKeyStore, keyStoreItem);
            }
        }
    }

    /**
     * Adds the trusted certificate entries from the provided {@code trustStore} to the combined truststore.
     *
     * @param combinedTrustStore AAS' truststore containing all its trusted certificates
     * @param trustStore         the truststore from the {@code TlsContext}
     * @throws KeyStoreException
     */
    void addTrustCertToCombinedTrustStore(final KeyStore combinedTrustStore, final KeyStore trustStore) throws KeyStoreException {

        if (Objects.isNull(combinedTrustStore) || Objects.isNull(trustStore)) {
            return;
        }

        for (final String alias : Collections.list(trustStore.aliases())) {
            combinedTrustStore.setCertificateEntry(alias, trustStore.getCertificate(alias));
            log.debug("Added certificate with alias: {} to AAS' truststore", alias);
        }
    }

    /**
     * Adds the private key and certificate entries from the provided {@code keyStoreItem} to the combined keystore.
     *
     * @param combinedKeyStore   AAS' keystore containing all its private key and certificate entries
     * @param clientKeyStoreItem container for the keystore from the {@code TlsContext}
     * @throws KeyStoreException
     * @throws UnrecoverableKeyException
     * @throws NoSuchAlgorithmException
     */
    void addClientCertToCombinedKeyStore(final KeyStore combinedKeyStore, final KeyStoreItem clientKeyStoreItem)
            throws KeyStoreException, UnrecoverableKeyException, NoSuchAlgorithmException {

        if (Objects.isNull(combinedKeyStore)) {
            return;
        }

        final KeyStore clientKeyStore = clientKeyStoreItem.getKeyStore();

        if (Objects.isNull(clientKeyStore)) {
            return;
        }

        for (final String alias : Collections.list(clientKeyStore.aliases())) {
            combinedKeyStore.setKeyEntry(alias, clientKeyStore.getKey(alias, clientKeyStoreItem.getKeyPassword().toCharArray()),
                    clientKeyStoreItem.getPassword().toCharArray(), clientKeyStore.getCertificateChain(alias));
            log.debug("Added private key entry with alias: {} to AAS' keystore", alias);

            // Sets the password needed to create the SSL Context
            this.keyPassword.compareAndSet(null, clientKeyStoreItem.getPassword().toCharArray());
        }
    }
}

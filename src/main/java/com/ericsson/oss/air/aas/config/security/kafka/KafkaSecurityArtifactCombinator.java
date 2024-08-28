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

import static com.ericsson.oss.air.aas.config.security.CertificateIdEnum.KAFKA;
import static com.ericsson.oss.air.aas.config.security.CertificateIdEnum.ROOTCA;
import static com.ericsson.oss.air.aas.config.security.CertificateIdEnum.SERVER;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.ericsson.adp.security.certm.certificatewatcher.KeyStoreItem;
import com.ericsson.adp.security.certm.certificatewatcher.TlsContext;
import com.ericsson.adp.security.certm.certificatewatcher.TrustStoreItem;
import com.ericsson.oss.air.aas.config.security.AdpCertificateDiscoveryProperties;
import com.ericsson.oss.air.aas.config.security.SecurityArtifactCombinator;
import com.ericsson.oss.air.aas.config.security.TlsConfiguration;
import com.ericsson.oss.air.aas.model.security.KafkaSecurityMaterial;
import com.ericsson.oss.air.util.logging.audit.AuditLogFactory;
import com.ericsson.oss.air.util.logging.audit.AuditLogger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Implementation of {@link SecurityArtifactCombinator} that adds the source {@code TlsContext}'s into a {@link KafkaSecurityMaterial} instance.
 */
@Configuration
@EnableConfigurationProperties(AdpCertificateDiscoveryProperties.class)
@ConditionalOnBean(TlsConfiguration.class)
@Slf4j
@RequiredArgsConstructor
public class KafkaSecurityArtifactCombinator implements SecurityArtifactCombinator<TlsContext, KafkaSecurityMaterial> {

    private static final AuditLogger AUDIT_LOGGER = AuditLogFactory.getLogger(KafkaSecurityArtifactCombinator.class);

    private static final String LOG_MSG_TEMPLATE = "Missing/invalid certificates in {}";

    private final AdpCertificateDiscoveryProperties adpCertificateDiscoveryProperties;

    /**
     * Combines the provided {@code TlsContext}'s needed to secure Kafka communication into a {@link KafkaSecurityMaterial} instance.
     * If any error case is encountered, then an empty {@code Optional} is returned.
     *
     * @param tlsContexts the source security material
     * @return a {@link KafkaSecurityMaterial}
     */
    @Override
    public Optional<KafkaSecurityMaterial> combine(final TlsContext... tlsContexts) {

        if (Objects.isNull(tlsContexts) || tlsContexts.length < 3) {
            log.debug("Invalid number of tlsContext passed to artifact combinator");
            return Optional.empty();
        }

        final TlsContext emptyContext = TlsContext.builder().build();

        final TlsContext kafkaContext =
                Arrays.stream(tlsContexts).filter(tlsContext -> KAFKA.getId().equals(tlsContext.getName())).findFirst().orElse(emptyContext);

        final TlsContext serverContext =
                Arrays.stream(tlsContexts).filter(tlsContext -> SERVER.getId().equals(tlsContext.getName())).findFirst().orElse(emptyContext);

        final TlsContext truststoreContext =
                Arrays.stream(tlsContexts).filter(tlsContext -> ROOTCA.getId().equals(tlsContext.getName())).findFirst().orElse(emptyContext);

        return combine(kafkaContext, serverContext, truststoreContext);
    }

    private Optional<KafkaSecurityMaterial> combine(final TlsContext kafkaKeyStoreContext, final TlsContext serverKeyStoreContext,
                                                    final TlsContext trustStoreContext) {

        final Optional<KeyStoreItem> keyStoreItemOptional = kafkaKeyStoreContext.getKeyStore();
        final Optional<KeyStoreItem> serverStoreItemOptional = serverKeyStoreContext.getKeyStore();
        final Optional<TrustStoreItem> trustStoreItemOptional = trustStoreContext.getTrustStore();
        final boolean isKafkaKeyStoreEmpty = keyStoreItemOptional.isEmpty();
        final boolean isServerKeyStoreEmpty = serverStoreItemOptional.isEmpty();
        final boolean isTrustStoreEmpty = trustStoreItemOptional.isEmpty();

        if (isKafkaKeyStoreEmpty || isServerKeyStoreEmpty || isTrustStoreEmpty) {
            this.logMissingCertificatesMessage(isKafkaKeyStoreEmpty, isServerKeyStoreEmpty, isTrustStoreEmpty);
            return Optional.empty();
        }

        final KafkaSecurityMaterial kafkaSecurityMaterial = KafkaSecurityMaterial.builder()
                .kafkaKeyStoreItem(keyStoreItemOptional.get())
                .serverKeyStoreItem(serverStoreItemOptional.get())
                .trustStoreItem(trustStoreItemOptional.get())
                .build();

        return Optional.of(kafkaSecurityMaterial);
    }

    /*
     * (non-javadoc)
     *
     * Logs a message when there are missing/invalid certificates.
     */
    private void logMissingCertificatesMessage(final boolean isKafkaKeyStoreEmpty, final boolean isServerKeyStoreEmpty,
                                               final boolean isTrustStoreEmpty) {

        final String missingCertificatePaths = this.getMissingCertificatePaths(isKafkaKeyStoreEmpty, isServerKeyStoreEmpty, isTrustStoreEmpty,
                this.adpCertificateDiscoveryProperties);

        AUDIT_LOGGER.warn(LOG_MSG_TEMPLATE, missingCertificatePaths);
    }

    /*
     * (non-javadoc)
     *
     * Gets the monitored paths of the missing/invalid certificate files.
     */
    private String getMissingCertificatePaths(final boolean isKafkaKeyStoreEmpty, final boolean isServerKeyStoreEmpty,
                                              final boolean isTrustStoreEmpty,
                                              final AdpCertificateDiscoveryProperties discoveryProperties) {

        final List<String> missingCertificateLocationList = new ArrayList<>();

        if (isKafkaKeyStoreEmpty) {
            final String readKafkaKeyStorePath = Paths.get(discoveryProperties.getRootReadPath(),
                            KAFKA.getId(), discoveryProperties.getKeystoreRelativeDir())
                    .normalize().toAbsolutePath().toString();
            missingCertificateLocationList.add(readKafkaKeyStorePath);
        }

        if (isServerKeyStoreEmpty) {
            final String readServerKeyStorePath = Paths.get(discoveryProperties.getRootReadPath(),
                            SERVER.getId(), discoveryProperties.getKeystoreRelativeDir())
                    .normalize().toAbsolutePath().toString();
            missingCertificateLocationList.add(readServerKeyStorePath);
        }

        if (isTrustStoreEmpty) {
            final String readTrustStorePath = Paths.get(discoveryProperties.getRootReadPath(),
                            ROOTCA.getId(), discoveryProperties.getTruststoreRelativeDir())
                    .normalize().toAbsolutePath().toString();
            missingCertificateLocationList.add(readTrustStorePath);
        }

        return String.join(",", missingCertificateLocationList);
    }
}

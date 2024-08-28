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

import static com.ericsson.oss.air.util.logging.TestLoggingUtils.FACILITY_KEY;
import static com.ericsson.oss.air.util.logging.TestLoggingUtils.FACILITY_VALUE;
import static com.ericsson.oss.air.util.logging.TestLoggingUtils.SUBJECT_KEY;
import static com.ericsson.oss.air.util.logging.TestLoggingUtils.SUBJECT_VALUE;
import static com.ericsson.oss.air.util.logging.TestLoggingUtils.assertAuditEvent;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.net.ssl.SSLContext;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.ericsson.adp.security.certm.certificatewatcher.CertificateWatcherService;
import com.ericsson.adp.security.certm.certificatewatcher.KeyStoreItem;
import com.ericsson.adp.security.certm.certificatewatcher.TlsContext;
import com.ericsson.adp.security.certm.certificatewatcher.TrustStoreItem;
import com.ericsson.oss.air.aas.model.security.KafkaSecurityMaterial;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@ExtendWith(MockitoExtension.class)
class TlsConfigurationTest {

    private static final String SUBSCRIBE_MSG = "Subscribing to certificate changes in certs";

    private static final String LOADING_MSG = "Loading new certificates from certs";

    @Mock
    private CertificateWatcherService certificateWatcherService;

    @Mock
    private SecurityArtifactCombinator<TlsContext, SSLContext> securityArtifactCombinator;

    @Mock
    private SecurityArtifactCombinator<TlsContext, KafkaSecurityMaterial> kafkaSecurityArtifactCombinator;

    @InjectMocks
    private TlsConfiguration tlsConfiguration;

    @Mock
    private KeyStoreItem mockKeyStoreItem;

    @Mock
    private TrustStoreItem mockTrustStoreItem;

    private static final TlsContext EMPTY_TLS_CONTEXT = TlsContext.builder().build();

    private final ApplicationContextRunner runner = new ApplicationContextRunner();

    @Component
    private static class TestCertificateWatcherService implements CertificateWatcherService {

        @Override
        public Flux<TlsContext> observe(String certificateId) {
            return Flux.empty();
        }
    }

    @Component
    private static class TestSecurityArtifactCombinator implements SecurityArtifactCombinator {

        @Override
        public Optional combine(Object[] artifacts) {
            return Optional.empty();
        }
    }

    private ListAppender<ILoggingEvent> listAppender;

    @BeforeEach
    void setup() {
        final Logger logger = (Logger) LoggerFactory.getLogger(TlsConfiguration.class);
        logger.setLevel(Level.INFO);
        this.listAppender = new ListAppender<>();
        this.listAppender.start();
        logger.addAppender(this.listAppender);
    }

    @AfterEach
    public void tearDown() throws Exception {
        this.listAppender.stop();
    }

    @Test
    public void runApplication_NoTlsConfigurationBean() {
        this.runner.run(context -> assertThat(context).doesNotHaveBean("tlsConfiguration"));
    }

    @Test
    public void runApplication_HasTlsConfigurationAndSubscriptionBeans_OnlyRootWritePath() {
        this.runner.withPropertyValues("adp-certificate.discovery.root-write-path=tmp")
                .withBean(TestCertificateWatcherService.class)
                .withBean(TestSecurityArtifactCombinator.class)
                .withUserConfiguration(TlsConfiguration.class)
                .run(context -> {
                    assertThat(context).hasBean("tlsConfiguration");
                    assertThat(context).hasBean("sslContextPublisher");
                    assertThat(context).hasBean("kafkaTlsContextPublisher");
                });
    }

    @Test
    public void runApplication_HasTlsConfigurationAndSubscriptionBeans_AllCertProperties() {
        this.runner.withPropertyValues("adp-certificate.discovery.root-write-path=tmp",
                        "adp-certificate.discovery.root-read-path=/opt/certs",
                        "adp-certificate.discovery.keystore-relative-dir=tmpkeystore",
                        "adp-certificate.discovery.truststore-relative-dir=tmptruststore",
                        "adp-certificate.discovery.keyPassword=password",
                        "adp-certificate.discovery.password=password")
                .withBean(TestCertificateWatcherService.class)
                .withBean(TestSecurityArtifactCombinator.class)
                .withUserConfiguration(TlsConfiguration.class)
                .run(context -> {
                    assertThat(context).hasBean("tlsConfiguration");
                    assertThat(context).hasBean("sslContextPublisher");
                    assertThat(context).hasBean("kafkaTlsContextPublisher");
                });
    }

    @Test
    public void sslContextPublisher_CombinesTlsContexts_ReturnsSslContextImmediately() {

        final TlsContext keyStoreTlsContext = TlsContext.builder().keyStore(this.mockKeyStoreItem).build();
        final TlsContext trustStoreTlsContext = TlsContext.builder().trustStore(this.mockTrustStoreItem).build();

        for (final CertificateIdEnum certificateIdEnum : CertificateIdEnum.values()) {

            if (Set.of(CertificateIdEnum.SERVER, CertificateIdEnum.LOG).contains(certificateIdEnum)) {
                when(this.certificateWatcherService.observe(certificateIdEnum.getId())).thenReturn(Flux.just(keyStoreTlsContext));
            } else if (Set.of(CertificateIdEnum.ROOTCA, CertificateIdEnum.PMCA).contains(certificateIdEnum)) {
                when(this.certificateWatcherService.observe(certificateIdEnum.getId())).thenReturn(Flux.just(trustStoreTlsContext));
            } else if (!CertificateIdEnum.KAFKA.equals(certificateIdEnum)) {
                when(this.certificateWatcherService.observe(certificateIdEnum.getId())).thenReturn(Flux.just(EMPTY_TLS_CONTEXT));
            }
        }

        final AdpCertificateDiscoveryProperties properties = new AdpCertificateDiscoveryProperties();
        properties.setRootWritePath("/tmp");
        final Optional<SSLContext> combinedSslContext = this.tlsConfiguration.sslContextPublisher(properties).blockFirst();

        assertNotNull(combinedSslContext);

        final List<ILoggingEvent> loggingEventList = this.listAppender.list;

        assertFalse(loggingEventList.isEmpty());
        assertEquals(2, loggingEventList.size());

        assertAuditEvent(loggingEventList.get(0), Level.WARN, SUBSCRIBE_MSG);
        assertAuditEvent(loggingEventList.get(1), Level.WARN, LOADING_MSG);
    }

    @Test
    public void kafkaTlsContextPublisher_CombinesTlsContexts_ReturnsTlsContextImmediately() {

        final TlsContext keyStoreTlsContext = TlsContext.builder().name("kafka").keyStore(this.mockKeyStoreItem).build();
        final TlsContext trustStoreTlsContext = TlsContext.builder().name("rootca").trustStore(this.mockTrustStoreItem).build();
        final TlsContext serverTlsContext = TlsContext.builder().name("server").keyStore(this.mockKeyStoreItem).build();

        when(this.certificateWatcherService.observe(CertificateIdEnum.KAFKA.getId())).thenReturn(Flux.just(keyStoreTlsContext));
        when(this.certificateWatcherService.observe(CertificateIdEnum.ROOTCA.getId())).thenReturn(Flux.just(trustStoreTlsContext));
        when(this.certificateWatcherService.observe(CertificateIdEnum.SERVER.getId())).thenReturn(Flux.just(serverTlsContext));

        final Optional<KafkaSecurityMaterial> kafkaSecurityMaterial = this.tlsConfiguration.kafkaTlsContextPublisher().blockFirst();

        assertNotNull(kafkaSecurityMaterial);
    }

    @Test
    public void certificateChangeLog_generates_logs() {

        final TlsContext tlsContext = TlsContext.builder().trustStore(this.mockTrustStoreItem).keyStore(this.mockKeyStoreItem).build();

        for (final CertificateIdEnum certificateIdEnum : CertificateIdEnum.values()) {
            when(this.certificateWatcherService.observe(certificateIdEnum.getId())).thenReturn(Flux.just(tlsContext));
        }

        this.tlsConfiguration.certificateChangeLog();

        final List<ILoggingEvent> loggingEventList = this.listAppender.list;

        assertFalse(loggingEventList.isEmpty());
        assertEquals(5, loggingEventList.size());

        final Set<String> certChangeMessages = new java.util.HashSet<>(Set.of(
                "Certificate change detected for Log Transformer, content: [keystore, truststore]",
                "Certificate change detected for Root Certificate Authority, content:"
                        + " [keystore, truststore]",
                "Certificate change detected for PM Server Certificate Authority, "
                        + "content: [keystore, truststore]",
                "Certificate change detected for Embedded Server, content: "
                        + "[keystore, truststore]",
                "Certificate change detected for Kafka, content: "
                        + "[keystore, truststore]"));

        for (final ILoggingEvent loggingEvent : loggingEventList) {
            assertEquals(Level.WARN, loggingEvent.getLevel());
            assertFalse(loggingEvent.getMDCPropertyMap().isEmpty());
            assertEquals(2, loggingEvent.getMDCPropertyMap().size());
            assertEquals(FACILITY_VALUE, loggingEvent.getMDCPropertyMap().get(FACILITY_KEY));
            assertEquals(SUBJECT_VALUE, loggingEvent.getMDCPropertyMap().get(SUBJECT_KEY));

            certChangeMessages.remove(loggingEvent.getFormattedMessage());
        }

        assertTrue(certChangeMessages.isEmpty());
    }
}
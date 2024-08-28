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

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicStampedReference;

import javax.net.ssl.SSLContext;

import com.ericsson.oss.air.util.logging.audit.AuditLogFactory;
import com.ericsson.oss.air.util.logging.audit.AuditLogger;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;

/**
 * Default implementation of {@link SslContextConfigurator}.
 */
@Configuration
@ConditionalOnBean(TlsConfiguration.class)
@Slf4j
@RequiredArgsConstructor
public class SslContextConfiguratorImpl implements SslContextConfigurator<Optional<SSLContext>> {

    private static final AuditLogger AUDIT_LOGGER = AuditLogFactory.getLogger(SslContextConfiguratorImpl.class);

    private final Flux<Optional<SSLContext>> sslContextPublisher;

    private final SecurityConfigurationRegistry configurationRegistry;

    private final AtomicStampedReference<SSLContext> sslContext = new AtomicStampedReference<>(null, 0);

    @Override
    @PostConstruct
    public void init() {

        log.debug("Initializing SSL context configurator");

        final Optional<SSLContext> sslContextOptional = this.sslContextPublisher.blockFirst();
        this.setSslContext(sslContextOptional);

        this.sslContextPublisher.skip(1).subscribe(this::refresh);

        log.info("Initialized SSL context configurator");
    }

    @Override
    public void refresh(final Optional<SSLContext> newSslContext) {

        final String securityConfig = "security configuration";

        AUDIT_LOGGER.warn("Reloading {}", securityConfig);

        this.setSslContext(newSslContext);
        this.configurationRegistry.reloadConfiguration();

        AUDIT_LOGGER.warn("Completed reloading {}", securityConfig);
    }

    @Override
    public Optional<SSLContext> getSslContext() {
        return Optional.ofNullable(this.sslContext.getReference());
    }

    /**
     * Gets the stamp for the {@code AtomicStampedReference} of the managed {@code SSLContext}. This method is
     * intended to be used for unit tests and debug logs.
     *
     * @return the stamp
     */
    int getSslContextStamp() {
        return this.sslContext.getStamp();
    }

    private void setSslContext(final Optional<SSLContext> sslContextOptional) {

        SSLContext newSslContext = null;

        if (Objects.isNull(sslContextOptional) || sslContextOptional.isEmpty()) {
            AUDIT_LOGGER.warn("New SSL context is empty");
        } else {
            newSslContext = sslContextOptional.get();
        }

        final int[] stampArray = new int[1];
        final SSLContext currentSslContext = this.sslContext.get(stampArray);

        final boolean isSslContextChanged = this.sslContext.compareAndSet(currentSslContext, newSslContext,
                stampArray[0], stampArray[0] + 1);

        if (isSslContextChanged) {
            AUDIT_LOGGER.warn("New SSL context set");
            log.debug("SSL context has been set {} time(s).", this.getSslContextStamp());
        }
    }
}

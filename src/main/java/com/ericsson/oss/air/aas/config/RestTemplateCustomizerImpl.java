/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.aas.config;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.net.ssl.SSLContext;

import com.ericsson.oss.air.aas.config.security.SecurityConfigurationRegistry;
import com.ericsson.oss.air.aas.config.security.SecurityConfigurationReloader;
import com.ericsson.oss.air.aas.config.security.SslContextConfigurator;
import com.ericsson.oss.air.util.logging.audit.AuditLogFactory;
import com.ericsson.oss.air.util.logging.audit.AuditLogger;
import jakarta.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * An implementation of {@code org.springframework.boot.web.client.RestTemplateCustomizer} that adds the SSL Context
 * if TLS/SSL is enabled.
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class RestTemplateCustomizerImpl implements RestTemplateCustomizer, SecurityConfigurationReloader {

    private static final AuditLogger AUDIT_LOGGER = AuditLogFactory.getLogger(RestTemplateCustomizerImpl.class);

    private final Optional<SslContextConfigurator<Optional<SSLContext>>> sslContextConfigurator;

    private final Optional<SecurityConfigurationRegistry> securityConfigurationRegistry;

    @Getter(AccessLevel.PACKAGE) // used only for unit testing
    private final Collection<RestTemplate> restTemplateList = new ConcurrentLinkedQueue<>();

    /**
     * Initializes the {@code RestTemplateCustomizerImpl} to be registered with the {@link SecurityConfigurationRegistry}.
     */
    @PostConstruct
    @Override
    public void init() {
        this.securityConfigurationRegistry.ifPresent(configRegistry -> configRegistry.register(this));
    }

    @Override
    public void customize(final RestTemplate restTemplate) {

        if (this.sslContextConfigurator.isPresent()) {

            log.debug("Customizing RestTemplate with SSL Context");

            this.restTemplateList.add(restTemplate);

            restTemplate.setRequestFactory(this.createRequestFactory(this.sslContextConfigurator.get()));
        } else {
            log.debug("Customizing RestTemplate without SSL Context");
        }
    }

    @Override
    public void reload() {

        if (this.sslContextConfigurator.isPresent()) {
            this.restTemplateList.forEach(
                    restTemplate -> restTemplate.setRequestFactory(this.createRequestFactory(this.sslContextConfigurator.get())));
            AUDIT_LOGGER.warn("Reloaded REST clients with new SSL context");
        }
    }

    /*
     * (non-javadoc)
     *
     * Creates a request factory for the RestTemplate. Assumes contextConfigurator is not null.
     *
     */
    private ClientHttpRequestFactory createRequestFactory(final SslContextConfigurator<?> contextConfigurator) {
        final SSLConnectionSocketFactoryBuilder sslSocketFactory = SSLConnectionSocketFactoryBuilder.create();
        contextConfigurator.getSslContext().ifPresent(sslSocketFactory::setSslContext);
        final HttpClientConnectionManager manager = PoolingHttpClientConnectionManagerBuilder
                .create()
                .setSSLSocketFactory(sslSocketFactory.build())
                .build();
        final CloseableHttpClient closeableHttpClient = HttpClients.custom().setConnectionManager(manager).build();
        return new HttpComponentsClientHttpRequestFactory(closeableHttpClient);
    }
}

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

import java.util.Collections;
import java.util.Optional;

import javax.net.ssl.SSLContext;

import com.ericsson.oss.air.util.logging.audit.AuditLogFactory;
import com.ericsson.oss.air.util.logging.audit.AuditLogger;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.embedded.jetty.JettyServerCustomizer;
import org.springframework.boot.web.embedded.jetty.JettyServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Customizes the embedded Jetty web server to set up and reload SSL context.
 */
@Configuration
@ConditionalOnBean(TlsConfiguration.class)
@ConditionalOnProperty(value = "security.server.mtls.enabled",
                       havingValue = "true")
@Slf4j
@RequiredArgsConstructor
public class JettyWebServerCustomizer implements WebServerFactoryCustomizer<JettyServletWebServerFactory>, SecurityConfigurationReloader {

    private static final AuditLogger AUDIT_LOGGER = AuditLogFactory.getLogger(JettyWebServerCustomizer.class);

    private final SslContextConfigurator<Optional<SSLContext>> sslContextConfigurator;

    private final SecurityConfigurationRegistry configurationRegistry;

    @Value("${server.port:8443}")
    private int port;

    /**
     * Initializes the {@code JettyWebServerCustomizer} to be registered with the {@link SecurityConfigurationRegistry}.
     */
    @PostConstruct
    @Override
    public void init() {
        this.configurationRegistry.register(this);
    }

    /**
     * Creates the {@code SslContextFactory.Server}, which will contain the initial AAS SSL Context.
     *
     * @return {@code SslContextFactory.Server}
     */
    @Bean
    public SslContextFactory.Server sslContextFactory() {

        final SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();

        this.setUpSslContextFactory(sslContextFactory, this.sslContextConfigurator.getSslContext());

        return sslContextFactory;
    }

    @Override
    public void customize(final JettyServletWebServerFactory factory) {

        final JettyServerCustomizer jettyServerCustomizer = server -> {
            final ServerConnector serverConnector = new ServerConnector(server, this.sslContextFactory());
            serverConnector.setPort(this.port);
            disableSNIHostCheck(serverConnector);
            server.setConnectors(new Connector[] { serverConnector });
        };

        factory.setServerCustomizers(Collections.singletonList(jettyServerCustomizer));
    }

    private static void disableSNIHostCheck(ServerConnector serverConnector) {
        HttpConnectionFactory connectionFactory = serverConnector.getConnectionFactory(HttpConnectionFactory.class);
        SecureRequestCustomizer secureRequestCustomizer = connectionFactory.getHttpConfiguration().getCustomizer(SecureRequestCustomizer.class);
        secureRequestCustomizer.setSniHostCheck(false);
    }

    @Override
    public void reload() {

        try {
            this.sslContextFactory()
                    .reload(factory -> this.setUpSslContextFactory((SslContextFactory.Server) factory, this.sslContextConfigurator.getSslContext()));
            AUDIT_LOGGER.warn("Reloaded server with new SSL context");
        } catch (final Exception e) {
            AUDIT_LOGGER.warn("Cannot refresh server SSL context: ", e);
        }
    }

    private void setUpSslContextFactory(final SslContextFactory.Server contextFactory, final Optional<SSLContext> sslContextOptional) {
        contextFactory.setSslContext(sslContextOptional.orElse(null));
        contextFactory.setNeedClientAuth(true);
    }
}



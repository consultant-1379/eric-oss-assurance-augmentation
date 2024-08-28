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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;

import javax.net.ssl.SSLContext;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.embedded.jetty.JettyServerCustomizer;
import org.springframework.boot.web.embedded.jetty.JettyServletWebServerFactory;

@ExtendWith(MockitoExtension.class)
class JettyWebServerCustomizerTest {

    @Mock
    private SslContextConfigurator sslContextConfigurator;

    @Mock
    private SecurityConfigurationRegistry configurationRegistry;

    @InjectMocks
    private JettyWebServerCustomizer customizer;

    private Logger log;

    private ListAppender<ILoggingEvent> listAppender;

    @BeforeEach
    public void setUp() {

        this.log = (Logger) LoggerFactory.getLogger(JettyWebServerCustomizer.class);
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
    void init_Valid() {

        this.customizer.init();

        verify(this.configurationRegistry, times(1)).register(this.customizer);
    }

    @Test
    void sslContextFactory_WithSslContext_ReturnsSslContextFactory() throws NoSuchAlgorithmException {

        final SSLContext expectedSslContext = SSLContext.getDefault();
        when(this.sslContextConfigurator.getSslContext()).thenReturn(Optional.of(expectedSslContext));

        final SslContextFactory.Server sslContextFactory = this.customizer.sslContextFactory();

        assertNotNull(sslContextFactory);
        assertTrue(sslContextFactory.getNeedClientAuth());
        assertEquals(expectedSslContext, sslContextFactory.getSslContext());
    }

    @Test
    void sslContextFactory_NullSslContext_ReturnsSslContextFactory() {

        when(this.sslContextConfigurator.getSslContext()).thenReturn(Optional.empty());

        final SslContextFactory.Server sslContextFactory = this.customizer.sslContextFactory();

        assertNotNull(sslContextFactory);
        assertTrue(sslContextFactory.getNeedClientAuth());
        assertNull(sslContextFactory.getSslContext());
    }

    @Test
    void customize_Valid() {

        final JettyServletWebServerFactory factory = new JettyServletWebServerFactory();

        this.customizer.customize(factory);

        assertNotNull(factory);
        assertEquals(1, factory.getServerCustomizers().size());
    }

    @Test
    void checkIfSniHostCheckDisabled() {
        final JettyServletWebServerFactory factory = new JettyServletWebServerFactory();
        Server server = new Server();
        this.customizer.customize(factory);
        JettyServerCustomizer jettyServerCustomizer = factory.getServerCustomizers().stream().findFirst().get();
        jettyServerCustomizer.customize(server);
        ServerConnector connector = (ServerConnector) server.getConnectors()[0];
        HttpConnectionFactory connectionFactory = connector.getConnectionFactory(HttpConnectionFactory.class);
        SecureRequestCustomizer secureRequestCustomizer = connectionFactory.getHttpConfiguration().getCustomizer(SecureRequestCustomizer.class);
        assertFalse(secureRequestCustomizer.isSniHostCheck());
    }

    @Test
    void reload_NewPresentSslContext() throws Exception {

        final SslContextFactory.Server sslContextFactory = this.customizer.sslContextFactory();
        assertNull(sslContextFactory.getSslContext());

        final JettyWebServerCustomizer testCustomizer = new JettyWebServerCustomizer(this.sslContextConfigurator, this.configurationRegistry) {

            @Override
            public SslContextFactory.Server sslContextFactory() {
                return sslContextFactory;
            }
        };

        final SSLContext expectedSslContext = SSLContext.getDefault();
        when(this.sslContextConfigurator.getSslContext()).thenReturn(Optional.of(expectedSslContext));

        testCustomizer.reload();

        assertEquals(expectedSslContext, sslContextFactory.getSslContext());
        assertTrue(sslContextFactory.getNeedClientAuth());

        final List<ILoggingEvent> loggingEventList = this.listAppender.list;

        assertFalse(loggingEventList.isEmpty());
        assertEquals(1, loggingEventList.size());

        assertAuditEvent(loggingEventList.get(0), Level.WARN, "Reloaded server with new SSL context");
    }

    @Test
    void reload_NewEmptySslContext() throws Exception {

        final SSLContext expectedSslContext = SSLContext.getDefault();
        when(this.sslContextConfigurator.getSslContext()).thenReturn(Optional.of(expectedSslContext));

        final SslContextFactory.Server sslContextFactory = this.customizer.sslContextFactory();
        assertNotNull(sslContextFactory.getSslContext());

        final JettyWebServerCustomizer testCustomizer = new JettyWebServerCustomizer(this.sslContextConfigurator, this.configurationRegistry) {

            @Override
            public SslContextFactory.Server sslContextFactory() {
                return sslContextFactory;
            }
        };

        when(this.sslContextConfigurator.getSslContext()).thenReturn(Optional.empty());

        testCustomizer.reload();

        assertNull(sslContextFactory.getSslContext());
        assertTrue(sslContextFactory.getNeedClientAuth());
    }

    @Test
    void reload_ContextFactoryException_HandlesException() {

        final Exception exception = new RuntimeException(EXCEPTION_MSG);

        final SslContextFactory.Server sslContextFactory = new SslContextFactory.Server() {

            @Override
            public void reload(final java.util.function.Consumer<SslContextFactory> consumer) throws Exception {
                throw exception;
            }
        };
        sslContextFactory.setNeedClientAuth(true);
        assertNull(sslContextFactory.getSslContext());

        final JettyWebServerCustomizer testCustomizer = new JettyWebServerCustomizer(this.sslContextConfigurator, this.configurationRegistry) {

            @Override
            public SslContextFactory.Server sslContextFactory() {
                return sslContextFactory;
            }
        };

        testCustomizer.reload();

        assertNull(sslContextFactory.getSslContext());
        assertTrue(sslContextFactory.getNeedClientAuth());

        final List<ILoggingEvent> loggingEventList = this.listAppender.list;

        assertFalse(loggingEventList.isEmpty());
        assertEquals(1, loggingEventList.size());

        assertAuditEvent(loggingEventList.get(0), Level.WARN, "Cannot refresh server SSL context: ", exception);
    }
}
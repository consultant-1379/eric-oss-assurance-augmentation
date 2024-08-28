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

import static com.ericsson.oss.air.util.logging.TestLoggingUtils.assertAuditEvent;
import static com.ericsson.oss.air.util.logging.TestLoggingUtils.assertNonAuditEvent;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import javax.net.ssl.SSLContext;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

@ExtendWith(MockitoExtension.class)
class SslContextConfiguratorImplTest {

    @Mock
    private SecurityConfigurationRegistry configurationRegistry;

    @Mock
    private SSLContext mockSslContext;

    private Logger log;

    private ListAppender<ILoggingEvent> listAppender;

    @BeforeEach
    public void setUp() {

        this.log = (Logger) LoggerFactory.getLogger(SslContextConfiguratorImpl.class);
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
    void init_WithSslContext_Valid() {

        final Flux<Optional<SSLContext>> testFlux = Flux.just(Optional.of(this.mockSslContext));
        final SslContextConfiguratorImpl sslContextConfigurator = new SslContextConfiguratorImpl(testFlux, this.configurationRegistry);

        sslContextConfigurator.init();

        assertEquals(1, sslContextConfigurator.getSslContextStamp());
        assertEquals(this.mockSslContext, sslContextConfigurator.getSslContext().get());

        final List<ILoggingEvent> loggingEventList = this.listAppender.list;

        assertFalse(loggingEventList.isEmpty());
        assertEquals(2, loggingEventList.size());

        assertAuditEvent(this.listAppender.list.get(0), Level.WARN, "New SSL context set");
        assertNonAuditEvent(this.listAppender.list.get(1), Level.INFO, "Initialized SSL context configurator");
    }

    @Test
    void init_EmptySslContext_Valid() {

        final Flux<Optional<SSLContext>> testFlux = Flux.just(Optional.empty());
        final SslContextConfiguratorImpl sslContextConfigurator = new SslContextConfiguratorImpl(testFlux, this.configurationRegistry);

        sslContextConfigurator.init();

        assertEquals(1, sslContextConfigurator.getSslContextStamp());
        assertTrue(sslContextConfigurator.getSslContext().isEmpty());

        final List<ILoggingEvent> loggingEventList = this.listAppender.list;

        assertFalse(loggingEventList.isEmpty());
        assertEquals(3, loggingEventList.size());

        assertAuditEvent(this.listAppender.list.get(0), Level.WARN, "New SSL context is empty");
        assertAuditEvent(this.listAppender.list.get(1), Level.WARN, "New SSL context set");

        assertNonAuditEvent(this.listAppender.list.get(2), Level.INFO, "Initialized SSL context configurator");
    }

    @Test
    void init_NullSslContext_Valid() {

        final Flux<Optional<SSLContext>> testFlux = Flux.empty();
        final SslContextConfiguratorImpl sslContextConfigurator = new SslContextConfiguratorImpl(testFlux, this.configurationRegistry);

        sslContextConfigurator.init();

        assertEquals(1, sslContextConfigurator.getSslContextStamp());
        assertTrue(sslContextConfigurator.getSslContext().isEmpty());

        final List<ILoggingEvent> loggingEventList = this.listAppender.list;

        assertFalse(loggingEventList.isEmpty());
        assertEquals(3, loggingEventList.size());

        assertAuditEvent(this.listAppender.list.get(0), Level.WARN, "New SSL context is empty");
        assertAuditEvent(this.listAppender.list.get(1), Level.WARN, "New SSL context set");

        assertNonAuditEvent(this.listAppender.list.get(2), Level.INFO, "Initialized SSL context configurator");
    }

    @Test
    void reload_WithSslContext_Valid() {

        final Flux<Optional<SSLContext>> testFlux = Flux.just(Optional.empty(), Optional.of(this.mockSslContext));
        final SslContextConfiguratorImpl sslContextConfigurator = new SslContextConfiguratorImpl(testFlux, this.configurationRegistry);

        sslContextConfigurator.init();

        assertEquals(2, sslContextConfigurator.getSslContextStamp());
        assertEquals(this.mockSslContext, sslContextConfigurator.getSslContext().get());

        final List<ILoggingEvent> loggingEventList = this.listAppender.list;

        assertFalse(loggingEventList.isEmpty());
        assertEquals(6, loggingEventList.size());

        assertAuditEvent(this.listAppender.list.get(0), Level.WARN, "New SSL context is empty");
        assertAuditEvent(this.listAppender.list.get(1), Level.WARN, "New SSL context set");
        assertAuditEvent(this.listAppender.list.get(2), Level.WARN, "Reloading security configuration");
        assertAuditEvent(this.listAppender.list.get(3), Level.WARN, "New SSL context set");
        assertAuditEvent(this.listAppender.list.get(4), Level.WARN, "Completed reloading security configuration");

        assertNonAuditEvent(this.listAppender.list.get(5), Level.INFO, "Initialized SSL context configurator");
    }

    @Test
    void reload_EmptySslContext_Valid() {

        final Flux<Optional<SSLContext>> testFlux = Flux.just(Optional.of(this.mockSslContext), Optional.empty());
        final SslContextConfiguratorImpl sslContextConfigurator = new SslContextConfiguratorImpl(testFlux, this.configurationRegistry);

        sslContextConfigurator.init();

        assertEquals(2, sslContextConfigurator.getSslContextStamp());
        assertTrue(sslContextConfigurator.getSslContext().isEmpty());

        final List<ILoggingEvent> loggingEventList = this.listAppender.list;

        assertFalse(loggingEventList.isEmpty());
        assertEquals(6, loggingEventList.size());

        assertAuditEvent(this.listAppender.list.get(0), Level.WARN, "New SSL context set");
        assertAuditEvent(this.listAppender.list.get(1), Level.WARN, "Reloading security configuration");
        assertAuditEvent(this.listAppender.list.get(2), Level.WARN, "New SSL context is empty");
        assertAuditEvent(this.listAppender.list.get(3), Level.WARN, "New SSL context set");
        assertAuditEvent(this.listAppender.list.get(4), Level.WARN, "Completed reloading security configuration");

        assertNonAuditEvent(this.listAppender.list.get(5), Level.INFO, "Initialized SSL context configurator");
    }
}
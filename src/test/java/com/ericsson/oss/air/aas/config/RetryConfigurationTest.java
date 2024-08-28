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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import io.github.resilience4j.common.retry.configuration.RetryConfigCustomizer;
import io.github.resilience4j.core.registry.EntryAddedEvent;
import io.github.resilience4j.core.registry.RegistryEventConsumer;
import io.github.resilience4j.retry.Retry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

@ExtendWith(MockitoExtension.class)
class RetryConfigurationTest {

    @Mock
    private EntryAddedEvent<Retry> entryAddedEvent;

    @Mock
    private Retry retry;

    @Mock
    private Retry.EventPublisher eventPublisher;

    private Logger log;

    private ListAppender<ILoggingEvent> listAppender;

    @BeforeEach
    public void setUp() {
        this.log = (Logger) LoggerFactory.getLogger(RetryConfiguration.class);
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
    void retryConfigCustomizer() {

        final RetryConfiguration retryConfiguration = new RetryConfiguration();

        final RetryConfigCustomizer customizer = retryConfiguration.retryConfigCustomizer();

        assertNotNull(customizer);
        assertEquals("jdbc", customizer.name());
    }

    @Test
    void aasRetryRegistryEventConsumer_Jdbc() {

        final RetryConfiguration retryConfiguration = new RetryConfiguration();

        final RegistryEventConsumer<Retry> eventConsumer = retryConfiguration.aasRetryRegistryEventConsumer();

        assertNotNull(eventConsumer);

        when(this.entryAddedEvent.getAddedEntry()).thenReturn(this.retry);
        when(this.retry.getName()).thenReturn("jdbc");
        when(this.retry.getEventPublisher()).thenReturn(this.eventPublisher);

        eventConsumer.onEntryAddedEvent(this.entryAddedEvent);

        verify(this.eventPublisher, times(1)).onError(any());
    }

    @Test
    void aasRetryRegistryEventConsumer_NotJdbc() {

        final RetryConfiguration retryConfiguration = new RetryConfiguration();

        final RegistryEventConsumer<Retry> eventConsumer = retryConfiguration.aasRetryRegistryEventConsumer();

        assertNotNull(eventConsumer);

        when(this.entryAddedEvent.getAddedEntry()).thenReturn(this.retry);
        when(this.retry.getName()).thenReturn("ardq");
        when(this.retry.getEventPublisher()).thenReturn(this.eventPublisher);

        eventConsumer.onEntryAddedEvent(this.entryAddedEvent);

        verify(this.eventPublisher, times(1)).onError(any());
    }
}
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

import static com.ericsson.oss.air.util.ExceptionUtils.isDatabaseConnectivityException;

import com.ericsson.oss.air.util.logging.audit.AuditLogFactory;
import com.ericsson.oss.air.util.logging.audit.AuditLogger;
import io.github.resilience4j.common.retry.configuration.RetryConfigCustomizer;
import io.github.resilience4j.core.registry.EntryAddedEvent;
import io.github.resilience4j.core.registry.EntryRemovedEvent;
import io.github.resilience4j.core.registry.EntryReplacedEvent;
import io.github.resilience4j.core.registry.RegistryEventConsumer;
import io.github.resilience4j.retry.Retry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Contains all configuration for the Resilience4J retry module
 */
@Configuration
public class RetryConfiguration {

    public static final String RETRY_JDBC = "jdbc";

    private static final AuditLogger AUDIT_LOGGER = AuditLogFactory.getLogger(RetryConfiguration.class);

    /**
     * Returns the retry configuration customizer. This retry configuration is merged with and takes precedence over the configured values
     * in Spring configuration files.
     *
     * @return the retry configuration customizer
     */
    @Bean
    public RetryConfigCustomizer retryConfigCustomizer() {
        return RetryConfigCustomizer.of(RETRY_JDBC, builder ->
                builder.retryOnException(throwable -> isDatabaseConnectivityException((Throwable) throwable))
                        .build()
        );
    }

    /**
     * Creates a {@code RegistryEventConsumer<Retry>} in order to log audit events.
     *
     * @return a {@code RegistryEventConsumer<Retry>}
     */
    @Bean
    public RegistryEventConsumer<Retry> aasRetryRegistryEventConsumer() {
        return new RegistryEventConsumer<>() {
            @Override
            public void onEntryAddedEvent(final EntryAddedEvent<Retry> entryAddedEvent) {
                final Retry retry = entryAddedEvent.getAddedEntry();
                if (RETRY_JDBC.equals(retry.getName())) {
                    retry.getEventPublisher().onError(event ->
                            AUDIT_LOGGER.error("Cannot connect to database: ", event.getLastThrowable()));
                } else {
                    retry.getEventPublisher()
                            .onError(event -> AUDIT_LOGGER.error("Retries exhausted after " + event.getNumberOfRetryAttempts() + " attempts. Cause: ",
                                    event.getLastThrowable()));
                }
            }

            @Override
            public void onEntryRemovedEvent(final EntryRemovedEvent<Retry> entryRemoveEvent) {
                //no-op
            }

            @Override
            public void onEntryReplacedEvent(final EntryReplacedEvent<Retry> entryReplacedEvent) {
                //no-op
            }
        };
    }

}

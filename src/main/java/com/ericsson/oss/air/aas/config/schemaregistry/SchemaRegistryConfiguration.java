/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 *
 *
 * The copyright to the computer program(s) herein is the property of
 *
 * Ericsson Inc. The programs may be used and/or copied only with written
 *
 * permission from Ericsson Inc. or in accordance with the terms and
 *
 * conditions stipulated in the agreement/contract under which the
 *
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.aas.config.schemaregistry;

import java.util.Optional;

import javax.net.ssl.SSLContext;

import com.ericsson.oss.air.aas.config.security.SecurityConfigurationRegistry;
import com.ericsson.oss.air.aas.config.security.SecurityConfigurationReloader;
import com.ericsson.oss.air.aas.config.security.SslContextConfigurator;
import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.rest.RestService;
import jakarta.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for Schema Registry Client Bean
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class SchemaRegistryConfiguration implements SecurityConfigurationReloader {

    private final Optional<SslContextConfigurator<Optional<SSLContext>>> sslContextConfigurator;

    private final Optional<SecurityConfigurationRegistry> securityConfigurationRegistry;

    @Setter(AccessLevel.PACKAGE) // only for unit tests
    @Value("${dmm.schemaRegistry.url}")
    private String schemaRegistryUrl;

    @Getter
    private SchemaRegistryClient schemaRegistryClient;

    /**
     * Initializes the {@code SchemaRegistryConfiguration} to be registered with the {@link SecurityConfigurationRegistry}
     * and creates the managed {@code SchemaRegistryClient}.
     */
    @PostConstruct
    @Override
    public void init() {
        this.securityConfigurationRegistry.ifPresent(configRegistry -> configRegistry.register(this));

        this.schemaRegistryClient = this.createSchemaRegistryClient();
    }

    /*
     * (non-javadoc)
     *
     * Creates the schema registry client. The client's underlying REST calls will be configured to use AAS' SSL context
     * if TLS is enabled.
     */
    private SchemaRegistryClient createSchemaRegistryClient() {

        final RestService restService = new RestService(this.schemaRegistryUrl);

        this.sslContextConfigurator.flatMap(SslContextConfigurator::getSslContext).ifPresent(sslContext ->
                                                                                                     restService.setSslSocketFactory(((SSLContext) sslContext).getSocketFactory()));

        return new CachedSchemaRegistryClient(restService, 5);
    }

    @Override
    public void reload() {

        if (this.sslContextConfigurator.isPresent()) {
            this.schemaRegistryClient = this.createSchemaRegistryClient();
            log.debug("Reloaded Schema Registry Client with new SSL context");
        }
    }
}
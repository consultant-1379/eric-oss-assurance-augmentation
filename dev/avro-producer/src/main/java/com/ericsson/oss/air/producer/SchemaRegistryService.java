/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.producer;

import java.io.IOException;

import javax.annotation.PostConstruct;

import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaMetadata;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.rest.RestService;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SchemaRegistryService {

    private final DynamicConfig dynamicConfig;

    private SchemaRegistryClient schemaRegistryClient;

    public SchemaRegistryService(final DynamicConfig dynamicConfig) {
        this.dynamicConfig = dynamicConfig;
    }

    @PostConstruct
    public void init() {
        log.info("Creating Schema Registry Client based on URL: {}", this.dynamicConfig.getSchemaRegistryUrl());
        final RestService restService = new RestService(this.dynamicConfig.getSchemaRegistryUrl());
        this.schemaRegistryClient = new CachedSchemaRegistryClient(restService, 5);
    }

    public SchemaMetadata getSchemaMetadata(final String subject) throws RestClientException, IOException {
        return this.schemaRegistryClient.getLatestSchemaMetadata(subject);
    }

}



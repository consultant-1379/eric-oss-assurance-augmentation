/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.aas.service.schema;

import static com.ericsson.oss.air.util.ExceptionUtils.isRestClientConnectivityException;
import static org.springframework.http.HttpStatus.resolve;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

import com.ericsson.oss.air.aas.config.schemaregistry.SchemaRegistryConfiguration;
import com.ericsson.oss.air.exception.SchemaRegistryHttpClientErrorException;
import com.ericsson.oss.air.exception.SchemaRegistryHttpServerErrorException;
import com.ericsson.oss.air.util.logging.audit.AuditLogFactory;
import com.ericsson.oss.air.util.logging.audit.AuditLogger;
import io.confluent.kafka.schemaregistry.ParsedSchema;
import io.confluent.kafka.schemaregistry.avro.AvroSchema;
import io.confluent.kafka.schemaregistry.client.SchemaMetadata;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.Schema;
import org.apache.avro.SchemaParseException;
import org.apache.logging.log4j.util.Strings;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 * Schema Registry Service class to register, parse and get version from the AvroSchema
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SchemaRegistryService {

    private static final AuditLogger AUDIT_LOGGER = AuditLogFactory.getLogger(SchemaRegistryService.class);

    public static final String SR_REQUEST_FAILED_MSG_PREFIX = "Schema Registry request failed: ";

    public static final String SR_REQUEST_FAILED_MSG = SR_REQUEST_FAILED_MSG_PREFIX + "{} {}";

    private final SchemaRegistryConfiguration schemaRegistryConfiguration;

    /**
     * Register schema
     *
     * @param subject subject of the schema
     * @param schema  AvroSchema to be registered
     * @return integer the retrieved AvroSchema id
     * @throws RestClientException
     * @throws IOException
     */
    @CircuitBreaker(name = "schemaRegistry")
    @Retry(name = "schemaRegistry")
    public int register(final String subject, final ParsedSchema schema) throws RestClientException, IOException {
        log.info("Register schema with subject: [{}] to the Schema Registry", subject);
        try {
            return this.schemaRegistryConfiguration.getSchemaRegistryClient().register(subject, schema);
        } catch (final RestClientException e) {
            throw this.toHttpException(e);
        }
    }

    /**
     * Get a schema version
     *
     * @param subject subject of the schema
     * @param schema  AvroSchema to get the version
     * @return integer the version
     * @throws RestClientException
     * @throws IOException
     */
    @CircuitBreaker(name = "schemaRegistry")
    @Retry(name = "schemaRegistry")
    public int getVersion(final String subject, final AvroSchema schema) throws RestClientException, IOException {
        log.debug("Getting version for schema subject: [{}] from the Schema Registry", subject);

        try {
            return this.schemaRegistryConfiguration.getSchemaRegistryClient().getVersion(subject, schema);
        } catch (final RestClientException e) {
            throw this.toHttpException(e);
        }
    }

    /**
     * Return true if the given Avro schema is found within the specified subject in the schema registry.
     *
     * @param subject the subject
     * @param schema  the schema
     * @return the boolean
     * @throws RestClientException the rest client exception
     * @throws IOException         the io exception
     */
    @Retry(name = "schemaRegistry")
    @CircuitBreaker(name = "schemaRegistry")
    public boolean isSchemaFound(final String subject, final AvroSchema schema) throws RestClientException, IOException {
        try {
            this.schemaRegistryConfiguration.getSchemaRegistryClient().getId(subject, schema);
        } catch (final RestClientException e) {
            if (e.getStatus() == HttpStatus.NOT_FOUND.value()) {
                AUDIT_LOGGER.error(SR_REQUEST_FAILED_MSG, HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND.getReasonPhrase(), e);
                return false;
            }
            throw this.toHttpException(e);
        }
        return true;
    }

    /**
     * Parse an AvroSchema in string
     *
     * @param schema AvroSchema to parse
     * @return Optional object of AvroSchema
     */
    public Optional<AvroSchema> parseSchema(final String schema) {
        try {
            final var schemaParser = new Schema.Parser();
            return Optional.of(new AvroSchema(schemaParser.parse(schema)));
        } catch (final SchemaParseException exception) {
            log.warn("Could not parse schema. Cause: ", exception);
            return Optional.empty();
        }
    }

    /**
     * Get the latest AvroSchema from schema name(subject)
     *
     * @param subject Schema name
     * @return Optional object of AvroSchema
     */
    @CircuitBreaker(name = "schemaRegistry")
    @Retry(name = "schemaRegistry")
    public Optional<AvroSchema> getLatestSchema(final String subject) throws RestClientException, IOException {
        log.info("Getting latest schema for subject: [{}] from the Schema Registry", subject);
        final Optional<SchemaMetadata> schemaMetadata = this.getLatestSchemaMetadata(subject);

        if (schemaMetadata.isPresent()) {
            return this.parseSchema(schemaMetadata.get().getSchema());
        }

        return Optional.empty();
    }

    /**
     * Get the latest SchemaMetadata from schema name(subject)
     *
     * @param subject Schema name
     * @return Optional object of SchemaMetadata
     */
    Optional<SchemaMetadata> getLatestSchemaMetadata(final String subject) throws RestClientException, IOException {

        try {
            final SchemaMetadata schemaMetadata = this.schemaRegistryConfiguration.getSchemaRegistryClient().getLatestSchemaMetadata(subject);
            return Optional.ofNullable(schemaMetadata);
        } catch (final SchemaParseException exception) {
            log.warn("Could not get SchemaMetadata: {} for subject: with cause: ", subject, exception);
            return Optional.empty();
        } catch (final RestClientException e) {
            throw this.toHttpException(e);
        }

    }

    /**
     * A helper method is used to convert Schema Registry RestClientException into either SchemaRegistryHttpServerErrorException or SchemaRegistryHttpClientErrorException.
     * <p>
     * This addresses a limitation in the Schema Registry library, which does not distinguish between HTTP server and client exceptions. Utilizing this method allows us to restrict retries to server failures only.
     *
     * @param restClientException the rest client exception
     * @return the rest client exception
     */
    private RestClientException toHttpException(final RestClientException restClientException) {

        final int statusCode = restClientException.getStatus();

        final HttpStatus status = resolve(statusCode);

        if (isRestClientConnectivityException(statusCode)) {
            AUDIT_LOGGER.error(SR_REQUEST_FAILED_MSG, statusCode, Objects.nonNull(status) ? status.getReasonPhrase() : Strings.EMPTY,
                    restClientException);
        }

        // The RestClientException from Schema Registry Client only contains 4XX and 5XX status and we only need to worry about those two cases
        if (statusCode >= 500) {
            return new SchemaRegistryHttpServerErrorException(restClientException.getMessage(), restClientException.getStatus(),
                    restClientException.getErrorCode());
        }

        return new SchemaRegistryHttpClientErrorException(restClientException.getMessage(), restClientException.getStatus(),
                restClientException.getErrorCode());

    }

}
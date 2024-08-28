/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.aas.service;

import static com.ericsson.oss.air.aas.service.kafka.KafkaAdminService.CANNOT_CONNECT_KAFKA_MSG;
import static com.ericsson.oss.air.aas.service.schema.SchemaRegistryService.SR_REQUEST_FAILED_MSG;
import static com.ericsson.oss.air.aas.service.schema.SchemaRegistryService.SR_REQUEST_FAILED_MSG_PREFIX;
import static com.ericsson.oss.air.util.ExceptionUtils.isKafkaConnectivityException;
import static com.ericsson.oss.air.util.ExceptionUtils.isRestClientConnectivityException;
import static org.springframework.http.HttpStatus.resolve;

import java.io.IOException;
import java.util.Objects;

import com.ericsson.oss.air.aas.config.kafka.APKafkaProperties;
import com.ericsson.oss.air.util.logging.audit.AuditLogFactory;
import com.ericsson.oss.air.util.logging.audit.AuditLogger;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import io.micrometer.core.instrument.Counter;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Headers;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * Service intended for augmentation processing messages
 */
@Service
@Slf4j
public class AugmentationProcessingService {

    private static final AuditLogger AUDIT_LOGGER = AuditLogFactory.getLogger(AugmentationProcessingService.class);

    @Autowired
    private APKafkaProperties apKafkaProperties;

    @Autowired
    @Qualifier("apKafkaTemplate")
    private KafkaTemplate kafkaTemplate;

    @Autowired
    private Counter outputRecordsCounter;

    /**
     * Send outgoing record of provided type {@link GenericRecord} via Kafka template.
     *
     * @param record  the augmentation processing generic data records to be sent
     * @param headers the outgoing record headers
     * @param key     the outgoing record key
     */
    public void sendRecord(final GenericRecord record, final Headers headers, final String key) {
        final ProducerRecord<String, GenericRecord> producerRecord = new ProducerRecord<>(
                this.apKafkaProperties.getAugmentationProcessing().getTopic(), null, key, record, headers);

        try {
            this.kafkaTemplate.send(producerRecord);
            this.outputRecordsCounter.increment();
        } catch (final Exception e) {

            if (!logSchemaRegistryConnectivityException(e) && isKafkaConnectivityException(e)) {
                AUDIT_LOGGER.error(CANNOT_CONNECT_KAFKA_MSG, e);
            }

            throw e;
        }

    }

    /*
     * Returns true if the exception indicates a Schema Registry connectivity issue and an audit log is emitted. Otherwise, returns false.
     */
    private boolean logSchemaRegistryConnectivityException(final Throwable exception) {

        if (Objects.nonNull(exception.getCause())) {

            if (exception.getCause() instanceof RestClientException restClientException) {

                final int statusCode = restClientException.getStatus();
                final HttpStatus status = resolve(statusCode);

                if (isRestClientConnectivityException(statusCode)) {
                    AUDIT_LOGGER.error(SR_REQUEST_FAILED_MSG, statusCode, Objects.nonNull(status) ? status.getReasonPhrase() : Strings.EMPTY,
                            restClientException);
                    return true;
                }
            } else if (exception.getCause() instanceof IOException) {
                AUDIT_LOGGER.error(SR_REQUEST_FAILED_MSG_PREFIX, exception);
                return true;
            }
        }
        return false;
    }

}

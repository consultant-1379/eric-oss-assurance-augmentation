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

package com.ericsson.oss.air.aas.handler;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

import com.ericsson.oss.air.aas.model.ardq.ArdqRequestDto;
import com.ericsson.oss.air.aas.model.ardq.ArdqResponseDto;
import com.ericsson.oss.air.aas.model.record.SchemaNamespace;
import com.ericsson.oss.air.aas.model.record.SchemaSubject;
import com.ericsson.oss.air.aas.service.AugmentationProcessingService;
import com.ericsson.oss.air.aas.service.ardq.ArdqService;
import com.ericsson.oss.air.api.generated.model.ArdqAugmentationFieldDto;
import com.ericsson.oss.air.exception.AasValidationException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.micrometer.core.instrument.Counter;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Headers;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.UnknownHttpStatusCodeException;

/**
 * Object class to represent the Augmentation Processor parameters
 */
@Slf4j
@Getter
@Builder
public class AugmentationProcessor {

    @NonNull
    private String ardqUrl;

    @NonNull
    private Schema outputSchema;

    @NonNull
    private List<ArdqAugmentationFieldDto> fields;

    private String ardqType;

    @NonNull
    private AugmentationProcessingService augProcService;

    @NonNull
    private ArdqService ardqService;

    @NonNull
    private Counter augmentationErrorsCounter;

    private Integer schemaVersion;

    /**
     * Performs the operations required to retrieve the augmented data and create the augmented output data record
     *
     * @param incomingRecord The input record used to generate the augmented output record
     */
    public void apply(final ConsumerRecord<String, GenericRecord> incomingRecord) {

        final ArdqRequestDto ardqRequestDto;

        final SchemaSubject schemaSubject = SchemaSubject.parse(incomingRecord.value().getSchema());

        try {
            // Populate the ARDQ query resource
            ardqRequestDto = AugmentationProcessorHandler.getPopulatedArdqRequest(incomingRecord.value(), this.getFields(),
                    this.ardqType);
        } catch (final AasValidationException e) {
            this.augmentationErrorsCounter.increment();
            log.error("[{}] Failed to prepare ARDQ request: ", schemaSubject, e);
            return;
        }

        try {

            // Send a POST request to ARDQ with the populated resource
            log.info("[{}] Retrieving augmentation data", schemaSubject);
            final ResponseEntity<ArdqResponseDto> ardqResponse = this.ardqService.getAugmentationData(this.getArdqUrl(), ardqRequestDto);

            for (final List<ArdqResponseDto.AugmentationField> augmentationFields : Objects.requireNonNull(ardqResponse.getBody()).getFields()) {
                // Copy un-augmented fields from incoming record AND augmented fields from ARDQ response to a new output record
                final GenericRecord outputRecord = AugmentationProcessorHandler.getPopulatedOutputRecord(incomingRecord, augmentationFields,
                        this.getOutputSchema());

                // Send output record message via Kafka template
                this.augProcService.sendRecord(outputRecord, generateOutputHeaders(incomingRecord, outputSchema), incomingRecord.key());
                log.info("[{}] Augmented record sent. record: {}", schemaSubject, outputRecord);
            }

        } catch (final HttpServerErrorException e) {
            this.augmentationErrorsCounter.increment();
            log.error("[{}] Failed to get augmentation data due to an HTTP Server Error: {}", schemaSubject, e.getMessage());
        } catch (final CallNotPermittedException e) {
            this.augmentationErrorsCounter.increment();
            log.error("[{}] Failed to get augmentation data: {}", schemaSubject, e.getMessage());
        } catch (final UnknownHttpStatusCodeException | HttpStatusCodeException e) {
            this.augmentationErrorsCounter.increment();
            log.error("[{}] Failed to get augmentation data", schemaSubject, e);
        } catch (final Exception e) {
            this.augmentationErrorsCounter.increment();
            log.error("[{}] Failed to process incoming record: ", schemaSubject, e);
        }

    }

    /**
     * Generate outgoing record headers
     * <p>
     * The outgoing record will use most of the incoming record headers, but only replace the incoming record headers for schemaSubject
     * by using the output schema subject
     *
     * @param consumerRecord       incoming record
     * @param outputSchema output schema
     * @return {@link Headers} outgoing record headers from the incoming record and the output schema
     */
    Headers generateOutputHeaders(final ConsumerRecord<String, GenericRecord> consumerRecord, final Schema outputSchema) {

        final Headers recordHeaders = consumerRecord.headers();

        recordHeaders.remove("schemaSubject");

        recordHeaders.add("schemaSubject",
                (SchemaSubject.builder()
                        .withSchemaNamespace(SchemaNamespace.parse(outputSchema.getNamespace()))
                        .withSchemaName(outputSchema.getName())
                        .build()
                        .toString())
                        .getBytes(StandardCharsets.UTF_8));

        recordHeaders.remove("schemaID"); //Need to be fixed in ESOA-3303

        recordHeaders.add("schemaID", this.schemaVersion.toString().getBytes(StandardCharsets.UTF_8));

        recordHeaders.forEach(header -> {
            String value = new String(header.value(), StandardCharsets.UTF_8);
            log.debug("header key: [{}] with value: [{}]", header.key(), value);
        });

        return recordHeaders;
    }

}

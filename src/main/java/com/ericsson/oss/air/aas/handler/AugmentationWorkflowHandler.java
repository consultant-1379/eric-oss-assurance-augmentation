/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.aas.handler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.ericsson.oss.air.aas.config.AugmentationProcessorFactory;
import com.ericsson.oss.air.aas.config.kafka.dynamic.APKafkaConsumerRegistrar;
import com.ericsson.oss.air.aas.config.kafka.dynamic.APMessageListener;
import com.ericsson.oss.air.aas.config.kafka.dynamic.APMessageListenerEndpointFactory;
import com.ericsson.oss.air.aas.model.SchemaReference;
import com.ericsson.oss.air.aas.model.SpecificationReference;
import com.ericsson.oss.air.aas.model.datacatalog.response.DataTypeResponseDto;
import com.ericsson.oss.air.aas.model.record.SchemaSubject;
import com.ericsson.oss.air.aas.repository.ArdqRegistrationDao;
import com.ericsson.oss.air.aas.repository.SchemaDao;
import com.ericsson.oss.air.aas.service.schema.DataCatalogService;
import com.ericsson.oss.air.aas.service.schema.SchemaRegistryService;
import com.ericsson.oss.air.api.generated.model.ArdqAugmentationFieldDto;
import com.ericsson.oss.air.api.generated.model.ArdqRegistrationDto;
import com.ericsson.oss.air.exception.SchemaNamespaceParseException;
import com.ericsson.oss.air.exception.SchemaReferenceParseException;
import com.ericsson.oss.air.exception.http.problem.exception.HttpProblemException;
import com.ericsson.oss.air.util.logging.FaultHandler;
import io.confluent.kafka.schemaregistry.avro.AvroSchema;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.Schema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

/**
 * A class to manage AugmentationWorkflow
 */
@Component
@Slf4j
public class AugmentationWorkflowHandler {

    @Autowired
    ArdqRegistrationDao ardqRegistrationDao;

    @Autowired
    APMessageListenerEndpointFactory apMessageListenerEndpointFactory;

    @Autowired
    APKafkaConsumerRegistrar apKafkaConsumerRegistrar;

    @Autowired
    AugmentationProcessorFactory augmentationProcessorFactory;

    @Autowired
    DataCatalogService dataCatalogService;

    @Autowired
    SchemaDao schemaDao;

    @Autowired
    private SchemaRegistryService schemaRegistryService;

    @Autowired
    private FaultHandler faultHandler;

    /**
     * Create ConsumerId from inputSchemaReference
     *
     * @param inputSchemaReference the input schema reference
     * @return the string
     */
    public static String mapConsumerId(final String inputSchemaReference) {
        return inputSchemaReference
                .replaceAll("^[^a-zA-Z0-9]", "")
                .replaceAll("[^a-zA-Z0-9/|\\-_]", "")
                .replace("|", "-")
                .replace("_", "-")
                .replace("/", "-");
    }

    /**
     * Create a new AugmentationWorkflow based on inputSchemaReference
     *
     * @param inputSchemaReference the input schema reference
     */
    public void create(final String inputSchemaReference) {

        final SchemaReference schemaReference;
        final String consumerId = mapConsumerId(inputSchemaReference);

        log.info("Creating an augmentation workflow with consumer id {}.", consumerId);

        try {
            schemaReference = SchemaReference.parse(inputSchemaReference);
        } catch (final SchemaReferenceParseException e) {
            log.debug("Failed to process input schema reference", e);
            log.error("Unable to create an augmentation workflow for the input schema reference {}.", inputSchemaReference);

            return;
        }

        final Optional<DataTypeResponseDto> dataTypeDto = this.getDCDataType(schemaReference);
        if (dataTypeDto.isEmpty()) {
            log.error("Unable to create an augmentation workflow for the input schema reference {}.", inputSchemaReference);
            return;
        }

        //Get kafka topic name
        final String topicName = dataTypeDto.get().getMessageSchema().getMessageDataTopic().getName();
        if (ObjectUtils.isEmpty(topicName)) {
            log.error("Kafka topic name not found in Data Catalog response for schema reference.");
            log.error("Unable to create an augmentation workflow for the input schema reference {}.", inputSchemaReference);
            return;
        }
        final SpecificationReference specificationReference = SpecificationReference.parse(
                dataTypeDto.get().getMessageSchema().getSpecificationReference());
        final SchemaSubject schemaSubject = specificationReference.getSchemaSubjectObject();

        final List<AugmentationProcessor> augmentationProcessorList = this.createAugmentationProcessorList(inputSchemaReference);

        if (ObjectUtils.isEmpty(augmentationProcessorList)) {
            return;
        }

        this.apKafkaConsumerRegistrar.createConsumer(consumerId, topicName, schemaSubject, augmentationProcessorList);
    }

    /**
     * Update existing AugmentationWorkflow with new processing info based on inputSchemaReference
     *
     * @param inputSchemaReference the input schema reference
     */
    public void update(final String inputSchemaReference) {

        final String consumerId = mapConsumerId(inputSchemaReference);

        log.info("Updating the augmentation workflow with consumer Id {}.", consumerId);

        if (this.apKafkaConsumerRegistrar.isRunning(consumerId)) {
            this.apKafkaConsumerRegistrar.pauseConsumer(consumerId);
        }

        final List<AugmentationProcessor> augmentationProcessorList = this.createAugmentationProcessorList(inputSchemaReference);
        final APMessageListener messageListener = this.apMessageListenerEndpointFactory.getMessageListener(consumerId);
        messageListener.setAugmentationProcessorList(augmentationProcessorList);

        if (augmentationProcessorList.isEmpty()) {
            log.debug("No augmentation processor created for input schema reference: {}. Will not start kafka consumer.", inputSchemaReference);
            return;
        }

        this.apKafkaConsumerRegistrar.resumeConsumer(consumerId);
    }

    /**
     * Stop existing AugmentationWorkflow based on inputSchemaReference
     *
     * @param inputSchemaReference the input schema reference
     */
    public void stop(final String inputSchemaReference) {

        final String consumerId = mapConsumerId(inputSchemaReference);

        log.info("Stopping augmentation workflow with consumer Id {}.", consumerId);

        if (this.apKafkaConsumerRegistrar.isRunning(consumerId)) {
            this.apKafkaConsumerRegistrar.pauseConsumer(consumerId);
        }

        final APMessageListener messageListener = this.apMessageListenerEndpointFactory.getMessageListener(consumerId);
        messageListener.setAugmentationProcessorList(new ArrayList<>());
    }

    /**
     * Return true if corresponding augmentation workflow is created
     *
     * @param inputSchemaReference the input schema reference
     * @return the boolean
     */
    public Boolean isCreated(final String inputSchemaReference) {
        return this.apKafkaConsumerRegistrar.isCreated(mapConsumerId(inputSchemaReference));
    }

    /**
     * Creates {@link AugmentationProcessor} List to create kafka consumer for given input schema.
     *
     * @param inputSchemaReference input schema reference
     * @return {@link AugmentationProcessor} list to create kafka consumer for given input schema.
     */
    protected List<AugmentationProcessor> createAugmentationProcessorList(final String inputSchemaReference) {

        final Optional<List<ArdqRegistrationDto>> registrationDtoList = this.ardqRegistrationDao.retrieveRegistrationsByInputSchemaRef(
                inputSchemaReference);

        final List<AugmentationProcessor> augmentationProcessorList = new ArrayList<>();

        if (registrationDtoList.isEmpty()) {
            return augmentationProcessorList;
        }

        for (final ArdqRegistrationDto registration : registrationDtoList.get()) {

            final String ardqId = registration.getArdqId();

            final Optional<String> outputSchemaReference = this.schemaDao.getOutputSchemaReference(ardqId, inputSchemaReference);

            final Optional<Schema> outputSchema = outputSchemaReference.flatMap(this.schemaDao::getSchema);

            final Optional<List<ArdqAugmentationFieldDto>> fieldsOptional = this.ardqRegistrationDao.getAugmentationFields(ardqId,
                    inputSchemaReference);

            final Optional<Integer> version = outputSchema.flatMap(schema -> {

                final String errorMessage = "Error occurred when trying to get output schema version from Schema Registry: ";
                try {
                    final SchemaSubject schemaSubject = SchemaSubject.parse(schema);
                    final AvroSchema avroSchema = new AvroSchema(schema);
                    assert schemaSubject != null;
                    return Optional.of(this.schemaRegistryService.getVersion(schemaSubject.toString(), avroSchema));
                } catch (final RestClientException | IOException | SchemaNamespaceParseException | CallNotPermittedException e) {
                    this.faultHandler.warn(errorMessage + schema + " schema issues ", e);
                }
                return Optional.empty();
            });

            if (outputSchema.isEmpty() || version.isEmpty()) {
                log.error(
                        "Unable to create augmentation processor with ardq id: [{}] and input schema reference: [{}] due to output schema or output"
                                + " schema version is empty ",
                        ardqId,
                        inputSchemaReference);
                continue;
            }

            log.info("Creating augmentationProcessor with: [ ardqId: [{}], input schema reference: [{}], output schema name: [{}] ]",
                    ardqId,
                    inputSchemaReference,
                    outputSchema.get().getName());

            final AugmentationProcessor augmentationProcessor = this.augmentationProcessorFactory.getAugmentationProcessor(registration.getArdqUrl(),
                    registration.getArdqType(),
                    outputSchema.get(),
                    fieldsOptional.orElseGet(
                            ArrayList::new),
                    version.get());

            if (fieldsOptional.isEmpty()) {
                log.debug(
                        "AugmentationProcessor [ ardqId: [{}], input schema reference: [{}], output schema name: [{}] ] is created with no "
                                + "augmentation fields ",
                        ardqId,
                        inputSchemaReference,
                        outputSchema.get().getName());
            }

            augmentationProcessorList.add(augmentationProcessor);
        }

        return augmentationProcessorList;
    }

    /**
     * Retrieve schema metadata from catalog service
     *
     * @param schemaReference the schema reference
     * @return an optional object DataTypeResponseDto
     */
    Optional<DataTypeResponseDto> getDCDataType(final SchemaReference schemaReference) {

        final DataTypeResponseDto dataTypeResponseDto;
        try {

            dataTypeResponseDto = this.dataCatalogService.retrieveSchemaMetadata(schemaReference);
        } catch (final HttpProblemException e) {

            log.error("Unable to retrieve schema information from data catalog", e);
            return Optional.empty();
        }

        return Optional.of(dataTypeResponseDto);
    }
}

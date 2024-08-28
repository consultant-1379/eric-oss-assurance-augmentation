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

package com.ericsson.oss.air.aas.config.kafka.dynamic;

import java.util.Collections;
import java.util.List;

import com.ericsson.oss.air.aas.handler.AugmentationProcessor;
import com.ericsson.oss.air.aas.model.record.SchemaSubject;
import io.micrometer.core.instrument.Counter;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.listener.MessageListener;

/**
 * The Augmentation Processing message Listener
 */
@Slf4j
@NoArgsConstructor
public class APMessageListener implements MessageListener<String, GenericRecord> {

    @Getter
    private List<AugmentationProcessor> augmentationProcessorList;

    private SchemaSubject schemaSubject;

    private Counter augmentedInputRecordsCounter;

    private Counter nonAugmentedInputRecordsCounter;


    /**
     * Instantiates a new Ap message listener.
     *
     * @param augmentationProcessorList        the augmentation processor list
     * @param schemaSubject                    the schema subject
     * @param augmentedInputRecordsCounter     the counter for input records to be augmented
     * @param nonAugmentedInputRecordsCounter  the counter for input records that do not require augmentation
     */
    public APMessageListener(final List<AugmentationProcessor> augmentationProcessorList, final SchemaSubject schemaSubject,
                             final Counter augmentedInputRecordsCounter, final Counter nonAugmentedInputRecordsCounter) {
        this.augmentationProcessorList = Collections.unmodifiableList(augmentationProcessorList);
        this.schemaSubject = schemaSubject;
        this.augmentedInputRecordsCounter = augmentedInputRecordsCounter;
        this.nonAugmentedInputRecordsCounter = nonAugmentedInputRecordsCounter;
    }

    /**
     * Sets augmentation processor list.
     *
     * @param augmentationProcessorList the augmentation processor list
     */
    public void setAugmentationProcessorList(final List<AugmentationProcessor> augmentationProcessorList) {
        this.augmentationProcessorList = Collections.unmodifiableList(augmentationProcessorList);
    }


    @Override
    public void onMessage(final @NonNull ConsumerRecord<String, GenericRecord> data) {
        log.debug("New data record received: {}", data);
        log.debug("Message: {}", data.value());

        if (!(this.validSchemaInformation(data.value().getSchema(), this.schemaSubject))) {
            this.nonAugmentedInputRecordsCounter.increment();
            return;
        }

        this.augmentedInputRecordsCounter.increment();

        log.info("[{}] Received an incoming record", this.schemaSubject);
        for (final AugmentationProcessor processor : this.augmentationProcessorList) {
            processor.apply(data);
        }
    }

    /**
     * Validate schema information based on SchemaSubject
     *
     * @param incomingRecordSchema schema of incoming record
     * @param schemaSubject        the schema subject
     * @return the boolean true if given schema matches schemaSubject
     */
    boolean validSchemaInformation(final Schema incomingRecordSchema, final SchemaSubject schemaSubject) {

        if (!incomingRecordSchema.getName().equals(schemaSubject.getSchemaName())) {
            log.debug("Received incoming record schema name: [{}] does not match with schema name in schema subject: [{}]. Skip it",
                    incomingRecordSchema.getName(), schemaSubject.getSchemaName());
            return false;
        }

        if (!incomingRecordSchema.getNamespace().equals(schemaSubject.getSchemaNamespace().toString())) {
            log.debug("Received incoming record schema namespace: [{}] does not match with namespace in schema subject: [{}]. Skip it",
                    incomingRecordSchema.getNamespace(), schemaSubject.getSchemaNamespace());
            return false;

        }

        return true;

    }

}

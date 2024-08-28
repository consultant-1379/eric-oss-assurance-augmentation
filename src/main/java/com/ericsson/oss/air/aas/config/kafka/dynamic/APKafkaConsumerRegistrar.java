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

import java.util.List;

import com.ericsson.oss.air.aas.handler.AugmentationProcessor;
import com.ericsson.oss.air.aas.model.record.SchemaSubject;

/**
 * The interface kafka consumer registrar for augmentation processing
 */
public interface APKafkaConsumerRegistrar {
    List<String> getConsumerIds();

    /**
     * Create a kafka consumer.
     *
     * @param consumerId                the consumer id
     * @param topic                     the topic
     * @param schemaSubject             the schema subject
     * @param augmentationProcessorList the list of processors
     */
    void createConsumer(String consumerId, String topic, SchemaSubject schemaSubject, List<AugmentationProcessor> augmentationProcessorList);

    /**
     * Pause an existing consumer with given id
     *
     * @param consumerId the consumer id
     */
    void pauseConsumer(String consumerId);

    /**
     * Resume a paused consumer.
     *
     * @param consumerId
     *         the consumer id
     */
    void resumeConsumer(String consumerId);

    /**
     * Deactivate a consumer.
     *
     * @param consumerId the consumer id
     */
    void deactivateConsumer(String consumerId);

    /**
     * Return true if a consumer with given consumerId is created
     *
     * @param consumerId the consumer id
     * @return the boolean
     */
    boolean isCreated(String consumerId);

    /**
     * Return true if a consumer with given consumerId is running
     *
     * @param consumerId the consumer id
     * @return the boolean
     */
    boolean isRunning(String consumerId);
}

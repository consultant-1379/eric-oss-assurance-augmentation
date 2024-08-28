/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.aas.config.kafka.property;

import java.util.Map;

/**
 * Interface for customizing the Kafka client properties in AAS
 */
public interface KafkaPropertyCustomizer {

    /**
     * Returns a map to configure the Kafka consumer clients for configuration notifications
     *
     * @return a map with consumer properties
     */
    Map<String, Object> kafkaConsumerProperties();

    /**
     * Returns a map to configure the Kafka producer clients for configuration notifications
     *
     * @return a map with producer properties
     */
    Map<String, Object> kafkaProducerProperties();

    /**
     * Returns a map to configure the Kafka admin client
     *
     * @return a map with kafka admin properties
     */
    Map<String, Object> kafkaAdminProperties();

    /**
     * Returns a map to configure augmentation processing kafka producers
     *
     * @return a map with producer properties
     */
    Map<String, Object> apKafkaProducerProperties();

    /**
     * Returns a map to configure augmentation processing kafka consumers
     *
     * @return a map with consumer properties
     */
    Map<String, Object> apKafkaConsumerProperties();
}

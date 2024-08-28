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

package com.ericsson.oss.air.aas.config.kafka;

import lombok.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Loads kafka configuration properties from the file
 */
@ConfigurationProperties(prefix = "spring.kafka")
@Value
public class APKafkaProperties {

    AugmentationProcessing augmentationProcessing;

    /**
     * Augmentation processing configuration properties
     */
    @Value
    public static class AugmentationProcessing {
        String topic;
        Consumer consumer;
        Producer producer;
    }

    /**
     * Augmentation processing producer configuration properties
     */
    @Value
    public static class Producer {
        int requestTimeoutMs;
        int retryBackoffMs;
        int reconnectBackoffMs;
        int reconnectBackoffMaxMs;
        int batchSize;
        int bufferMemory;
        int maxRequestSize;
        int linger;
    }

    /**
     * Consumer configuration properties
     */
    @Value
    public static class Consumer {
        int maxPollRecords;
        int maxPollIntervalMs;
    }

}

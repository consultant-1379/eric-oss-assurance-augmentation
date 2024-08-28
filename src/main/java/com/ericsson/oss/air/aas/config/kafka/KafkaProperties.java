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
public class KafkaProperties {

    Admin admin;
    String bootstrapServers;
    AutoConfigTopics autoConfigTopics;
    RegistrationNotification registrationNotification;

    /**
     * Kafka Admin client configuration properties
     */
    @Value
    public static class Admin {
        int retry;
        int retryBackoffMs;
        int reconnectBackoffMs;
        int reconnectBackoffMaxMs;
        int requestTimeoutMs;
        int retryInterval;
    }

    /**
     * Kafka topics configuration properties
     */
    @Value
    public static class AutoConfigTopics {
        Notification notification;
        AugmentationProcessing augmentationProcessing;
    }

    /**
     * Registration notification configuration properties
     */
    @Value
    public static class RegistrationNotification {
        String topic;
        Consumer consumer;
        Producer producer;
    }

    /**
     * Kafka topic configs for notification flow
     */
    @Value
    public static class Notification {
        String name;
        int partitions;
        short replicas;
        String retention;
        int minInSyncReplicas;
    }

    /**
     * Consumer configuration properties
     */
    @Value
    public static class Consumer {
        int maxPollRecords;
        int maxPollIntervalMs;
    }

    /**
     * Producer configuration properties
     */
    @Value
    public static class Producer {
        int requestTimeoutMs;
    }

    /**
     * Kafka topic configs for augmentation processing
     */
    @Value
    public static class AugmentationProcessing {
        String name;
        int partitions;
        short replicas;
        String compression;
        String retention;
        int minInSyncReplicas;
    }
}

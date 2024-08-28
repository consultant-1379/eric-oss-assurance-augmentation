/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.aas.service.kafka;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import com.ericsson.oss.air.aas.config.kafka.property.KafkaPropertyCustomizer;
import com.ericsson.oss.air.exception.KafkaRuntimeException;
import com.ericsson.oss.air.exception.TopicCreationFailedException;
import com.ericsson.oss.air.exception.UnsatisfiedExternalDependencyException;
import com.ericsson.oss.air.util.logging.FaultHandler;
import com.ericsson.oss.air.util.logging.audit.AuditLogFactory;
import com.ericsson.oss.air.util.logging.audit.AuditLogger;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.errors.TopicExistsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

/**
 * Kafka admin service to handle topics creation at startup. This admin service will be deprecated when AAS switches
 * over to Strimzi CRs.
 */
@Service
@Slf4j
public class KafkaAdminService {

    private static final AuditLogger AUDIT_LOGGER = AuditLogFactory.getLogger(KafkaAdminService.class);

    public static final String CANNOT_CONNECT_KAFKA_MSG = "Cannot connect to Kafka: ";

    @Autowired
    private KafkaPropertyCustomizer kafkaPropertyCustomizer;

    @Autowired
    private FaultHandler faultHandler;

    /**
     * Creates kafka topics at the startup
     *
     * @param topics a list of new topics
     */
    @Retryable(retryFor = TopicCreationFailedException.class,
               maxAttemptsExpression = "${spring.kafka.availability.retryAttempts}",
               backoff = @Backoff(delayExpression = "${spring.kafka.availability.retryInterval}"))
    public void createKafkaTopics(final NewTopic... topics) {

        log.info("Trigger auto configuration for Kafka topics: {}", Arrays.stream(topics).map(NewTopic::name).toList());

        try (Admin adminClient = Admin.create(this.kafkaPropertyCustomizer.kafkaAdminProperties())) {

            final Set<String> topicNames = this.getTopics(adminClient);
            for (final NewTopic topic : topics) {
                this.createTopicIfNotAlreadyCreated(adminClient, topic, topicNames);
            }
        } catch (final InterruptedException e) {

            Thread.currentThread().interrupt();
            throw new TopicCreationFailedException("Interrupted while creating Kafka topics: ", e);
        } catch (final KafkaException e) {

            log.warn("Failed to create Kafka topic: {}", e.getMessage());
            throw new TopicCreationFailedException("Unable to create Kafka topic", e);
        } catch (final ExecutionException e) {

            // ExecutionException is a wrapper of actual exception.
            // In order to get actual exception, we have to call .getCause
            final Throwable cause = e.getCause();
            log.warn("Failed to create Kafka topic: {}", cause.getMessage());
            throw new TopicCreationFailedException("Unable to create Kafka topic", cause);
        }
    }

    @Recover
    public void recoverCreateKafkaTopics(final Exception e) {
        this.recoverMethod(e);
    }

    /**
     * Gets a set of kafka topics from the admin client
     *
     * @param client kafka admin client
     * @return a set of existing kafka topics
     * @throws InterruptedException interrupted exception
     * @throws ExecutionException   execution exception while fetching topics
     */
    Set<String> getTopics(final Admin client) throws InterruptedException, ExecutionException {
        final ListTopicsResult listTopicsResult = client.listTopics();
        return listTopicsResult.names().get();
    }

    private void createTopicIfNotAlreadyCreated(final Admin client, final NewTopic topic, final Set<String> topicNames)
            throws InterruptedException, ExecutionException {

        log.info("Creating topic: [{}] with [{}] partitions and [{}] replicas", topic.name(), topic.numPartitions(),
                topic.replicationFactor());

        if (topicNames.contains(topic.name())) {
            log.info("Topic: [{}] already exists", topic.name());
            return;
        }

        final String topicName = topic.name();
        final CreateTopicsResult result = client.createTopics(Collections.singleton(topic));

        try {

            final var future = result.values().get(topicName);
            future.get();
        } catch (final ExecutionException e) {

            final Throwable cause = e.getCause();
            if (cause instanceof TopicExistsException) {
                this.faultHandler.warn("Topic already exists. The topic could have been created by a different instance of AAS.", e);
            } else {
                throw e;
            }
        }

        log.info("Topic: [{}] created successfully", topicName);
    }

    /**
     * Checks for Kafka server availability.
     *
     * @return true if Kafka server is reachable, else false.
     */
    @Retryable(retryFor = { UnsatisfiedExternalDependencyException.class },
               maxAttemptsExpression = "${spring.kafka.availability.retryAttempts}",
               backoff = @Backoff(delayExpression = "${spring.kafka.availability.retryInterval}"))
    public boolean verifyConnection() {
        log.info("Checking for Kafka availability");
        final boolean isKafkaNodeAvailable;
        try (var client = Admin.create(this.kafkaPropertyCustomizer.kafkaAdminProperties())) {
            isKafkaNodeAvailable = this.checkKafkaNode(client);
        } catch (final InterruptedException ie) {
            this.faultHandler.warn("Kafka Connection check failed: ", ie);
            Thread.currentThread().interrupt();
            throw new UnsatisfiedExternalDependencyException("Thread Interrupted: " + ie.getMessage());
        } catch (final Exception e) {
            this.faultHandler.warn("Kafka Connection check failed: ", e);
            throw new UnsatisfiedExternalDependencyException("Kafka not available : " + e.getMessage());
        }
        log.info("Successfully connected to Kafka");
        return isKafkaNodeAvailable;
    }

    @Recover
    public boolean recoverVerifyConnection(final Exception e) {
        this.recoverMethod(e);
        return false;
    }

    boolean checkKafkaNode(final Admin client) throws ExecutionException, InterruptedException {
        final Collection<Node> nodes = client.describeCluster().nodes().get();

        if (nodes.isEmpty()) {
            throw new UnsatisfiedExternalDependencyException("Kafka not available");
        } else {
            return true;
        }
    }

    private void recoverMethod(final Exception e) {
        AUDIT_LOGGER.error(CANNOT_CONNECT_KAFKA_MSG, e);
        throw new KafkaRuntimeException(e);
    }
}

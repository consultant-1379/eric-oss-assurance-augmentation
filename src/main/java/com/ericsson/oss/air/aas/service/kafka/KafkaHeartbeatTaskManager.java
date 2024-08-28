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

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.ScheduledFuture;

import com.ericsson.oss.air.aas.config.kafka.property.KafkaPropertyCustomizer;
import jakarta.annotation.PreDestroy;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

/**
 * Service to schedule and manage {@link KafkaHeartbeatTask}'s. This service allows AAS to monitor connectivity between AAS and Kafka.
 */
@Service
@Data
@Slf4j
public class KafkaHeartbeatTaskManager {

    private final ThreadPoolTaskScheduler kafkaHeartbeatTaskScheduler;

    private final KafkaPropertyCustomizer kafkaPropertyCustomizer;

    // In seconds
    @Value("${spring.kafka.heartbeat.initialDelay:60}")
    private long initialDelay;

    // In seconds
    @Value("${spring.kafka.heartbeat.fixedDelay:60}")
    private long fixedDelay;

    private ScheduledFuture<?> heartbeatFuture;

    /**
     * Schedules heartbeat messages to be sent to Kafka on a schedule.
     */
    public void startHeartbeat() {
        log.info("Starting scheduler to send heartbeats to Kafka");

        final KafkaHeartbeatTask heartbeatTask = new KafkaHeartbeatTask(this.kafkaPropertyCustomizer.kafkaAdminProperties());

        this.heartbeatFuture = this.kafkaHeartbeatTaskScheduler.scheduleWithFixedDelay(heartbeatTask,
                Instant.now().plusSeconds(initialDelay), Duration.ofSeconds(fixedDelay));
    }

    /**
     * Cancels the heartbeat scheduled tasks upon shut down of AAS.
     */
    @PreDestroy
    public void shutdown() {

        log.info("Stopping heartbeats between AAS and Kafka");
        if (Objects.nonNull(this.heartbeatFuture)) {
            this.heartbeatFuture.cancel(true);
        }
    }

}

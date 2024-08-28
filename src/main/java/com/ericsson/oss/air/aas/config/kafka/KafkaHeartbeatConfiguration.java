/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.aas.config.kafka;

import com.ericsson.oss.air.aas.service.kafka.KafkaHeartbeatTask;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * Configures sending of Kafka heartbeat messages
 */
@Configuration
public class KafkaHeartbeatConfiguration {

    private static final int POOL_SIZE = 100;

    private static final String THREAD_NAME_PREFIX = "aas-kafka-heartbeat-task-";

    /**
     * Returns a {@code ThreadPoolTaskScheduler} for scheduling {@link KafkaHeartbeatTask}'s
     *
     * @return {@code ThreadPoolTaskScheduler}
     */
    @Bean
    public ThreadPoolTaskScheduler kafkaHeartbeatTaskScheduler() {
        final ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(POOL_SIZE);
        threadPoolTaskScheduler.setThreadNamePrefix(THREAD_NAME_PREFIX);
        return threadPoolTaskScheduler;
    }

}

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

class KafkaHeartbeatConfigurationTest {

    @Test
    void kafkaHeartbeatTaskScheduler() {
        final KafkaHeartbeatConfiguration configuration = new KafkaHeartbeatConfiguration();
        final ThreadPoolTaskScheduler taskScheduler = configuration.kafkaHeartbeatTaskScheduler();

        assertNotNull(taskScheduler);
        assertEquals(100, taskScheduler.getPoolSize());
        assertEquals("aas-kafka-heartbeat-task-", taskScheduler.getThreadNamePrefix());
    }
}
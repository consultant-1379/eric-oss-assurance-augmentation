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

import static com.ericsson.oss.air.aas.service.kafka.KafkaAdminService.CANNOT_CONNECT_KAFKA_MSG;

import java.util.Map;

import com.ericsson.oss.air.util.logging.audit.AuditLogFactory;
import com.ericsson.oss.air.util.logging.audit.AuditLogger;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.Admin;

/**
 * Task that sends a heartbeat to Kafka
 */
@Data
@Slf4j
public class KafkaHeartbeatTask implements Runnable {

    private static final AuditLogger AUDIT_LOGGER = AuditLogFactory.getLogger(KafkaHeartbeatTask.class);

    private final Map<String, Object> kafkaAdminProperties;

    /**
     * Sends a heartbeat to Kafka and logs the result.
     */
    @Override
    public void run() {
        log.info("Sending heartbeat to Kafka");

        try (final Admin client = Admin.create(this.kafkaAdminProperties)) {
            client.describeCluster().nodes().get();
            log.debug("Successfully connected to Kafka");

        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            AUDIT_LOGGER.error(CANNOT_CONNECT_KAFKA_MSG, e);
        } catch (final Exception e) {
            AUDIT_LOGGER.error(CANNOT_CONNECT_KAFKA_MSG, e);
        }

    }

}

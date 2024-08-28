/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.aas.config;

import com.ericsson.oss.air.util.logging.audit.AuditLogFactory;
import com.ericsson.oss.air.util.logging.audit.AuditLogger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.ErrorCode;
import org.flywaydb.core.api.FlywayException;
import org.springframework.context.annotation.Configuration;

/**
 * Implementation of the Flyway schema migration.
 */
@Configuration
@Slf4j
@RequiredArgsConstructor
public class FlywaySchemaMigration {

    private static final AuditLogger AUDIT_LOGGER = AuditLogFactory.getLogger(FlywaySchemaMigration.class);

    private static final String MIGRATION_FAILED_MSG = "Flyway migration failed: {}";

    private static final String BASELINING_FAILED_MSG = "Baselining migrations failed: {}";

    private final Flyway flyway;

    /**
     * Performs the database schema migration operations.
     */
    public void migrate() {

        log.info("Migrating database schema");

        try {
            this.flyway.migrate();
        } catch (final FlywayException fe) {

            if (ErrorCode.DB_CONNECTION == fe.getErrorCode()) {
                AUDIT_LOGGER.error(MIGRATION_FAILED_MSG, fe.getMessage());
                throw fe;
            }

            log.error(MIGRATION_FAILED_MSG + ". Baselining migrations.", fe.getMessage());
            this.baselineMigrations();
        }

    }

    private void baselineMigrations() {
        try {
            this.flyway.baseline();
        } catch (final FlywayException fe) {

            if (ErrorCode.DB_CONNECTION == fe.getErrorCode()) {
                AUDIT_LOGGER.error(BASELINING_FAILED_MSG, fe.getMessage());
            } else {
                log.error(BASELINING_FAILED_MSG, fe.getMessage());
            }

            throw fe;
        }
    }

}

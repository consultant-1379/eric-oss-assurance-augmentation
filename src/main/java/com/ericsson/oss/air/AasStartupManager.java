/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air;

import java.util.ArrayList;

import com.ericsson.oss.air.aas.config.FlywaySchemaMigration;
import com.ericsson.oss.air.aas.config.kafka.APKafkaConfiguration;
import com.ericsson.oss.air.aas.config.kafka.KafkaConfiguredEvent;
import com.ericsson.oss.air.aas.handler.CreateNotificationHandler;
import com.ericsson.oss.air.aas.handler.registration.AugmentSchemaHandler;
import com.ericsson.oss.air.aas.repository.ArdqRegistrationDao;
import com.ericsson.oss.air.aas.service.ConfigurationNotificationService;
import com.ericsson.oss.air.aas.service.kafka.KafkaAdminService;
import com.ericsson.oss.air.util.logging.FaultHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * A class to manage the workflow at AAS startup
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class AasStartupManager {

    private final CreateNotificationHandler notificationHandler;

    private final ArdqRegistrationDao ardqRegistrationDao;

    private final AugmentSchemaHandler augmentSchemaHandler;

    private final KafkaAdminService adminService;

    private final APKafkaConfiguration apKafkaConfiguration;

    private final FlywaySchemaMigration flywaySchemaMigration;

    private final ConfigurationNotificationService configurationNotificationService;

    private final FaultHandler faultHandler;

    /**
     * Initializing the Assurance Augmentation Service
     */
    @EventListener(KafkaConfiguredEvent.class)
    public void init() {

        log.info("Updating database schemas for Assurance Augmentation Service");
        this.flywaySchemaMigration.migrate();

        log.info("Verifying Kafka connection");
        this.adminService.verifyConnection();

        //Create topic for augmentation processing.
        this.adminService.createKafkaTopics(this.apKafkaConfiguration.getAugmentationProcessingTopic());

        log.info("Starting the Assurance Augmentation Service");

        try {
            // Resync schemas in database against SR and DC
            this.ardqRegistrationDao
                    .getAllArdqRegistrations()
                    .orElseGet(ArrayList::new)
                    .forEach(this.augmentSchemaHandler::resync);

            this.ardqRegistrationDao.getAllArdqRegistrations()
                    .ifPresent(ardqRegistrationDtos -> ardqRegistrationDtos.forEach(this.notificationHandler::apply));
        } catch (final Exception e) {
            this.faultHandler.error("Failed to restore previous registered augmentations. Fatal error. Service restart may be required. ", e);
        }

        log.info("Starting the Configuration Notification Service");
        this.configurationNotificationService.startKafkaListener();
    }
}
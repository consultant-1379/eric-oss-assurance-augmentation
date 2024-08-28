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

package com.ericsson.oss.air.aas.handler.registration;

import java.util.List;

import com.ericsson.oss.air.aas.generated.model.notification.ArdqNotificationType;
import com.ericsson.oss.air.aas.generated.model.notification.ArdqRegistrationNotification;
import com.ericsson.oss.air.aas.handler.registration.validation.ArdqRegistrationDtoValidator;
import com.ericsson.oss.air.aas.handler.registration.validation.AugmentationRulesValidator;
import com.ericsson.oss.air.aas.handler.registration.validation.ObjectConflictValidator;
import com.ericsson.oss.air.aas.handler.registration.validation.UrlValidator;
import com.ericsson.oss.air.aas.model.IoSchema;
import com.ericsson.oss.air.aas.repository.ArdqRegistrationDao;
import com.ericsson.oss.air.aas.repository.SchemaDao;
import com.ericsson.oss.air.aas.service.ConfigurationNotificationService;
import com.ericsson.oss.air.api.generated.model.ArdqRegistrationDto;
import io.micrometer.core.instrument.Counter;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The type Create ardq registration handler.
 */
@Component
@AllArgsConstructor
public class CreateArdqRegistrationHandler {

    private final ArdqRegistrationDtoValidator validator;

    private final ConfigurationNotificationService configurationNotificationService;

    private final ArdqRegistrationDao ardqRegistrationDao;

    private final SchemaDao schemaDao;

    private final AugmentSchemaHandler augmentSchemaHandler;

    private final Counter createdArdqRegistrationsCounter;

    /**
     * Instantiates a new CreateArdqRegistrationHandler object.
     *
     * @param objectConflictValidator          the id validator
     * @param urlValidator                     the url validator
     * @param augmentationRulesValidator       the augmentation rules validator
     * @param ardqRegistrationDao              the ardq registration dao
     * @param schemaDao                        the schema dao
     * @param augmentSchemaHandler             the augment schema handler
     * @param configurationNotificationService the configuration notification service
     * @param createdArdqRegistrationsCounter  the counter for successfully created ARDQ registrations
     */
    @Autowired
    public CreateArdqRegistrationHandler(final ObjectConflictValidator objectConflictValidator,
                                         final UrlValidator urlValidator,
                                         final AugmentationRulesValidator augmentationRulesValidator,
                                         final ArdqRegistrationDao ardqRegistrationDao,
                                         final SchemaDao schemaDao,
                                         final AugmentSchemaHandler augmentSchemaHandler,
                                         final ConfigurationNotificationService configurationNotificationService,
                                         final Counter createdArdqRegistrationsCounter) {
        this.validator = objectConflictValidator
                .and(urlValidator)
                .and(augmentationRulesValidator);
        this.ardqRegistrationDao = ardqRegistrationDao;
        this.schemaDao = schemaDao;
        this.augmentSchemaHandler = augmentSchemaHandler;
        this.configurationNotificationService = configurationNotificationService;
        this.createdArdqRegistrationsCounter = createdArdqRegistrationsCounter;
    }

    /**
     * Handle a request to create ARDQ Registration
     *
     * @param ardqRegistrationDto the ardq registration dto
     */
    public void handle(final ArdqRegistrationDto ardqRegistrationDto) {
        // 1. Context validation
        this.validator.test(ardqRegistrationDto);
        // 2. Create augmented schema, get input schema, store augmented schema
        final List<IoSchema> ioSchemas = this.augmentSchemaHandler.create(ardqRegistrationDto);
        // 3. Save registration IDUN-52052
        this.ardqRegistrationDao.saveArdqRegistration(ardqRegistrationDto);

        // 4.Update IO Schema Mapping
        this.schemaDao.updateIOSchemaMapping(ioSchemas);

        // 5. Post ARDQ registration notification.
        final ArdqRegistrationNotification notification = ArdqRegistrationNotification.newBuilder()
                .setArdqId(ardqRegistrationDto.getArdqId())
                .setArdqNotificationType(ArdqNotificationType.CREATE)
                .build();
        this.configurationNotificationService.notify(notification);

        this.createdArdqRegistrationsCounter.increment();
    }

}

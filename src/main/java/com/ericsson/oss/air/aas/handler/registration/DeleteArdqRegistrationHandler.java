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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.ericsson.oss.air.aas.generated.model.notification.ArdqNotificationType;
import com.ericsson.oss.air.aas.generated.model.notification.ArdqRegistrationNotification;
import com.ericsson.oss.air.aas.handler.registration.validation.StringObjectMissingValidator;
import com.ericsson.oss.air.aas.repository.ArdqRegistrationDao;
import com.ericsson.oss.air.aas.repository.SchemaDao;
import com.ericsson.oss.air.aas.service.ConfigurationNotificationService;
import com.ericsson.oss.air.api.generated.model.ArdqRegistrationDto;
import io.micrometer.core.instrument.Counter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DeleteArdqRegistrationHandler extends ArdqRegistrationHandler {

    private final ArdqRegistrationDao ardqRegistrationDao;

    private final SchemaDao schemaDao;

    private final StringObjectMissingValidator validator;

    private final ConfigurationNotificationService configurationNotificationService;

    private final Counter deletedArdqRegistrationsCounter;

    /**
     * Instantiates a new Delete ardq registration handler.
     *
     * @param ardqRegistrationDao             the ardq registration dao
     * @param schemaDao                       the schema dao
     * @param validator                       the validator
     * @param service                         the service
     * @param deletedArdqRegistrationsCounter the counter for successfully deleted ARDQ registrations
     */
    @Autowired
    public DeleteArdqRegistrationHandler(final ArdqRegistrationDao ardqRegistrationDao,
                                         final SchemaDao schemaDao,
                                         final StringObjectMissingValidator validator,
                                         final ConfigurationNotificationService service,
                                         final Counter deletedArdqRegistrationsCounter) {
        this.ardqRegistrationDao = ardqRegistrationDao;
        this.schemaDao = schemaDao;
        this.validator = validator;
        this.configurationNotificationService = service;
        this.deletedArdqRegistrationsCounter = deletedArdqRegistrationsCounter;
    }

    /**
     * Delete the ARDQ Registration by the ardqId.
     *
     * @param ardqId the ardqId
     */
    public void handle(final String ardqId) {

        // Throw 404 if not found
        this.validator.test(ardqId);

        // Retrieve registration from AAS
        final Optional<ArdqRegistrationDto> ardqRegistrationDtoOptional = this.ardqRegistrationDao.findByArdqId(ardqId);

        if (ardqRegistrationDtoOptional.isEmpty()) {
            return;
        }

        final ArdqRegistrationDto ardqRegistrationDto = ardqRegistrationDtoOptional.get();
        final List<String> inputSchemaList = this.getInputSchemaList(ardqRegistrationDto);
        final List<String> deprecatedInputSchemaList = super.getNonReferencedInputSchemaReferenceList(this.schemaDao, inputSchemaList, ardqId);

        // Cleanup schema
        this.getSchemaToDeleteList(deprecatedInputSchemaList, ardqId)
                .forEach(this.schemaDao::deleteSchema);

        // Delete the registration in the AAS DB
        this.ardqRegistrationDao.deleteRegistrationByArdqId(ardqId);

        // Post ARDQ de-registration notification to kafka, including the set of deprecated schemas
        final ArdqRegistrationNotification notification = ArdqRegistrationNotification.newBuilder()
                .setArdqId(ardqId)
                .setAffectedInputSchemas(inputSchemaList)
                .setDeprecatedInputSchemas(deprecatedInputSchemaList)
                .setArdqNotificationType(ArdqNotificationType.DELETE)
                .build();

        this.configurationNotificationService.notify(notification);

        this.deletedArdqRegistrationsCounter.increment();
    }

    /**
     * Gets all schemas need to be deleted in this delete request. The calculation based on following logic
     * - Any removed input schema that is not referenced by other ardqRegistration need to be deleted
     * - All output schemas created by a ardq registration with given ardqId
     *
     * @param nonReferencedInputSchemaReferenceList the non referenced input schema reference list
     * @param ardqId                                the ardq id
     * @return the schema to delete list
     */
    List<String> getSchemaToDeleteList(final List<String> nonReferencedInputSchemaReferenceList, final String ardqId) {

        final List<String> schemasToDelete = new ArrayList<>();

        // All output schema related to this ardqId will be removed.
        schemasToDelete.addAll(this.schemaDao.getOutputSchemaReferenceList(ardqId));
        // All non-referenced input schema related to this ardqId will be removed.
        schemasToDelete.addAll(nonReferencedInputSchemaReferenceList);

        return schemasToDelete;
    }

}

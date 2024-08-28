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
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.ericsson.oss.air.aas.generated.model.notification.ArdqNotificationType;
import com.ericsson.oss.air.aas.generated.model.notification.ArdqRegistrationNotification;
import com.ericsson.oss.air.aas.handler.registration.validation.ArdqRegistrationDtoValidator;
import com.ericsson.oss.air.aas.handler.registration.validation.AugmentationRulesValidator;
import com.ericsson.oss.air.aas.handler.registration.validation.ObjectMissingValidator;
import com.ericsson.oss.air.aas.handler.registration.validation.UrlValidator;
import com.ericsson.oss.air.aas.model.IoSchema;
import com.ericsson.oss.air.aas.repository.ArdqRegistrationDao;
import com.ericsson.oss.air.aas.repository.SchemaDao;
import com.ericsson.oss.air.aas.service.ConfigurationNotificationService;
import com.ericsson.oss.air.api.generated.model.ArdqRegistrationDto;
import io.micrometer.core.instrument.Counter;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The type UpdateArdqRegistrationHandler.
 */
@Component
@Slf4j
@AllArgsConstructor
public class UpdateArdqRegistrationHandler extends ArdqRegistrationHandler {

    private final ArdqRegistrationDtoValidator validator;

    private final ArdqRegistrationDao ardqRegistrationDao;

    private final SchemaDao schemaDao;

    private final AugmentSchemaHandler augmentSchemaHandler;

    private final ConfigurationNotificationService configurationNotificationService;

    private final Counter updatedArdqRegistrationsCounter;

    /**
     * Instantiates a UpdateArdqRegistrationHandler.
     *
     * @param objectMissingValidator          the object missing validator
     * @param urlValidator                    the url validator
     * @param augmentationRulesValidator      the augmentation rules validator
     * @param ardqRegistrationDao             the ardqRegistrationDao
     * @param schemaDao                       the schema dao
     * @param service                         the service
     * @param augmentSchemaHandler            the augment schema handler
     * @param updatedArdqRegistrationsCounter the counter for successfully updated ARDQ registrations
     */
    @Autowired
    public UpdateArdqRegistrationHandler(final ObjectMissingValidator objectMissingValidator,
                                         final UrlValidator urlValidator,
                                         final AugmentationRulesValidator augmentationRulesValidator,
                                         final ArdqRegistrationDao ardqRegistrationDao,
                                         final SchemaDao schemaDao,
                                         final ConfigurationNotificationService service,
                                         final AugmentSchemaHandler augmentSchemaHandler,
                                         final Counter updatedArdqRegistrationsCounter) {
        this.validator = objectMissingValidator
                .and(urlValidator)
                .and(augmentationRulesValidator);
        this.ardqRegistrationDao = ardqRegistrationDao;
        this.schemaDao = schemaDao;
        this.configurationNotificationService = service;
        this.augmentSchemaHandler = augmentSchemaHandler;
        this.updatedArdqRegistrationsCounter = updatedArdqRegistrationsCounter;
    }

    /**
     * Handle a request to Update existing ARDQ Registration
     *
     * @param ardqRegistrationDto the ardq registration dto
     */
    public void handle(final ArdqRegistrationDto ardqRegistrationDto) {

        final String ardqId = ardqRegistrationDto.getArdqId();

        // 1. Context validation
        this.validator.test(ardqRegistrationDto);

        // 2. Retrieving registration from DB
        final Optional<ArdqRegistrationDto> existingRegistrationDto = this.ardqRegistrationDao.findByArdqId(ardqId);
        if (existingRegistrationDto.isEmpty()) {
            return;
        }
        // 3. comparing Ardq registration with existing resource
        if (ardqRegistrationDto.equals(existingRegistrationDto.get())) {
            return;
        }

        // 4. Get a list of input schema reference that need to be deprecated and all schema references need to be removed
        // Following Operation need to be done before any DAO changes
        final List<String> removedInputSchemaReferenceList = this.getRemovedInputSchemaReferenceList(ardqRegistrationDto,
                existingRegistrationDto.get());
        final List<String> deprecatedInputSchemaList = this.getDeprecatedInputSchemaReferenceList(removedInputSchemaReferenceList, ardqId);

        // 5. Cleanup schemas related this change
        this.getSchemaToDeleteList(removedInputSchemaReferenceList, deprecatedInputSchemaList, ardqId)
                .forEach(this.schemaDao::deleteSchema);

        // 5. update augmented schema
        final List<IoSchema> ioSchemas = this.augmentSchemaHandler.update(ardqRegistrationDto);

        // 6. Overwrite existing registration in DB
        this.ardqRegistrationDao.saveArdqRegistration(ardqRegistrationDto);

        // 7. Update IO Schema Mapping
        this.schemaDao.updateIOSchemaMapping(ioSchemas);

        // 8. Post Ardq registration notification
        log.debug("Deprecated input schema list generated. Size : {}", deprecatedInputSchemaList.size());
        final ArdqRegistrationNotification notification = ArdqRegistrationNotification.newBuilder()
                .setArdqId(ardqId)
                .setDeprecatedInputSchemas(deprecatedInputSchemaList)
                .setAffectedInputSchemas(removedInputSchemaReferenceList)
                .setArdqNotificationType(ArdqNotificationType.UPDATE)
                .build();
        this.configurationNotificationService.notify(notification);

        this.updatedArdqRegistrationsCounter.increment();
    }

    /**
     * Returns deprecated input schema reference list
     * <p>
     * Creates a list of deprecated input schemas. For each input schema reference in the deprecation candidate schema list, if that input schema is
     * not referenced in the registrations, it is added to the final list of deprecated input schemas.
     *
     * @param removedInputSchemaReferenceList a list of deprecated candidate input schemas
     * @param ardqId                          the ardq id
     * @return a list of deprecated input schemas
     */
    List<String> getDeprecatedInputSchemaReferenceList(final List<String> removedInputSchemaReferenceList, final String ardqId) {
        return super.getNonReferencedInputSchemaReferenceList(this.schemaDao, removedInputSchemaReferenceList, ardqId);
    }

    /**
     * Get a list of input schema reference which removed from oldRegistration
     *
     * @param newRegistration is the incoming ARDQ registration
     * @param oldRegistration is the existing ARDQ registration
     * @return a list of schema reference removed from oldRegistration
     */
    List<String> getRemovedInputSchemaReferenceList(final ArdqRegistrationDto newRegistration, final ArdqRegistrationDto oldRegistration) {

        // Creates a list of deprecation candidate input schemas. By comparing the incoming registration and existing registration.
        // if a given input schema that was included in certain rules in the existing registration and is no longer included in any rules
        // in the incoming registration, that particular input schema is added to the candidate list.
        final HashSet<String> inputSchemasInIncomingRegistration = new HashSet<>(this.getInputSchemaList(newRegistration));
        return this.getInputSchemaList(oldRegistration)
                .stream()
                .filter(inputSchema -> !inputSchemasInIncomingRegistration.contains(inputSchema))
                .distinct()
                .collect(Collectors.toList());

    }

    /**
     * Gets all schemas need to be delete in this update request. The calculation based on following logic
     * - Any removed input schema that is not referenced by other ardqRegistration need to be deleted
     * - Any output schemas that mapped to removed input schema need to be deleted
     *
     * @param removedInputSchemaReferenceList       A list input schema reference that is removed in this update request
     * @param nonReferencedInputSchemaReferenceList A list input schema reference that is deprecated in this update request
     * @param ardqId                                the ardq id
     * @return the schemas to delete list
     */
    List<String> getSchemaToDeleteList(final List<String> removedInputSchemaReferenceList,
                                       final List<String> nonReferencedInputSchemaReferenceList,
                                       final String ardqId) {

        // All non-referenced input schema reference need to be removed
        final List<String> schemasToDelete = new ArrayList<>(nonReferencedInputSchemaReferenceList);

        // All output schema related to removed input schema reference and current ardq registration need to be deleted from DB
        removedInputSchemaReferenceList
                .stream()
                .distinct()
                .map(inputSchemaRef -> this.schemaDao.getOutputSchemaReference(ardqId, inputSchemaRef))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(schemasToDelete::add);

        return schemasToDelete;
    }

}

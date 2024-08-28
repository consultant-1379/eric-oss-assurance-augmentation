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

package com.ericsson.oss.air.aas.handler;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.ericsson.oss.air.aas.generated.model.notification.ArdqRegistrationNotification;
import com.ericsson.oss.air.aas.repository.ArdqRegistrationDao;
import com.ericsson.oss.air.api.generated.model.ArdqAugmentationRuleDto;
import com.ericsson.oss.air.api.generated.model.ArdqRegistrationDto;
import lombok.AllArgsConstructor;

/**
 * Abstract class that holds common functionality to handle Create, Update and Delete registration notifications.
 */
@AllArgsConstructor
public abstract class NotificationHandler {

    protected ArdqRegistrationDao ardqRegistrationDao;

    protected AugmentationWorkflowHandler augmentationWorkflowHandler;

    /**
     * Handles ARDQ registration notification {@link ArdqRegistrationNotification}
     *
     * @param ardqRegistrationNotification ARDQ registration notification
     */
    public abstract void apply(final ArdqRegistrationNotification ardqRegistrationNotification);

    /**
     * Handles ARDQ registration {@link ArdqRegistrationDto}.
     *
     * @param ardqRegistrationDto {@link ArdqRegistrationDto}
     */
    public void apply(final ArdqRegistrationDto ardqRegistrationDto) {
        final Set<String> inputSchemaRefList = ardqRegistrationDto.getRules()
                .stream()
                .map(ArdqAugmentationRuleDto::getInputSchema)
                .collect(Collectors.toSet());

        for (final String inputSchemaRef : inputSchemaRefList) {
            if (this.augmentationWorkflowHandler.isCreated(inputSchemaRef)) {
                this.augmentationWorkflowHandler.update(inputSchemaRef);
            } else {
                this.augmentationWorkflowHandler.create(inputSchemaRef);
            }
        }
    }

    /**
     * Handle deprecated input schema changes in ArdqRegistrationNotification.
     * <ul>
     * <li> If a input schema is deprecated, stop corresponding augmentation workflow</li>
     * <li> If a input schema is affected and not deprecated, update corresponding augmentation workflow</li>
     * </ul>
     *
     * @param ardqRegistrationNotification the ardq registration notification
     */
    public void handleInputSchemaChanges(final ArdqRegistrationNotification ardqRegistrationNotification) {
        final List<String> deprecatedInputSchemas = Optional.ofNullable(ardqRegistrationNotification.getDeprecatedInputSchemas())
                .orElse(Collections.emptyList());
        final List<String> affectedInputSchemas = Optional.ofNullable(ardqRegistrationNotification.getAffectedInputSchemas())
                .orElse(Collections.emptyList());

        final HashSet<String> nonDeprecatedAffectedInputSchemas = new HashSet<>(affectedInputSchemas);
        deprecatedInputSchemas.forEach(nonDeprecatedAffectedInputSchemas::remove);

        deprecatedInputSchemas.forEach(this.augmentationWorkflowHandler::stop);
        nonDeprecatedAffectedInputSchemas.forEach(this.augmentationWorkflowHandler::update);
    }

    /**
     * Retrieve ArdqRegistrationDto object
     *
     * @param ardqRegistrationNotification the ardq registration notification
     * @return ArdqRegistrationDto Object
     */
    public Optional<ArdqRegistrationDto> getArdqRegistrationDto(final ArdqRegistrationNotification ardqRegistrationNotification) {
        final String ardqId = String.valueOf(ardqRegistrationNotification.getArdqId());
        return this.ardqRegistrationDao.findByArdqId(ardqId);
    }

}

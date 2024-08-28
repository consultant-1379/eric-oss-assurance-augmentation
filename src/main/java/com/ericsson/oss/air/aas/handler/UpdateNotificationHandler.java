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

import java.util.Optional;

import com.ericsson.oss.air.aas.generated.model.notification.ArdqRegistrationNotification;
import com.ericsson.oss.air.aas.repository.ArdqRegistrationDao;
import com.ericsson.oss.air.api.generated.model.ArdqRegistrationDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Handles update notifications
 */
@Component
@Slf4j
public class UpdateNotificationHandler extends NotificationHandler {

    @Autowired
    public UpdateNotificationHandler(final ArdqRegistrationDao ardqRegistrationDao, final AugmentationWorkflowHandler augmentationWorkflowHandler) {
        super(ardqRegistrationDao, augmentationWorkflowHandler);
    }

    @Override
    public void apply(final ArdqRegistrationNotification ardqRegistrationNotification) {

        this.handleInputSchemaChanges(ardqRegistrationNotification);

        final Optional<ArdqRegistrationDto> ardqRegistrationDtoOptional = this.getArdqRegistrationDto(ardqRegistrationNotification);

        if (ardqRegistrationDtoOptional.isEmpty()) {
            log.error("No registration found with ardq id: [{}] ", ardqRegistrationNotification.getArdqId());
            return;
        }

        this.apply(ardqRegistrationDtoOptional.get());
    }

}

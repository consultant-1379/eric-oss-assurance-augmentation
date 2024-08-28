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

import com.ericsson.oss.air.aas.generated.model.notification.ArdqRegistrationNotification;
import com.ericsson.oss.air.aas.repository.ArdqRegistrationDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Handles delete notifications.
 */
@Component
public class DeleteNotificationHandler extends NotificationHandler {

    @Autowired
    public DeleteNotificationHandler(final ArdqRegistrationDao ardqRegistrationDao, final AugmentationWorkflowHandler augmentationWorkflowHandler) {
        super(ardqRegistrationDao, augmentationWorkflowHandler);
    }

    @Override
    public void apply(final ArdqRegistrationNotification ardqRegistrationNotification) {

        this.handleInputSchemaChanges(ardqRegistrationNotification);
    }

}

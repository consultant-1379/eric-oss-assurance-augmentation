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

package com.ericsson.oss.air.aas.config;

import com.ericsson.oss.air.aas.generated.model.notification.ArdqNotificationType;
import com.ericsson.oss.air.aas.handler.CreateNotificationHandler;
import com.ericsson.oss.air.aas.handler.DeleteNotificationHandler;
import com.ericsson.oss.air.aas.handler.NotificationHandler;
import com.ericsson.oss.air.aas.handler.UpdateNotificationHandler;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Configuration;

/**
 * Creates NotificationHandler instance based on ardq notification type.
 */
@Configuration
@AllArgsConstructor
public class NotificationHandlerConfiguration {

    protected CreateNotificationHandler createNotificationHandler;

    protected UpdateNotificationHandler updateNotificationHandler;

    protected DeleteNotificationHandler deleteNotificationHandler;

    /**
     * Create and return appropriate NotificationHandler based on ardq notification type.
     *
     * @param ardqNotificationType {@link ArdqNotificationType}
     * @return {@link NotificationHandler}
     */
    public NotificationHandler getHandler(final ArdqNotificationType ardqNotificationType) {

        switch (ardqNotificationType) {

            case UPDATE:

                return this.updateNotificationHandler;

            case DELETE:

                return this.deleteNotificationHandler;

            default:

                return this.createNotificationHandler;

        }
    }
}

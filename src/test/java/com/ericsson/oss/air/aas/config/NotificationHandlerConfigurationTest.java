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
import com.ericsson.oss.air.aas.handler.UpdateNotificationHandler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class NotificationHandlerConfigurationTest {

    @Mock
    private CreateNotificationHandler createNotificationHandler;

    @Mock
    private UpdateNotificationHandler updateNotificationHandler;

    @Mock
    private DeleteNotificationHandler deleteNotificationHandler;


    @InjectMocks
    private NotificationHandlerConfiguration notificationHandlerConfiguration;

    @Test
    void getHandler_createTypeNotification() {
        Assertions.assertEquals(
                CreateNotificationHandler.class,
                this.notificationHandlerConfiguration.getHandler(ArdqNotificationType.CREATE).getClass()
        );
    }

    @Test
    void getHandler_updateTypeNotification() {
        Assertions.assertEquals(
                UpdateNotificationHandler.class,
                this.notificationHandlerConfiguration.getHandler(ArdqNotificationType.UPDATE).getClass()
        );
    }

    @Test
    void getHandler_deleteTypeNotification() {
        Assertions.assertEquals(
                DeleteNotificationHandler.class,
                this.notificationHandlerConfiguration.getHandler(ArdqNotificationType.DELETE).getClass()
        );
    }
}

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

import static com.ericsson.oss.air.aas.handler.RegistrationTestUtil.DEPRECATED_SCHEMA_REFERENCE;
import static com.ericsson.oss.air.aas.handler.RegistrationTestUtil.VALID_DELETE_ARDQ_REGISTRATION_NOTIFICATION;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DeleteNotificationHandlerTest {

    @Mock
    private AugmentationWorkflowHandler augmentationWorkflowHandler;

    @InjectMocks
    private DeleteNotificationHandler notificationHandler;

    @Test
    public void apply_general_pass() {

        this.notificationHandler.apply(VALID_DELETE_ARDQ_REGISTRATION_NOTIFICATION);

        verify(this.augmentationWorkflowHandler, times(1)).stop(DEPRECATED_SCHEMA_REFERENCE);

    }

}

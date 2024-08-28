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

import static com.ericsson.oss.air.aas.handler.RegistrationTestUtil.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Optional;

import com.ericsson.oss.air.aas.config.kafka.dynamic.APKafkaConsumerRegistrar;
import com.ericsson.oss.air.aas.generated.model.notification.ArdqNotificationType;
import com.ericsson.oss.air.aas.generated.model.notification.ArdqRegistrationNotification;
import com.ericsson.oss.air.aas.repository.ArdqRegistrationDao;
import com.ericsson.oss.air.aas.repository.SchemaDao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class UpdateNotificationHandlerTest {

    private static final String INPUT_SCHEMA_CONSUMER_ID = "5GPMCOUNTERSAMFMobilityNetworkSlice1";

    private static final String DEPRECATED_SCHEMA_CONSUMER_ID = "5GDEPRECATEDAMFMobilityNetworkSlice1";

    @Mock
    private ArdqRegistrationDao registrationDao;

    @Mock
    private SchemaDao schemaDao;

    @Mock
    private APKafkaConsumerRegistrar registrar;

    @Mock
    private AugmentationWorkflowHandler augmentationWorkflowHandler;

    @InjectMocks
    private UpdateNotificationHandler notificationHandler;

    @Test
    public void apply_general_pass() {

        when(this.registrationDao.findByArdqId(ARDQ_ID)).thenReturn(Optional.of(VALID_REGISTRATION_DTO));

        this.notificationHandler.apply(VALID_UPDATE_ARDQ_REGISTRATION_NOTIFICATION);

        verify(this.augmentationWorkflowHandler, times(1)).stop(DEPRECATED_SCHEMA_REFERENCE);
        verify(this.augmentationWorkflowHandler, times(1)).create(INPUT_SCHEMA_REFERENCE);
        verify(this.augmentationWorkflowHandler, times(0)).update(INPUT_SCHEMA_REFERENCE);

    }

    @Test
    public void test_apply_NoRegistrationError() {
        when(this.registrationDao.findByArdqId(ARDQ_ID)).thenReturn(Optional.empty());
        this.notificationHandler.apply(VALID_UPDATE_ARDQ_REGISTRATION_NOTIFICATION);
        assertFalse(this.registrationDao.retrieveRegistrationsByInputSchemaRef(INPUT_SCHEMA_REFERENCE).isPresent());
    }

    @Test
    public void apply_noDeprecatedInputSchema_pass() {

        when(this.registrationDao.findByArdqId(ARDQ_ID)).thenReturn(Optional.of(VALID_REGISTRATION_DTO));

        final ArdqRegistrationNotification notification = ArdqRegistrationNotification.newBuilder()
                .setArdqId(ARDQ_ID)
                .setArdqNotificationType(ArdqNotificationType.UPDATE)
                .setDeprecatedInputSchemas(new ArrayList<>())
                .build();

        this.notificationHandler.apply(notification);

        verify(this.augmentationWorkflowHandler, times(0)).stop(DEPRECATED_SCHEMA_REFERENCE);
        verify(this.augmentationWorkflowHandler, times(1)).create(INPUT_SCHEMA_REFERENCE);
        verify(this.augmentationWorkflowHandler, times(0)).update(INPUT_SCHEMA_REFERENCE);

    }

    @Test
    public void apply_updateExistingAugmentationWorkflow_pass() {

        when(this.registrationDao.findByArdqId(ARDQ_ID)).thenReturn(Optional.of(VALID_REGISTRATION_DTO));
        when(this.augmentationWorkflowHandler.isCreated(INPUT_SCHEMA_REFERENCE)).thenReturn(true);

        final ArdqRegistrationNotification notification = ArdqRegistrationNotification.newBuilder()
                .setArdqId(ARDQ_ID)
                .setArdqNotificationType(ArdqNotificationType.UPDATE)
                .setDeprecatedInputSchemas(new ArrayList<>())
                .build();

        this.notificationHandler.apply(notification);

        verify(this.augmentationWorkflowHandler, times(0)).create(INPUT_SCHEMA_REFERENCE);
        verify(this.augmentationWorkflowHandler, times(1)).update(INPUT_SCHEMA_REFERENCE);

    }

}

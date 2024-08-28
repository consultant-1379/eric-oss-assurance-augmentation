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

import static com.ericsson.oss.air.aas.handler.RegistrationTestUtil.ARDQ_ID;
import static com.ericsson.oss.air.aas.handler.RegistrationTestUtil.INPUT_SCHEMA_REFERENCE;
import static com.ericsson.oss.air.aas.handler.RegistrationTestUtil.ONE_RULE_REGISTRATION_DTO;
import static com.ericsson.oss.air.aas.handler.RegistrationTestUtil.OUTPUT_SCHEMA_REFERENCE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import com.ericsson.oss.air.aas.generated.model.notification.ArdqNotificationType;
import com.ericsson.oss.air.aas.generated.model.notification.ArdqRegistrationNotification;
import com.ericsson.oss.air.aas.handler.registration.validation.StringObjectMissingValidator;
import com.ericsson.oss.air.aas.repository.ArdqRegistrationDao;
import com.ericsson.oss.air.aas.repository.SchemaDao;
import com.ericsson.oss.air.aas.service.ConfigurationNotificationService;
import com.ericsson.oss.air.exception.http.problem.exception.HttpNotFoundRequestProblemException;
import io.micrometer.core.instrument.Counter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DeleteArdqRegistrationHandlerTest {

    @Mock
    private ArdqRegistrationDao ardqRegistrationDao;

    @Mock
    private SchemaDao schemaDao;

    @Mock
    private StringObjectMissingValidator validator;

    @Mock
    private ConfigurationNotificationService configurationNotificationService;

    @Mock
    private Counter deletedArdqRegistrationsCounter;

    @InjectMocks
    private DeleteArdqRegistrationHandler deleteHandler;

    @BeforeEach
    void setUp() {
        lenient().when(this.ardqRegistrationDao.findByArdqId(ARDQ_ID)).thenReturn(Optional.of(ONE_RULE_REGISTRATION_DTO));
        lenient().when(this.schemaDao.getOutputSchemaReferenceList(ARDQ_ID)).thenReturn(List.of(OUTPUT_SCHEMA_REFERENCE));
    }

    @Test
    public void test_handle_no_referenced() {

        when(this.schemaDao.getAffectedArdqRegistrationIds(INPUT_SCHEMA_REFERENCE)).thenReturn(List.of(ARDQ_ID));

        assertDoesNotThrow(() -> deleteHandler.handle(ARDQ_ID));

        verify(this.schemaDao, times(1)).deleteSchema(INPUT_SCHEMA_REFERENCE);
        verify(this.schemaDao, times(1)).deleteSchema(OUTPUT_SCHEMA_REFERENCE);

        final ArgumentCaptor<ArdqRegistrationNotification> arg = ArgumentCaptor.forClass(ArdqRegistrationNotification.class);
        verify(this.configurationNotificationService, times(1)).notify(arg.capture());
        verify(this.deletedArdqRegistrationsCounter, times(1)).increment();

        assertEquals(ArdqNotificationType.DELETE, arg.getValue().getArdqNotificationType());
        assertEquals(ARDQ_ID, arg.getValue().getArdqId());
        assertThat(arg.getValue().getDeprecatedInputSchemas())
                .contains(INPUT_SCHEMA_REFERENCE);
    }

    @Test
    public void test_handle_schemaRef_referenced() {

        when(this.schemaDao.getAffectedArdqRegistrationIds(INPUT_SCHEMA_REFERENCE)).thenReturn(List.of(ARDQ_ID, "ANOTHER_ARDQ_ID"));

        assertDoesNotThrow(() -> deleteHandler.handle(ARDQ_ID));

        // When Input schema is referenced by another ARDQ registration, no deletion required
        verify(this.schemaDao, never()).deleteSchema(INPUT_SCHEMA_REFERENCE);
        // All output schema related current ARDQ registration should be removed
        verify(this.schemaDao, times(1)).deleteSchema(OUTPUT_SCHEMA_REFERENCE);

        final ArgumentCaptor<ArdqRegistrationNotification> arg = ArgumentCaptor.forClass(ArdqRegistrationNotification.class);
        verify(this.configurationNotificationService, times(1)).notify(arg.capture());
        verify(this.deletedArdqRegistrationsCounter, times(1)).increment();

        assertEquals(ArdqNotificationType.DELETE, arg.getValue().getArdqNotificationType());
        assertEquals(ARDQ_ID, arg.getValue().getArdqId());
        assertThat(arg.getValue().getDeprecatedInputSchemas())
                .doesNotContain(INPUT_SCHEMA_REFERENCE);
    }

    @Test
    public void test_handle_exception() {

        when(this.validator.test(ARDQ_ID)).thenThrow(HttpNotFoundRequestProblemException.builder().description("ArdqId: cardq not found").build());

        assertThrows(HttpNotFoundRequestProblemException.class, () -> deleteHandler.handle(ARDQ_ID));

        verify(this.configurationNotificationService, never()).notify(any());
        verify(this.deletedArdqRegistrationsCounter, never()).increment();
    }

}
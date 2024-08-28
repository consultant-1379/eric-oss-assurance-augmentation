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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import com.ericsson.oss.air.aas.generated.model.notification.ArdqNotificationType;
import com.ericsson.oss.air.aas.generated.model.notification.ArdqRegistrationNotification;
import com.ericsson.oss.air.aas.handler.registration.validation.ArdqRegistrationDtoValidator;
import com.ericsson.oss.air.aas.handler.registration.validation.AugmentationRulesValidator;
import com.ericsson.oss.air.aas.handler.registration.validation.ObjectConflictValidator;
import com.ericsson.oss.air.aas.handler.registration.validation.UrlValidator;
import com.ericsson.oss.air.aas.repository.ArdqRegistrationDao;
import com.ericsson.oss.air.aas.repository.SchemaDao;
import com.ericsson.oss.air.aas.service.ConfigurationNotificationService;
import com.ericsson.oss.air.api.generated.model.ArdqAugmentationFieldDto;
import com.ericsson.oss.air.api.generated.model.ArdqAugmentationRuleDto;
import com.ericsson.oss.air.api.generated.model.ArdqRegistrationDto;
import com.ericsson.oss.air.exception.http.problem.exception.HttpConflictRequestProblemException;
import io.micrometer.core.instrument.Counter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateArdqRegistrationHandlerTest {

    @Mock
    private ArdqRegistrationDtoValidator validator;

    @Mock
    private ArdqRegistrationDao ardqRegMockDao;

    @Mock
    private SchemaDao schemaMockDao;

    @Mock
    private AugmentSchemaHandler augmentSchemaHandler;

    @Mock
    private ConfigurationNotificationService configurationNotificationService;

    @Mock
    private Counter createdArdqRegistrationsCounter;

    private CreateArdqRegistrationHandler handler;

    @BeforeEach
    void setUp() {
        this.handler = new CreateArdqRegistrationHandler(this.validator, this.configurationNotificationService, this.ardqRegMockDao,
                this.schemaMockDao, this.augmentSchemaHandler, this.createdArdqRegistrationsCounter);
    }

    private static ArdqRegistrationDto prepareArdqRegistrationDto() {
        final ArdqAugmentationFieldDto ardqAugmentationFieldDto = new ArdqAugmentationFieldDto();
        ardqAugmentationFieldDto.setOutput("Output_Field_1");
        ardqAugmentationFieldDto.setInput(List.of("Input_Field_1, Input_Field_2"));

        final ArdqAugmentationRuleDto ardqAugmentationRuleDto = new ArdqAugmentationRuleDto();
        ardqAugmentationRuleDto.setInputSchema("Input_Schema");
        ardqAugmentationRuleDto.setFields(List.of(ardqAugmentationFieldDto));

        final ArdqRegistrationDto ardqRegistrationDto = new ArdqRegistrationDto();
        ardqRegistrationDto.setArdqUrl("http://eric-oss-cardq:8080");
        ardqRegistrationDto.setArdqId("cardq");
        ardqRegistrationDto.addRulesItem(ardqAugmentationRuleDto);

        return ardqRegistrationDto;
    }

    @Test
    void handle() {
        when(this.validator.test(ArgumentMatchers.any(ArdqRegistrationDto.class))).thenReturn(true);

        final ArdqRegistrationDto ardqRegistrationDto = CreateArdqRegistrationHandlerTest.prepareArdqRegistrationDto();
        assertDoesNotThrow(() -> handler.handle(ardqRegistrationDto));
        verify(this.createdArdqRegistrationsCounter, times(1)).increment();
    }

    @Test
    void handle_ValidatorConstruction() {
        final UrlValidator urlValidator = new UrlValidator() {
            @Override
            public boolean test(final ArdqRegistrationDto regDto) {
                return true;
            }
        };

        final AugmentationRulesValidator augmentationRulesValidator = new AugmentationRulesValidator() {
            @Override
            public boolean test(final ArdqRegistrationDto ardqRegistrationDto) {
                return false;
            }
        };

        final ObjectConflictValidator objectConflictValidator = new ObjectConflictValidator(this.ardqRegMockDao) {
            @Override
            public boolean test(final ArdqRegistrationDto ardqRegistrationDto) {
                return true;
            }
        };

        final CreateArdqRegistrationHandler handler = new CreateArdqRegistrationHandler(objectConflictValidator, urlValidator,
                augmentationRulesValidator, this.ardqRegMockDao, this.schemaMockDao, this.augmentSchemaHandler,
                this.configurationNotificationService, this.createdArdqRegistrationsCounter);
        final ArdqRegistrationDto ardqRegistrationDto = prepareArdqRegistrationDto();

        assertDoesNotThrow(() -> handler.handle(ardqRegistrationDto));
        verify(this.createdArdqRegistrationsCounter, times(1)).increment();
    }

    @Test
    void handle_exception() {
        when(this.validator.test(ArgumentMatchers.any(ArdqRegistrationDto.class))).thenThrow(
                HttpConflictRequestProblemException.builder().description("Provided ARDQ ID: id already exists").build());

        assertThrows(HttpConflictRequestProblemException.class, () -> handler.handle(new ArdqRegistrationDto()));
        verify(this.configurationNotificationService, times(0)).notify(any());
        verify(this.createdArdqRegistrationsCounter, never()).increment();
    }

    @Test
    void handle_validContext_notifyCalled() {
        when(this.validator.test(ArgumentMatchers.any(ArdqRegistrationDto.class))).thenReturn(true);

        final ArdqRegistrationDto ardqRegistrationDto = CreateArdqRegistrationHandlerTest.prepareArdqRegistrationDto();
        handler.handle(ardqRegistrationDto);

        final ArgumentCaptor<ArdqRegistrationNotification> arg = ArgumentCaptor.forClass(ArdqRegistrationNotification.class);
        verify(this.configurationNotificationService, times(1)).notify(arg.capture());

        assertEquals(ardqRegistrationDto.getArdqId(), arg.getValue().getArdqId());
        assertEquals(ArdqNotificationType.CREATE, arg.getValue().getArdqNotificationType());
        verify(this.createdArdqRegistrationsCounter, times(1)).increment();
    }

}

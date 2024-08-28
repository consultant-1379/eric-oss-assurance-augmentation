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

import static com.ericsson.oss.air.aas.handler.RegistrationTestUtil.FIELDS;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.ericsson.oss.air.aas.handler.registration.validation.ArdqRegistrationDtoValidator;
import com.ericsson.oss.air.aas.handler.registration.validation.AugmentationRulesValidator;
import com.ericsson.oss.air.aas.handler.registration.validation.ObjectMissingValidator;
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
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpdateArdqRegistrationHandlerTest {

    @Mock
    private ArdqRegistrationDtoValidator validator;

    @Mock
    private ArdqRegistrationDao ardqRegMockDao;

    @Mock
    private SchemaDao schemaMockDao;

    @Mock
    private ConfigurationNotificationService service;

    @Mock
    private AugmentSchemaHandler augmentSchemaHandler;

    @Mock
    private Counter updatedArdqRegistrationsCounter;

    private UpdateArdqRegistrationHandler handler;

    @BeforeEach
    void setUp() {
        this.handler = new UpdateArdqRegistrationHandler(this.validator, this.ardqRegMockDao, this.schemaMockDao, this.augmentSchemaHandler,
                this.service, this.updatedArdqRegistrationsCounter);
    }

    @Test
    void handle_EmptyRequest() {
        when(this.validator.test(ArgumentMatchers.any(ArdqRegistrationDto.class))).thenReturn(true);

        assertDoesNotThrow(() -> this.handler.handle(new ArdqRegistrationDto()));
        verify(this.updatedArdqRegistrationsCounter, never()).increment();

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

        final ObjectMissingValidator objectMissingValidator = new ObjectMissingValidator(this.ardqRegMockDao) {
            @Override
            public boolean test(final ArdqRegistrationDto ardqRegistrationDto) {
                return true;
            }
        };

        final UpdateArdqRegistrationHandler handler = new UpdateArdqRegistrationHandler(objectMissingValidator, urlValidator,
                augmentationRulesValidator, this.ardqRegMockDao, this.schemaMockDao, this.service,
                this.augmentSchemaHandler, this.updatedArdqRegistrationsCounter);

        assertDoesNotThrow(() -> handler.handle(new ArdqRegistrationDto()));
        verify(this.updatedArdqRegistrationsCounter, never()).increment();
    }

    @Test
    void handle_exception() {
        when(this.validator.test(ArgumentMatchers.any(ArdqRegistrationDto.class))).thenThrow(
                HttpConflictRequestProblemException.builder().build());
        final ArdqRegistrationDto ardqRegistrationDto = new ArdqRegistrationDto();

        assertThrows(HttpConflictRequestProblemException.class, () -> handler.handle(ardqRegistrationDto));
        verify(this.service, never()).notify(any());
        verify(this.updatedArdqRegistrationsCounter, never()).increment();
    }

    @Test
    void handle_comparing_identical_registrations() {
        final ArdqAugmentationRuleDto ardqAugmentationRuleDto = new ArdqAugmentationRuleDto().fields(FIELDS);
        final ArdqRegistrationDto ardqRegistrationDto = new ArdqRegistrationDto().ardqId("ardq_1")
                .ardqUrl("url").addRulesItem(ardqAugmentationRuleDto);
        final ArdqRegistrationDto existingRegistrationDto = new ArdqRegistrationDto().ardqId("ardq_1").ardqUrl("url")
                .addRulesItem(ardqAugmentationRuleDto);

        when(this.validator.test(ArgumentMatchers.any(ArdqRegistrationDto.class))).thenReturn(true);

        when(ardqRegMockDao.findByArdqId("ardq_1")).thenReturn(Optional.of(existingRegistrationDto));

        handler.handle(ardqRegistrationDto);
        verify(this.updatedArdqRegistrationsCounter, never()).increment();
    }

    @Test
    void handle_comparing_not_identical_registrations() {
        final ArdqAugmentationRuleDto ardqAugmentationRuleDto = new ArdqAugmentationRuleDto().fields(FIELDS);
        final ArdqRegistrationDto ardqRegistrationDto = new ArdqRegistrationDto().ardqId("ardq_1").ardqUrl("url")
                .addRulesItem(ardqAugmentationRuleDto);
        final ArdqRegistrationDto existingRegistrationDto = new ArdqRegistrationDto().ardqId("ardq_1").ardqUrl("url");

        when(this.validator.test(ArgumentMatchers.any(ArdqRegistrationDto.class))).thenReturn(true);

        when(this.ardqRegMockDao.findByArdqId("ardq_1")).thenReturn(Optional.of(existingRegistrationDto));

        this.handler.handle(ardqRegistrationDto);
        verify(this.service, times(1)).notify(any());
        verify(this.updatedArdqRegistrationsCounter, times(1)).increment();

    }

    @Test
    void testDeprecationCandidateInputSchemaListGeneration_givenOldRegistrationHasRulesWithAnInputSchemaWhichIsNotInRulesOfNewRegistration_shouldReturnListContainingThatSchema() {
        final ArdqRegistrationDto oldRegistration = new ArdqRegistrationDto()
                .ardqId("test-ardq-id")
                .ardqUrl("test-url")
                .addRulesItem(new ArdqAugmentationRuleDto().inputSchema("test-schema")
                        .addFieldsItem(new ArdqAugmentationFieldDto().addInputItem("snssai")
                                .addInputItem("moFDN").output("nsi")));
        final ArdqRegistrationDto newRegistration = new ArdqRegistrationDto()
                .ardqId("test-ardq-id")
                .ardqUrl("test-url")
                .addRulesItem(new ArdqAugmentationRuleDto().inputSchema("test-new-schema")
                        .addFieldsItem(new ArdqAugmentationFieldDto().addInputItem("new-input")
                                .addInputItem("other-new-input").output("new-output")));
        final List<String> removedInputSchemaReferenceList = this.handler.getRemovedInputSchemaReferenceList(newRegistration, oldRegistration);
        final List<String> deprecatedInputSchemaReferenceList = this.handler.getDeprecatedInputSchemaReferenceList(removedInputSchemaReferenceList,
                newRegistration.getArdqId());

        assertEquals(List.of("test-schema"), deprecatedInputSchemaReferenceList);
    }

    @Test
    void testDeprecationCandidateInputSchemaListGeneration_givenOldRegistrationHasRulesWithAnInputSchemaWhichIsAlsoInRulesOfNewRegistration_shouldReturnEmptyList() {
        final ArdqRegistrationDto oldRegistration = new ArdqRegistrationDto()
                .ardqId("test-ardq-id")
                .ardqUrl("test-url")
                .addRulesItem(new ArdqAugmentationRuleDto().inputSchema("test-schema")
                        .addFieldsItem(new ArdqAugmentationFieldDto().addInputItem("snssai")
                                .addInputItem("moFDN").output("nsi")));
        final ArdqRegistrationDto newRegistration = new ArdqRegistrationDto()
                .ardqId("test-ardq-id")
                .ardqUrl("test-url")
                .addRulesItem(new ArdqAugmentationRuleDto().inputSchema("test-schema")
                        .addFieldsItem(new ArdqAugmentationFieldDto().addInputItem("new-input")
                                .addInputItem("other-new-input").output("new-output")));

        final List<String> removedInputSchemaReferenceList = this.handler.getRemovedInputSchemaReferenceList(newRegistration, oldRegistration);
        final List<String> deprecatedInputSchemaReferenceList = this.handler.getDeprecatedInputSchemaReferenceList(removedInputSchemaReferenceList,
                newRegistration.getArdqId());

        assertTrue(deprecatedInputSchemaReferenceList.isEmpty());
    }

    @Test
    void testDeprecationCandidateInputSchemaListGeneration_givenOldRegistrationHasRulesWithTwoInputSchemasWhichAreNotInRulesOfNewRegistration_shouldReturnListContainingThoseTwoSchemas() {
        final ArdqRegistrationDto oldRegistration = new ArdqRegistrationDto()
                .ardqId("test-ardq-id")
                .ardqUrl("test-url")
                .addRulesItem(new ArdqAugmentationRuleDto().inputSchema("test-schema")
                        .addFieldsItem(new ArdqAugmentationFieldDto().addInputItem("snssai")
                                .addInputItem("moFDN").output("nsi")))
                .addRulesItem(new ArdqAugmentationRuleDto().inputSchema("test-diff-schema")
                        .addFieldsItem(new ArdqAugmentationFieldDto().addInputItem("new-input")
                                .addInputItem("other-new-input").output("new-output")))
                .addRulesItem(new ArdqAugmentationRuleDto().inputSchema("test-other-schema")
                        .addFieldsItem(new ArdqAugmentationFieldDto().addInputItem("snssai")
                                .addInputItem("moFDN").output("something-different-output")));
        final ArdqRegistrationDto newRegistration = new ArdqRegistrationDto()
                .ardqId("test-ardq-id")
                .ardqUrl("test-url")
                .addRulesItem(new ArdqAugmentationRuleDto().inputSchema("test-diff-schema")
                        .addFieldsItem(new ArdqAugmentationFieldDto().addInputItem("snssai")
                                .addInputItem("moFDN").output("nsi")));

        final List<String> removedInputSchemaReferenceList = this.handler.getRemovedInputSchemaReferenceList(newRegistration, oldRegistration);
        final List<String> deprecatedInputSchemaReferenceList = this.handler.getDeprecatedInputSchemaReferenceList(removedInputSchemaReferenceList,
                newRegistration.getArdqId());

        assertEquals(List.of("test-schema", "test-other-schema"), deprecatedInputSchemaReferenceList);
    }

    @Test
    void testDeprecatedInputSchemaListGeneration_givenRegistrationsReferencingAllDeprecationCandidateInputSchemas_shouldReturnEmptyList() {
        final ArdqRegistrationDto oldRegistration = new ArdqRegistrationDto()
                .ardqId("test-ardq-id")
                .ardqUrl("test-url")
                .addRulesItem(new ArdqAugmentationRuleDto().inputSchema("test-schema")
                        .addFieldsItem(new ArdqAugmentationFieldDto().addInputItem("snssai")
                                .addInputItem("moFDN").output("nsi")))
                .addRulesItem(new ArdqAugmentationRuleDto().inputSchema("test-diff-schema")
                        .addFieldsItem(new ArdqAugmentationFieldDto().addInputItem("new-input")
                                .addInputItem("other-new-input").output("new-output")))
                .addRulesItem(new ArdqAugmentationRuleDto().inputSchema("test-other-schema")
                        .addFieldsItem(new ArdqAugmentationFieldDto().addInputItem("snssai")
                                .addInputItem("moFDN").output("something-different-output")));
        final ArdqRegistrationDto newRegistration = new ArdqRegistrationDto()
                .ardqId("test-ardq-id")
                .ardqUrl("test-url")
                .addRulesItem(new ArdqAugmentationRuleDto().inputSchema("test-diff-schema")
                        .addFieldsItem(new ArdqAugmentationFieldDto().addInputItem("snssai")
                                .addInputItem("moFDN").output("nsi")));

        when(this.schemaMockDao.getAffectedArdqRegistrationIds("test-schema")).thenReturn(List.of("test-ardq-id", "some-other-ardq-id"));
        when(this.schemaMockDao.getAffectedArdqRegistrationIds("test-other-schema")).thenReturn(List.of("test-ardq-id", "some-other-ardq-id"));

        final List<String> removedInputSchemaReferenceList = this.handler.getRemovedInputSchemaReferenceList(newRegistration, oldRegistration);
        final List<String> deprecatedInputSchemaReferenceList = this.handler.getDeprecatedInputSchemaReferenceList(removedInputSchemaReferenceList,
                newRegistration.getArdqId());

        assertTrue(deprecatedInputSchemaReferenceList.isEmpty());
    }

    @Test
    void testDeprecatedInputSchemaListGeneration_givenRegistrationsGeneratingEmptyDeprecationCandidateInputSchemas_shouldReturnEmptyList() {
        final ArdqRegistrationDto oldRegistration = new ArdqRegistrationDto()
                .ardqId("test-ardq-id")
                .ardqUrl("test-url")
                .addRulesItem(new ArdqAugmentationRuleDto().inputSchema("test-schema")
                        .addFieldsItem(new ArdqAugmentationFieldDto().addInputItem("snssai")
                                .addInputItem("moFDN").output("nsi")));
        final ArdqRegistrationDto newRegistration = new ArdqRegistrationDto()
                .ardqId("test-ardq-id")
                .ardqUrl("test-url")
                .addRulesItem(new ArdqAugmentationRuleDto().inputSchema("test-schema")
                        .addFieldsItem(new ArdqAugmentationFieldDto().addInputItem("new-input")
                                .addInputItem("other-new-input").output("new-output")));

        final List<String> removedInputSchemaReferenceList = this.handler.getRemovedInputSchemaReferenceList(newRegistration, oldRegistration);
        final List<String> deprecatedInputSchemaReferenceList = this.handler.getDeprecatedInputSchemaReferenceList(removedInputSchemaReferenceList,
                newRegistration.getArdqId());

        assertTrue(deprecatedInputSchemaReferenceList.isEmpty());
    }

    @Test
    void testDeprecatedInputSchemaListGeneration_givenRegistrationsNotReferencingDeprecationCandidateInputSchemas_shouldReturnUnreferencedInputSchemas() {
        final ArdqRegistrationDto oldRegistration = new ArdqRegistrationDto()
                .ardqId("test-ardq-id")
                .ardqUrl("test-url")
                .addRulesItem(new ArdqAugmentationRuleDto().inputSchema("old-schema1")
                        .addFieldsItem(new ArdqAugmentationFieldDto().addInputItem("new-input")
                                .addInputItem("other-new-input").output("new-output")))
                .addRulesItem(new ArdqAugmentationRuleDto().inputSchema("old-schema2")
                        .addFieldsItem(new ArdqAugmentationFieldDto().addInputItem("new-input")
                                .addInputItem("other-new-input").output("new-test-output")));
        final ArdqRegistrationDto newRegistration = new ArdqRegistrationDto()
                .ardqId("test-ardq-id")
                .ardqUrl("test-url-new")
                .addRulesItem(new ArdqAugmentationRuleDto().inputSchema("old-schema2")
                        .addFieldsItem(new ArdqAugmentationFieldDto().addInputItem("snssai")
                                .addInputItem("moFDN").output("something-different-output")))
                .addRulesItem(new ArdqAugmentationRuleDto().inputSchema("new-schema1")
                        .addFieldsItem(new ArdqAugmentationFieldDto().addInputItem("new-input")
                                .addInputItem("other-new-input").output("new-test-output")));

        when(this.schemaMockDao.getAffectedArdqRegistrationIds("old-schema1")).thenReturn(List.of("test-ardq-id"));

        final List<String> removedInputSchemaReferenceList = this.handler.getRemovedInputSchemaReferenceList(newRegistration, oldRegistration);
        final List<String> deprecatedInputSchemaReferenceList = this.handler.getDeprecatedInputSchemaReferenceList(removedInputSchemaReferenceList,
                newRegistration.getArdqId());

        assertEquals(List.of("old-schema1"), deprecatedInputSchemaReferenceList);
    }

    @Test
    void testGetSchemaToDeleteList_withNonReferenceToOtherRegistration_shouldReturnCorrectList() {
        final List<String> nonReferencedInputSchemaReferenceList = List.of("test-schema", "test-other-schema");
        final List<String> schemaToDeleteList = this.handler.getSchemaToDeleteList(new ArrayList<>(), nonReferencedInputSchemaReferenceList,
                "test-ardq-id");

        assertEquals(nonReferencedInputSchemaReferenceList, schemaToDeleteList);
    }

    @Test
    void testGetSchemaToDeleteList_withReferenceToOtherRegistration_shouldReturnCorrectList() {
        when(this.schemaMockDao.getOutputSchemaReference("test-ardq-id", "test-schema")).thenReturn(Optional.ofNullable("outputSchema1"));
        when(this.schemaMockDao.getOutputSchemaReference("test-ardq-id", "test-other-schema")).thenReturn(Optional.ofNullable("outputSchema2"));

        final List<String> removedInputSchemaReferenceList = List.of("test-schema", "test-other-schema");
        final List<String> schemaToDeleteList = this.handler.getSchemaToDeleteList(removedInputSchemaReferenceList, singletonList("test-schema"),
                "test-ardq-id");

        assertEquals(3, schemaToDeleteList.size());
        assertEquals(Set.of("outputSchema1", "outputSchema2", "test-schema"), new HashSet<>(schemaToDeleteList));
    }
}
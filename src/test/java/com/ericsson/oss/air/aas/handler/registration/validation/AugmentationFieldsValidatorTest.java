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

package com.ericsson.oss.air.aas.handler.registration.validation;

import static com.ericsson.oss.air.aas.handler.RegistrationTestUtil.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import com.ericsson.oss.air.api.generated.model.ArdqAugmentationFieldDto;
import com.ericsson.oss.air.exception.http.problem.exception.HttpBadRequestProblemException;
import com.ericsson.oss.air.exception.http.problem.exception.HttpConflictRequestProblemException;
import org.junit.jupiter.api.Test;

public class AugmentationFieldsValidatorTest {
    private final String fieldName = "test_field";

    @Test
    void ensureInputFieldsInSchema_NoInputs() {
        assertDoesNotThrow(() -> AugmentationFieldsValidator.inputFieldValidator.test(List.of(new ArdqAugmentationFieldDto()), INPUT_SCHEMA));
    }

    @Test
    void ensureInputFieldsInSchemaTest() {
        ArdqAugmentationFieldDto fieldDto = new ArdqAugmentationFieldDto()
                .addInputItem(INPUT_FIELD2);

        assertDoesNotThrow(() -> AugmentationFieldsValidator.inputFieldValidator.test(List.of(fieldDto), INPUT_SCHEMA));
    }

    @Test
    void ensureInputFieldsInSchema_ThrowsException() {
        ArdqAugmentationFieldDto fieldDto = new ArdqAugmentationFieldDto()
                .addInputItem(fieldName);

        assertThrows(HttpBadRequestProblemException.class,
                () -> AugmentationFieldsValidator.inputFieldValidator.test(List.of(fieldDto), INPUT_SCHEMA));
    }

    @Test
    void verifyOutputFieldsInInputSchema_ShouldThrowException() {
        ArdqAugmentationFieldDto fieldDto = new ArdqAugmentationFieldDto()
                .output(INPUT_FIELD1);

        assertThrows(HttpBadRequestProblemException.class,
                () -> AugmentationFieldsValidator.outputFieldValidator.test(List.of(fieldDto), INPUT_SCHEMA));
    }

    @Test
    void verifyOutputFieldsInInputSchema_ShouldNotThrowException() {
        ArdqAugmentationFieldDto fieldDto = new ArdqAugmentationFieldDto()
                .output(fieldName);

        assertDoesNotThrow(() -> AugmentationFieldsValidator.outputFieldValidator.test(List.of(fieldDto), INPUT_SCHEMA));
    }

    @Test
    void validateFieldsConflicts() {
        List<ArdqAugmentationFieldDto> newFieldsList = List.of(
                new ArdqAugmentationFieldDto().input(List.of(INPUT_FIELD1, INPUT_FIELD2)).output(OUTPUT_FIELD1),
                new ArdqAugmentationFieldDto().input(List.of(fieldName)).output(fieldName));
        assertDoesNotThrow(() -> AugmentationFieldsValidator.validateFieldsConflict(newFieldsList, FIELDS));
    }

    @Test
    void validateFieldsConflicts_outputDoesNotExist_NoException() {
        List<ArdqAugmentationFieldDto> newFieldsList = List.of(
                new ArdqAugmentationFieldDto().input(List.of(INPUT_FIELD1, INPUT_FIELD2)).output("nsi"));
        assertDoesNotThrow(() -> AugmentationFieldsValidator.validateFieldsConflict(newFieldsList, FIELDS));
    }

    @Test
    void validateFieldsConflicts_differentTwoSets_ThrowException() {
        List<ArdqAugmentationFieldDto> newFieldsList = List.of(
                new ArdqAugmentationFieldDto().input(List.of(INPUT_FIELD3, fieldName)).output(OUTPUT_FIELD2));
        assertThrows(HttpConflictRequestProblemException.class,
                () -> AugmentationFieldsValidator.validateFieldsConflict(newFieldsList, FIELDS));
    }
}

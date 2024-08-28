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

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

import com.ericsson.oss.air.api.generated.model.ArdqAugmentationFieldDto;
import com.ericsson.oss.air.exception.http.problem.exception.HttpBadRequestProblemException;
import com.ericsson.oss.air.exception.http.problem.exception.HttpConflictRequestProblemException;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.Schema;

/**
 * Intended to validate the Augmentation fields for {@link ArdqAugmentationFieldDto ArdqAugmentationFieldDto}
 */
@Slf4j
public class AugmentationFieldsValidator {

    /**
     * Validates the input fields in list of {@link ArdqAugmentationFieldDto} exists in the schema
     */
    public static final BiPredicate<List<ArdqAugmentationFieldDto>, Schema> inputFieldValidator = (ardqAugmentationFieldDtos, schema) -> {
        final Set<String> schemaFields = getSchemaFieldsName(schema);
        ardqAugmentationFieldDtos.forEach(ardqAugmentationFieldDto -> {
            for (String name : ardqAugmentationFieldDto.getInput()) {
                if (!schemaFields.contains(name)) {
                    final String errorMsg = String.format("Validation failed: input field %s does not exist in the schema fields list %s", name,
                            schemaFields);
                    log.error(errorMsg);
                    throw HttpBadRequestProblemException.builder().description(errorMsg).build();
                }
            }
        });
        return true;
    };

    /**
     * Validates the output fields in list of {@link ArdqAugmentationFieldDto} does not exist in the schema
     */
    public static final BiPredicate<List<ArdqAugmentationFieldDto>, Schema> outputFieldValidator = (ardqAugmentationFieldDtos, schema) -> {
        final Set<String> schemaFields = getSchemaFieldsName(schema);
        ardqAugmentationFieldDtos.forEach(ardqAugmentationFieldDto -> {
            final String outputField = ardqAugmentationFieldDto.getOutput();
            if (schemaFields.contains(outputField)) {
                final String errorMsg = String.format("Validation failed: output field %s already exist in the schema fields list %s.", outputField,
                        schemaFields);
                log.error(errorMsg);
                throw HttpBadRequestProblemException.builder().description(errorMsg).build();
            }
        });
        return true;
    };

    /**
     * Validates the fields in list of {@link ArdqAugmentationFieldDto} based on the rules
     */
    public static final BiPredicate<List<ArdqAugmentationFieldDto>, Schema> fieldValidator = inputFieldValidator.and(outputFieldValidator);

    private static Set<String> getSchemaFieldsName(final Schema schema) {
        return schema.getFields().stream().map(Schema.Field::name).collect(Collectors.toSet());
    }

    /**
     * The method checks the conflicts between the new fields and existing fields. If the new fields are different from the existing ones, throw a
     * {@link HttpConflictRequestProblemException}
     *
     * @param updateFieldList    the new fields list
     * @param existingFieldsList the existing fields list
     */
    public static void validateFieldsConflict(final List<ArdqAugmentationFieldDto> updateFieldList,
                                              final List<ArdqAugmentationFieldDto> existingFieldsList) {

        updateFieldList.forEach(ardqAugmentationFieldDto -> {

            // Get ArdqAugmentationFieldDto with same output field
            final Optional<ArdqAugmentationFieldDto> matchedArdqAugmentationFieldDto = existingFieldsList.stream()
                    .filter(field -> field.getOutput().equals(ardqAugmentationFieldDto.getOutput())).findFirst();
            if (matchedArdqAugmentationFieldDto.isEmpty()) {
                return;
            }

            final Set<String> matchedInputFields = new HashSet<>(matchedArdqAugmentationFieldDto.get().getInput());
            final Set<String> inputFields = new HashSet<>(ardqAugmentationFieldDto.getInput());

            if (!matchedInputFields.equals(inputFields)) {
                final String errorMsg = String.format("Validation failed: the new fields %s are different from the existing fields %s", inputFields,
                        matchedInputFields);
                log.error(errorMsg);
                throw HttpConflictRequestProblemException.builder().description(errorMsg).build();
            }

        });
    }

    private AugmentationFieldsValidator() {
    }

}

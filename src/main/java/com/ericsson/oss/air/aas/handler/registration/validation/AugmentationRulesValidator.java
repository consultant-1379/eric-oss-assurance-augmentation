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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.ericsson.oss.air.api.generated.model.ArdqAugmentationFieldDto;
import com.ericsson.oss.air.api.generated.model.ArdqAugmentationRuleDto;
import com.ericsson.oss.air.api.generated.model.ArdqRegistrationDto;
import com.ericsson.oss.air.exception.http.problem.exception.HttpBadRequestProblemException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Intended to validate the augmentation rules for {@link ArdqRegistrationDto ArdqRegistrationDto's}.
 */
@Component
@Slf4j
public class AugmentationRulesValidator implements ArdqRegistrationDtoValidator {

    private static final String ERROR_MESSAGE_PREFIX = "Validation failed: invalid augmentation rules for input schema: ";
    private static final String INVALID_OUTPUT_FIELDS = "Output field names must be unique and cannot be the same as input field names. Invalid output fields: ";

    /*
     * (non-javadoc)
     *
     * Returns a list of output fields that are duplicates or already exist as input fields. Those fields will be treated as conflicting fields.
     */
    private static Set<String> getConflictingOutputFields(final List<String> outputFieldList, final List<String> inputFieldList) {

        // Find duplicate fields
        final Set<String> outputFieldSet = new HashSet<>();
        final Set<String> duplicateOutputFields = outputFieldList.stream()
                .filter(outputField -> !outputFieldSet.add(outputField))
                .collect(Collectors.toSet());

        // Find output field names that equal input field names
        final Set<String> conflictingFields = new HashSet<>(inputFieldList);
        conflictingFields.retainAll(outputFieldList);

        conflictingFields.addAll(duplicateOutputFields);

        return conflictingFields;
    }

    /*
     * (non-javadoc)
     *
     * Returns an optional that may contain an error message. If the optional is empty, then no rule conflicts were detected.
     */
    private static Optional<String> generateRuleConflictErrorMessage(final Set<String> conflictingOutputFields, final String inputSchema) {

        return conflictingOutputFields.isEmpty() ? Optional.empty() :
                Optional.of(ERROR_MESSAGE_PREFIX + inputSchema + ". " + INVALID_OUTPUT_FIELDS + conflictingOutputFields);

    }

    /**
     * Validates that the augmentation rules in the provided {@link ArdqRegistrationDto} do not conflict.
     * <p/>
     * Conflict definition:
     *
     * <ol>
     * <li>output fields must be unique for an input schema</li>
     * <li>input field names may not be the same as output field names for a given input schema</li>
     * </ol>
     *
     * @param ardqRegistrationDto the ARDQ registration to be validated
     */
    static void validate(final ArdqRegistrationDto ardqRegistrationDto) {

        if (Objects.isNull(ardqRegistrationDto)) {
            return;
        }

        final Map<String, List<ArdqAugmentationRuleDto>> groupedRules = ardqRegistrationDto.getRules().stream()
                .collect(Collectors.groupingBy(ArdqAugmentationRuleDto::getInputSchema));

        groupedRules.values().forEach(ardqAugmentationRuleDtos -> {

            final List<ArdqAugmentationFieldDto> fieldDtoList = ardqAugmentationRuleDtos.stream()
                    .map(ArdqAugmentationRuleDto::getFields)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());

            final List<String> outputFieldList = new ArrayList<>();
            final List<String> inputFieldList = new ArrayList<>();

            fieldDtoList.forEach(fieldDto -> {
                outputFieldList.add(fieldDto.getOutput());
                inputFieldList.addAll(fieldDto.getInput());
            });

            final Set<String> conflictingOutputFields = AugmentationRulesValidator.getConflictingOutputFields(outputFieldList, inputFieldList);

            final Optional<String> errorMessage = AugmentationRulesValidator.generateRuleConflictErrorMessage(conflictingOutputFields,
                    ardqAugmentationRuleDtos.get(0).getInputSchema());

            if (errorMessage.isPresent()) {
                log.error(errorMessage.get());
                throw HttpBadRequestProblemException.builder().description(errorMessage.get()).build();
            }

        });
    }

    @Override
    public boolean test(final ArdqRegistrationDto ardqRegistrationDto) {
        AugmentationRulesValidator.validate(ardqRegistrationDto);

        return true;
    }
}

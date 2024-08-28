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

package com.ericsson.oss.air.aas.repository.impl.inmemorydb;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.ericsson.oss.air.aas.repository.ArdqRegistrationDao;
import com.ericsson.oss.air.api.generated.model.ArdqAugmentationFieldDto;
import com.ericsson.oss.air.api.generated.model.ArdqAugmentationRuleDto;
import com.ericsson.oss.air.api.generated.model.ArdqRegistrationDto;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.util.ObjectUtils;

/**
 * ArdqRegistration in memory db to store for sharable resources.
 * <p>
 * This in memory db implementation will contain all valid resources regardless of whether they have been instantiated in a downstream system.
 */
@Repository
@AllArgsConstructor
@Slf4j
public class ArdqRegistrationDaoImpl implements ArdqRegistrationDao {

    private final Map<String, ArdqRegistrationDto> ardqRegistrationByArdqId = new HashMap<>();

    @Override
    public Optional<ArdqRegistrationDto> findByArdqId(final String ardqId) {
        return Optional.ofNullable(this.ardqRegistrationByArdqId.get(ardqId));
    }

    @Override
    public void saveArdqRegistration(final ArdqRegistrationDto ardqRegistrationDto) {
        this.ardqRegistrationByArdqId.put(ardqRegistrationDto.getArdqId(), ardqRegistrationDto);
    }

    @Override
    public Optional<List<ArdqAugmentationFieldDto>> getAugmentationFields(final String ardqRegistrationId, final String inputSchemaReference) {
        final ArdqRegistrationDto registrationDto = this.ardqRegistrationByArdqId.get(ardqRegistrationId);
        if (ObjectUtils.isEmpty(registrationDto)) {
            return Optional.empty();
        }

        // For each unique input schema in the ARDQ registration
        final List<ArdqAugmentationFieldDto> fieldsPerInputSchemaRefMap = this.getFieldsFromRegistrationByInputSchemaReference(registrationDto,
                inputSchemaReference);

        if (fieldsPerInputSchemaRefMap.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(fieldsPerInputSchemaRefMap);
    }

    @Override
    public Optional<List<ArdqRegistrationDto>> getAllArdqRegistrations() {
        return Optional.of(new ArrayList<>(this.ardqRegistrationByArdqId.values()));
    }

    @Override
    public Optional<Integer> getTotalRegistrations() {
        return Optional.of(this.ardqRegistrationByArdqId.values().size());
    }

    @Override
    public Optional<List<ArdqRegistrationDto>> retrieveRegistrationsByInputSchemaRef(final String schemaRef) {
        final List<ArdqRegistrationDto> registrationDtoList = new ArrayList<>();

        this.ardqRegistrationByArdqId.forEach((ardqId, ardqRegistrationDto) -> {
            for (final ArdqAugmentationRuleDto rule : ardqRegistrationDto.getRules()) {
                if (schemaRef.equals(rule.getInputSchema())) {
                    registrationDtoList.add(ardqRegistrationDto);
                    break;
                }
            }
        });

        if (ObjectUtils.isEmpty(registrationDtoList)) {
            return Optional.empty();
        }

        return Optional.of(registrationDtoList);
    }

    @Override
    public Integer deleteRegistrationByArdqId(final String ardqId) {
        final Optional<ArdqRegistrationDto> registrationDto = Optional.ofNullable(this.ardqRegistrationByArdqId.remove(ardqId));

        return registrationDto.isPresent() ? 1 : 0;
    }

    private List<ArdqAugmentationFieldDto> getFieldsFromRegistrationByInputSchemaReference(final ArdqRegistrationDto ardqRegistrationDto,
                                                                                           final String inputSchemaReference) {
        return ardqRegistrationDto.getRules()
                .stream()
                .filter(ardqAugmentationRuleDto -> ardqAugmentationRuleDto.getInputSchema().equals(inputSchemaReference))
                .map(ArdqAugmentationRuleDto::getFields)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

}

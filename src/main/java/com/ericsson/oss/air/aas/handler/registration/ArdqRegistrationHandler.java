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

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import com.ericsson.oss.air.aas.repository.SchemaDao;
import com.ericsson.oss.air.api.generated.model.ArdqAugmentationRuleDto;
import com.ericsson.oss.air.api.generated.model.ArdqRegistrationDto;

/**
 * This class contains the common functionalities for ARDQ registration.
 */
public class ArdqRegistrationHandler {

    /**
     * Returns non-referenced input schema reference list.
     * <p>
     * For each candidate in the deprecated input schema list, check if there are any ARDQ registration referring to same input schema reference
     * If there is a reference from any other ARDQ registration, remove from the deprecated list.
     *
     * @param schemaDao                   the schema dao
     * @param candidateInputSchemaRefList deprecated candidate input schema reference list
     * @param ardqId                      the ardq id
     * @return a list of on-referenced input schema reference
     */
    List<String> getNonReferencedInputSchemaReferenceList(final SchemaDao schemaDao, final List<String> candidateInputSchemaRefList,
                                                          final String ardqId) {

        return candidateInputSchemaRefList.stream()
                .filter(inputSchema -> {
                    final HashSet<String> affectedArdqId = new HashSet<>(schemaDao.getAffectedArdqRegistrationIds(inputSchema));
                    affectedArdqId.remove(ardqId);
                    return affectedArdqId.isEmpty();
                })
                .collect(Collectors.toList());
    }

    /**
     * Gets a input schema list from a ArdqRegistrationDto object
     *
     * @param ardqRegistrationDto the ardq registration dto
     * @return the input schema list
     */
    List<String> getInputSchemaList(final ArdqRegistrationDto ardqRegistrationDto) {
        return ardqRegistrationDto
                .getRules()
                .stream()
                .map(ArdqAugmentationRuleDto::getInputSchema)
                .distinct()
                .collect(Collectors.toList());
    }
    
}

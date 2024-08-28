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

package com.ericsson.oss.air.aas.repository;

import java.util.List;
import java.util.Optional;

import com.ericsson.oss.air.api.generated.model.ArdqAugmentationFieldDto;
import com.ericsson.oss.air.api.generated.model.ArdqRegistrationDto;

/**
 * The interface for ArdqRegistration dao.
 */
public interface ArdqRegistrationDao {

    /**
     * Find ArdqRegistration Object by a ardq_id
     *
     * @param ardqId ArdqRegistration id
     * @return ArdqRegistration object, empty Optional returned if no matching registration is found
     */
    Optional<ArdqRegistrationDto> findByArdqId(final String ardqId);

    /**
     * Add ArdqRegistrationDto to Map, sorted by name
     *
     * @param ardqRegistrationDto The ArdqRegistrationDto
     */
    void saveArdqRegistration(final ArdqRegistrationDto ardqRegistrationDto);

    /**
     * Retrieves the list of all persisted ARDQ registrations
     *
     * @return the list of ARDQ registration DTOs
     */
    Optional<List<ArdqRegistrationDto>> getAllArdqRegistrations();

    /**
     * Gets total number registrations.
     *
     * @return the total registrations
     */
    Optional<Integer> getTotalRegistrations();

    /**
     * Retrieves the list of Registrations from the map based on the provided input schema reference
     *
     * @param schemaRef Input schema reference
     * @return List ArdqRegistrationDtos, empty Optional returned if no matching schema reference is found
     */
    Optional<List<ArdqRegistrationDto>> retrieveRegistrationsByInputSchemaRef(final String schemaRef);

    /**
     * Retrieves the list of augmentation field specifications by the ARDQ registration ID and the input schema reference
     *
     * @param ardqRegistrationId   is the ARDQ registration ID
     * @param inputSchemaReference is the reference to the input schema
     * @return the list of augmentation field specifications
     */
    Optional<List<ArdqAugmentationFieldDto>> getAugmentationFields(final String ardqRegistrationId, final String inputSchemaReference);

    /**
     * Deletes the ARDQ registrations using an ARDQ registration ID
     *
     * @param registrationId is the ARDQ registration ID
     * @return the number of registrations deleted
     */
    Integer deleteRegistrationByArdqId(String registrationId);

}

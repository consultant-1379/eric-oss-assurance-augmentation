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

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.ericsson.oss.air.aas.repository.ArdqRegistrationDao;
import com.ericsson.oss.air.aas.repository.SchemaDao;
import com.ericsson.oss.air.api.generated.model.ArdqRegistrationDto;
import com.ericsson.oss.air.api.generated.model.ArdqRegistrationResponseDto;
import com.ericsson.oss.air.api.generated.model.ArdqRegistrationSchemaMappingDto;
import com.ericsson.oss.air.exception.http.problem.exception.HttpNotFoundRequestProblemException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * A Handler for API requests
 */
@Component
@Slf4j
public class ApiRegistrationHandler {

    @Autowired
    private ArdqRegistrationDao ardqRegistrationDao;

    @Autowired
    private SchemaDao schemaDao;

    @Autowired
    private CreateArdqRegistrationHandler createArdqRegistrationHandler;

    @Autowired
    private DeleteArdqRegistrationHandler deleteArdqRegistrationHandler;

    @Autowired
    private UpdateArdqRegistrationHandler updateArdqRegistrationHandler;

    /**
     * Get the ArdqRegistration using the ardqId
     *
     * @param ardqId The Id of the ArdqRegistration
     * @return Return a ResponseEntity
     */
    public ArdqRegistrationResponseDto getArdqRegistrationById(final String ardqId) {
        final Optional<ArdqRegistrationDto> ardqRegistrationDto = ardqRegistrationDao.findByArdqId(ardqId);
        if (ardqRegistrationDto.isEmpty()) {
            throw HttpNotFoundRequestProblemException.builder()
                    .description(String.format("This id '%s' is not found in the current Registration", ardqId)).build();
        }
        final ArdqRegistrationResponseDto responseDto = new ArdqRegistrationResponseDto();
        responseDto.setArdqId(ardqRegistrationDto.get().getArdqId());
        responseDto.setArdqUrl(ardqRegistrationDto.get().getArdqUrl());
        responseDto.setArdqType(ardqRegistrationDto.get().getArdqType());
        responseDto.setRules(ardqRegistrationDto.get().getRules());
        responseDto.setSchemaMappings(getSchemaMappings(ardqRegistrationDto.get()));
        return responseDto;
    }

    /**
     * Get the list of ARDQ Registration IDs
     *
     * @return the list of ARDQ registration IDs persisted in the DB
     */
    public Optional<List<String>> getAllArdqRegistrationIds() {
        return ardqRegistrationDao.getAllArdqRegistrations().map(ardqRegistrationDtos ->
                ardqRegistrationDtos.stream().map(ArdqRegistrationDto::getArdqId).collect(Collectors.toList()));
    }

    /**
     * creates ARDQ Registration by calling create handler
     *
     * @param registrationDto RegistrationDto to register with AAS
     */
    public void createArdqRegistration(final ArdqRegistrationDto registrationDto) {
        createArdqRegistrationHandler.handle(registrationDto);
    }

    /**
     * deletes ARDQ Registration by calling delete handler
     *
     * @param ardqId registration Id to delete
     */
    public void deleteArdqRegistration(final String ardqId) {
        try {
            this.deleteArdqRegistrationHandler.handle(ardqId);
        } catch (final HttpNotFoundRequestProblemException e) {
            log.info("{} not found.", ardqId, e);
        }

    }

    /**
     * Updates ARDQ Registration by calling update handler
     *
     * @param registrationDto RegistrationDto to update with AAS
     */
    public void updateArdqRegistration(final ArdqRegistrationDto registrationDto) {
        updateArdqRegistrationHandler.handle(registrationDto);
    }

    private List<ArdqRegistrationSchemaMappingDto> getSchemaMappings(final ArdqRegistrationDto ardqRegistrationDto) {

        return this.schemaDao.getIOSchemas(ardqRegistrationDto.getArdqId())
                .stream()
                .map(ioSchema ->
                        new ArdqRegistrationSchemaMappingDto()
                                .inputSchema(ioSchema.getInputSchemaReference())
                                .outputSchema(ioSchema.getOutputSchemaReference()))
                .toList();

    }
}

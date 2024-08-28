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

import java.util.Optional;

import com.ericsson.oss.air.aas.repository.ArdqRegistrationDao;
import com.ericsson.oss.air.api.generated.model.ArdqRegistrationDto;
import com.ericsson.oss.air.exception.http.problem.exception.HttpConflictRequestProblemException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * A ArdqRegistrationDtoValidator verify existence of given ArdqRegistrationDto test operation will fail if given @{ArdqRegistrationDto} exists
 */
@Component
@Slf4j
public class ObjectConflictValidator implements ArdqRegistrationDtoValidator {

    private final ArdqRegistrationDao ardqRegDao;

    /**
     * Instantiates a new Id validator.
     *
     * @param ardqRegDao the ardq reg dao
     */
    @Autowired
    public ObjectConflictValidator(final ArdqRegistrationDao ardqRegDao) {
        this.ardqRegDao = ardqRegDao;
    }

    @Override
    public boolean test(final ArdqRegistrationDto ardqRegistrationDto) {
        final String ardqId = ardqRegistrationDto.getArdqId();
        final Optional<ArdqRegistrationDto> matchedArdqRegDto = this.ardqRegDao.findByArdqId(ardqId);

        if (matchedArdqRegDto.isPresent()) {
            final String errorMsg = String.format("Validation failed: provided ARDQ ID: [%s] already exists!", ardqId);
            log.error(errorMsg);
            throw HttpConflictRequestProblemException.builder().description(errorMsg).build();
        }

        return true;
    }
}

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
import com.ericsson.oss.air.exception.http.problem.exception.HttpNotFoundRequestProblemException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * A ArdqRegistrationDtoValidator verify existence of given ArdqRegistrationDto
 * test operation will fail if given @{ArdqRegistrationDto} exists
 */
@Component
public class ObjectMissingValidator implements ArdqRegistrationDtoValidator {

    private final ArdqRegistrationDao ardqRegDao;

    /**
     * Instantiates a new Id validator.
     *
     * @param ardqRegDao the ardq reg dao
     */
    @Autowired
    public ObjectMissingValidator(final ArdqRegistrationDao ardqRegDao) {
        this.ardqRegDao = ardqRegDao;
    }

    @Override
    public boolean test(final ArdqRegistrationDto ardqRegistrationDto) {
        final String ardqId = ardqRegistrationDto.getArdqId();
        final Optional<ArdqRegistrationDto> matchedArdqRegDto = this.ardqRegDao.findByArdqId(ardqId);

        if (matchedArdqRegDto.isEmpty()) {
            final String errorMsg = String.format("ARDQ Registration with ID: [%s] doesn't exists!", ardqId);
            throw HttpNotFoundRequestProblemException.builder().description(errorMsg).build();
        }

        return true;
    }
}

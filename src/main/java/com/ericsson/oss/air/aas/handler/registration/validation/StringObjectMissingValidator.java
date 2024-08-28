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
import java.util.function.Predicate;

import com.ericsson.oss.air.aas.repository.ArdqRegistrationDao;
import com.ericsson.oss.air.api.generated.model.ArdqRegistrationDto;
import com.ericsson.oss.air.exception.http.problem.exception.HttpNotFoundRequestProblemException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StringObjectMissingValidator implements Predicate<String> {

    private final ArdqRegistrationDao ardqRegDao;

    @Autowired
    public StringObjectMissingValidator(final ArdqRegistrationDao ardqRegDao) {
        this.ardqRegDao = ardqRegDao;
    }

    @Override
    public boolean test(final String ardqId) {
        final Optional<ArdqRegistrationDto> matchedArdqRegDto = this.ardqRegDao.findByArdqId(ardqId);

        if (matchedArdqRegDto.isEmpty()) {
            throw HttpNotFoundRequestProblemException.builder()
                    .description("ArdqId: " + ardqId + " not found")
                    .build();
        }

        return true;
    }
}

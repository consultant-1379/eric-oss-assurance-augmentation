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

import com.ericsson.oss.air.api.generated.model.ArdqRegistrationDto;
import com.ericsson.oss.air.util.UrlUtil;
import org.springframework.stereotype.Component;

@Component
public class UrlValidator implements ArdqRegistrationDtoValidator {

    @Override
    public boolean test(ArdqRegistrationDto regDto) {
        String url = regDto.getArdqUrl();
        UrlUtil.validateUrl(url);
        return true;
    }
}

/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.aas.service.ardq;

import com.ericsson.oss.air.aas.model.ardq.ArdqRequestDto;
import com.ericsson.oss.air.aas.model.ardq.ArdqResponseDto;
import com.ericsson.oss.air.exception.AasValidationException;
import com.ericsson.oss.air.util.LombokExtensions;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.micrometer.core.instrument.Counter;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.UnknownHttpStatusCodeException;

/**
 * This service takes care of submitting augmentation data query to external ARDQ microservice.
 */
@Slf4j
@RequiredArgsConstructor
@Service
@ExtensionMethod({ LombokExtensions.class })
public class ArdqService {

    static final String AUGMENTATION_URL = "/v1/augmentation-info/augmentation";

    @Autowired
    private final RestTemplate restTemplate;

    @Autowired
    private final Validator validator;

    @Autowired
    private final Counter ardqValidResponsesCounter;

    @Autowired
    private final Counter ardqInvalidResponsesCounter;

    @Autowired
    private final Counter ardqErrorResponsesCounter;

    /**
     * Submit request to get an augmentation data
     *
     * @param ardqUrl Full endpoint path to ARDQ service provider.
     * @param request Augmentation request to be sent to ARDQ service provider.
     */
    @CircuitBreaker(name = "ardq")
    @Retry(name = "ardq")
    public ResponseEntity<ArdqResponseDto> getAugmentationData(final String ardqUrl, final ArdqRequestDto request)
            throws HttpServerErrorException, HttpClientErrorException, UnknownHttpStatusCodeException, AasValidationException {

        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        final HttpEntity<ArdqRequestDto> entity = new HttpEntity<>(request, headers);

        this.restTemplate.setErrorHandler(new ArdqHttpResponseErrorHandler());

        log.info("Submitting ARDQ request to {} with content: {}", ardqUrl, request.toJsonString());
        final ResponseEntity<ArdqResponseDto> response;
        final ArdqResponseDto responseBody;
        try {
            response = this.restTemplate.exchange(ardqUrl + AUGMENTATION_URL, HttpMethod.POST, entity,
                    ArdqResponseDto.class);
            responseBody = response.getBody();
            if (!ObjectUtils.isEmpty(responseBody)) {
                log.info("Response received from ARDQ: {}", responseBody.toJsonString());
            }

        } catch (final Exception e) {
            this.ardqErrorResponsesCounter.increment();
            throw e;
        }


        final var violationSet = this.validator.validate(responseBody);

        if (!violationSet.isEmpty()) {
            this.ardqInvalidResponsesCounter.increment();
            throw new AasValidationException("The response received from ARDQ is invalid");
        }

        this.ardqValidResponsesCounter.increment();

        return response;
    }
}

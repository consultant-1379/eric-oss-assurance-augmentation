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

package com.ericsson.oss.air.aas.controller;

import java.util.List;

import com.ericsson.oss.air.aas.handler.registration.ApiRegistrationHandler;
import com.ericsson.oss.air.api.generated.ArdqRegistrationApi;
import com.ericsson.oss.air.api.generated.model.ArdqRegistrationDto;
import com.ericsson.oss.air.api.generated.model.ArdqRegistrationResponseDto;
import com.ericsson.oss.air.util.LombokExtensions;
import lombok.experimental.ExtensionMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * The Controller for the ArdqRegistration endpoints
 */
@RestController
@Slf4j
@ExtensionMethod(LombokExtensions.class)
public class ArdqRestController implements ArdqRegistrationApi {

    @Autowired
    private ApiRegistrationHandler handler;

    /***
     * The controller method for a GET request on /v1/augmentation/registration/ardq/{ardq_id}
     *
     * @param ardqId ID of a previously registered ARDQ rule set (required)
     * @return Returns an HTTP Response
     */
    @Override
    public ResponseEntity<ArdqRegistrationResponseDto> retrieveOneArdqRegistration(
            @PathVariable("ardq_id")
                    String ardqId) {
        log.info("Received GET request on /v1/augmentation/registration/ardq/{} ", ardqId);
        ArdqRegistrationResponseDto responseDto = handler.getArdqRegistrationById(ardqId);
        return ResponseEntity.ok().body(responseDto);
    }

    /**
     * The controller method for a GET request on /v1/augmentation/registration/ardq-ids
     *
     * @return Returns the list of all ARDQ registration IDs as an HTTP response
     */
    @Override
    public ResponseEntity<List<String>> retrieveAllArdqRegistrationIds() {
        log.info("Received GET request on /v1/augmentation/registration/ardq-ids");
        return ResponseEntity.of(handler.getAllArdqRegistrationIds());
    }

    /**
     * The controller method for a DELETE request on /v1/augmentation/registration/ardq/{ardq_id}
     *
     * @param ardqId ID of a previously registered ARDQ rule set (required)
     * @return Returns an HTTP Status
     */
    @Override
    public ResponseEntity<Void> deleteArdqRegistration(
            @PathVariable("ardq_id")
            final String ardqId) {
        log.info("Received DELETE request on /v1/augmentation/registration/ardq/{} ", ardqId);
        this.handler.deleteArdqRegistration(ardqId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * The controller method for a POST request on /v1/augmentation/registration/ardq
     *
     * @param ardqRegistrationDto Ardq Registration to register with AAS
     * @return Returns an HTTP Status
     */
    @Override
    public ResponseEntity<Void> createArdqRegistration(
            @RequestBody
            final ArdqRegistrationDto ardqRegistrationDto) {
        log.info("Received POST request on /v1/augmentation/registration/ardq with body: {}", ardqRegistrationDto.toJsonString());
        this.handler.createArdqRegistration(ardqRegistrationDto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * The controller method for a PUT request on /v1/augmentation/registration/ardq
     *
     * @param registrationDto Ardq Registration to update with AAS
     * @return Returns an HTTP Status
     */
    @Override
    public ResponseEntity<Void> updateArdqRegistration(
            @RequestBody
            final ArdqRegistrationDto registrationDto) {
        log.info("Received PUT request on /v1/augmentation/registration/ardq with body: {}", registrationDto.toJsonString());
        this.handler.updateArdqRegistration(registrationDto);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

}

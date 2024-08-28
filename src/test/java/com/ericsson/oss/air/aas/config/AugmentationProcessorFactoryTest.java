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
package com.ericsson.oss.air.aas.config;

import static com.ericsson.oss.air.aas.handler.RegistrationTestUtil.OUTPUT_SCHEMA;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import com.ericsson.oss.air.aas.service.AugmentationProcessingService;
import com.ericsson.oss.air.aas.service.ardq.ArdqService;
import io.micrometer.core.instrument.Counter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AugmentationProcessorFactoryTest {

    @Mock
    private AugmentationProcessingService augProcService;

    @Mock
    private ArdqService ardqService;

    @Mock
    private Counter augmentationErrorsCounter;

    @InjectMocks
    private AugmentationProcessorFactory factory;

    @Test
    void getAugmentationProcessor() {
        assertNotNull(this.factory.getAugmentationProcessor("url", "core", OUTPUT_SCHEMA, List.of(), 1));
    }
}
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

import java.util.List;

import com.ericsson.oss.air.aas.handler.AugmentationProcessor;
import com.ericsson.oss.air.aas.service.AugmentationProcessingService;
import com.ericsson.oss.air.aas.service.ardq.ArdqService;
import com.ericsson.oss.air.api.generated.model.ArdqAugmentationFieldDto;
import io.micrometer.core.instrument.Counter;
import org.apache.avro.Schema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AugmentationProcessorFactory {

    @Autowired
    private AugmentationProcessingService augmentationProcessingService;

    @Autowired
    private ArdqService ardqService;

    @Autowired
    private Counter augmentationErrorsCounter;

    /**
     * Create and return a new {@link AugmentationProcessor} object
     *
     * @param ardqUrl       ARDQ URL
     * @param ardqType      ARDQ type
     * @param outputSchema  output schema
     * @param fields        a list of {@link ArdqAugmentationFieldDto}
     * @param schemaVersion output schema version
     * @return a {@link AugmentationProcessor} object
     */
    public AugmentationProcessor getAugmentationProcessor(final String ardqUrl,
                                                          final String ardqType,
                                                          final Schema outputSchema,
                                                          final List<ArdqAugmentationFieldDto> fields,
                                                          final Integer schemaVersion) {
        return AugmentationProcessor
                .builder()
                .ardqUrl(ardqUrl)
                .ardqType(ardqType)
                .outputSchema(outputSchema)
                .fields(fields)
                .schemaVersion(schemaVersion)
                .augProcService(this.augmentationProcessingService)
                .ardqService(this.ardqService)
                .augmentationErrorsCounter(this.augmentationErrorsCounter)
                .build();
    }

}
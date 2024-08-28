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
package com.ericsson.oss.air.aas.model.impl.inmemorydb;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.NotEmpty;

import com.ericsson.oss.air.api.generated.model.ArdqAugmentationFieldDto;
import lombok.*;
import org.apache.avro.Schema;

/**
 * Represents the details of the output schema.
 */
@Data
@AllArgsConstructor
@Builder(setterPrefix = "with")
public class OutputSchemaDetails {

    /**
     * ARDQ Registration ID
     */
    @NonNull private String ardqRegistrationId;

    /**
     * Reference to the input schema
     */
    @NonNull private String inputSchemaRef;

    /**
     * Output schema
     */
    @NonNull private Schema outputSchema;

    /**
     * Reference to the output schema
     */
    @NonNull private String outputSchemaRef;

    /**
     * Name of the output schema
     */
    @NonNull private String outputSchemaName;

    /**
     * List of augmentation field specifications
     */
    @NotEmpty private List<ArdqAugmentationFieldDto> augmentationFieldSpecs = new ArrayList<>();

}

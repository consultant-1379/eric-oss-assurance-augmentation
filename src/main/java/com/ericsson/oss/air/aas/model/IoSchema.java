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

package com.ericsson.oss.air.aas.model;

import lombok.Data;
import lombok.NonNull;

/**
 * The type Io schema.
 */
@Data
public class IoSchema {

    /**
     * a id of ardq registration
     */
    @NonNull
    private String ardqRegistrationId;

    /**
     * Input schema reference in string format
     */
    @NonNull
    private String inputSchemaReference;

    /**
     * Output schema reference in string format
     */
    @NonNull
    private String outputSchemaReference;


}

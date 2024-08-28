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

package com.ericsson.oss.air.aas.model.datacatalog.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.*;
import lombok.extern.jackson.Jacksonized;

/**
 * The predicate parameters (param-value pair) supported by a Data Service. Predicates is a part of Input Data Specification (IDS) and it
 * specifies the logical expression which is applied for filtering a consumed message or files. rApp request can be
 * simple, requesting all data on the data location, in that case no predicates are defined in IDS.
 */
@Data
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({ "id", "parameterName", "dataServiceId", "isPassedToConsumedService", "dataService"})
public class SupportedPredicateParameterResponseDto {

    private Integer id;
    private String parameterName;
    private Integer dataServiceId;
    private Boolean isPassedToConsumedService;
    private DataServiceResponseDto dataService;

}

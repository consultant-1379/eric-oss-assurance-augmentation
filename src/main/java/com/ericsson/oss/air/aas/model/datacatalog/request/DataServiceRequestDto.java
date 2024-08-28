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

package com.ericsson.oss.air.aas.model.datacatalog.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.*;
import lombok.extern.jackson.Jacksonized;

/**
 * A platform service that produces data from a data provider to one or more data locations (kafka stream or file).
 */
@Data
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({ "id", "dataServiceName" })
public class DataServiceRequestDto {

  private Integer id;
  private String dataServiceName;

}


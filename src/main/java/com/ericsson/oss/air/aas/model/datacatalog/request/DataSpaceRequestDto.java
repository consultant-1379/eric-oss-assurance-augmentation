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

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.*;
import lombok.extern.jackson.Jacksonized;

/**
 * Represents a domain for the data. Data Space is defined by Data Service during registration of data.
 * For example, 4G, 3G, and so on.
 */
@Data
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({ "dataProviderTypeIds", "id", "name"})
public class DataSpaceRequestDto {

  @Singular private List<Integer> dataProviderTypeIds;
  private Integer id;
  private String name;

}

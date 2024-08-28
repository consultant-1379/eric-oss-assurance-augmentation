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
 * Represents a specific type of data source. For example, Ericsson Network Manager (ENM) or calculated data source.
 */
@Data
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({ "dataSpaceId", "dataCategoryId", "fileFormatIds", "id", "messageSchemaIds", "providerTypeId", "providerVersion" })
public class DataProviderTypeRequestDto {

  private Integer dataSpaceId;
  private Integer dataCategoryId;
  @Singular private List<Integer> fileFormatIds;
  private Integer id;
  @Singular private List<Integer> messageSchemaIds;
  private String providerTypeId;
  private String providerVersion;

}


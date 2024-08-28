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
 * A specific type of data on a data location (kafka stream or file) produced by a data service which has a specific
 * schema or format.
 */
@Data
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({"mediumType", "schemaName", "schemaVersion", "isExternal", "consumedDataSpace",
        "consumedDataCategory", "consumedDataProvider", "consumedSchemaName", "consumedSchemaVersion"})
public class DataTypeRequestDto {

  private String mediumType;
  private String schemaName;
  private String schemaVersion;
  private Boolean isExternal;
  private String consumedDataSpace;
  private String consumedDataCategory;
  private String consumedDataProvider;
  private String consumedSchemaName;
  private String consumedSchemaVersion;

}


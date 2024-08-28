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
 * A Data Service that can be instantiated more, deployed several times with different service alias, possible with
 * different configurations. Each instance produces the same data types. For example, Data Service:
 * PM-Counter-SFTP-file-Transfer can have data service instance as PM-Counter-SFTP-file-Transfer-ENM1 and
 * PM-Counter-SFTP-file-Transfer-ENM2.
 */
@Data
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({ "id", "dataServiceId", "dataServiceInstanceName", "controlEndpoint", "consumedDataSpace", "consumedDataCategory",
 "consumedDataProvider", "consumedSchemaName", "consumedSchemaVersion"})
public class DataServiceInstanceResponseDto {

  private Integer id;
  private Integer dataServiceId;
  private String dataServiceInstanceName;
  private String controlEndpoint;
  private String consumedDataSpace;
  private String consumedDataCategory;
  private String consumedDataProvider;
  private String consumedSchemaName;
  private String consumedSchemaVersion;

}


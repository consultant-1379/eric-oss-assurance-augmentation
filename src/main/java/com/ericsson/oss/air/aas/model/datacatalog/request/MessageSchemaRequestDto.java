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
 * A schema to describe the structure for each message. Having a schema allows for independence and understanding
 * between the data producers and data consumers. Data Producers provides data which is compliant to the schema
 * and Data Consumers understands how to read the data. A message Schema consists of a specific Data Provider Type,
 * Data Space, Data Service, Data Category, and Topic, subscription and Input Data Specification (IDS) for data type as stream.
 */
@Data
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({ "id", "dataSpace", "dataCategory", "dataProviderType", "messageStatusTopic", "messageDataTopic",
"messageSchema", "dataService", "dataServiceInstance", "supportedPredicateParameter", "dataType"})
public class MessageSchemaRequestDto {

  private Integer id;
  private DataSpaceRequestDto dataSpace;
  private DataCategoryRequestDto dataCategory;
  private DataProviderTypeRequestDto dataProviderType;
  private MessageStatusTopicRequestDto messageStatusTopic;
  private MessageDataTopicRequestDto messageDataTopic;
  private InnerMessageSchemaRequestDto messageSchema;
  private DataServiceRequestDto dataService;
  private DataServiceInstanceRequestDto dataServiceInstance;
  private SupportedPredicateParameterRequestDto supportedPredicateParameter;
  private DataTypeRequestDto dataType;

}


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

import com.ericsson.oss.air.aas.model.datacatalog.EncodingEnum;
import com.fasterxml.jackson.annotation.*;
import lombok.*;
import lombok.extern.jackson.Jacksonized;

/**
 * A topic on a specific Message Bus that holds a set of message data topics.
 */
@Data
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({ "encoding", "id", "messageBusId", "name", "specificationReference"})
public class MessageStatusTopicRequestDto {

  private EncodingEnum encoding;
  private Integer id;
  private Integer messageBusId;
  private String name;
  private String specificationReference;

}


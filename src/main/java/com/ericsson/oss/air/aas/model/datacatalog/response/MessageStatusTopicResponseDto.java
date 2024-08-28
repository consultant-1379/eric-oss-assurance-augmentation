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

import java.util.List;

import com.ericsson.oss.air.aas.model.datacatalog.EncodingEnum;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.*;
import lombok.extern.jackson.Jacksonized;

/**
 * A topic on a specific Message Bus that holds a set of message data topics.
 */
@Data
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({ "encoding", "id",  "messageBus", "messageDataTopicIds", "name", "specificationReference"})
public class MessageStatusTopicResponseDto {

    private EncodingEnum encoding;
    private Integer id;
    private MessageBusResponseDto messageBus;
    @Singular private List<Integer> messageDataTopicIds ;
    private String name;
    private String specificationReference;

}

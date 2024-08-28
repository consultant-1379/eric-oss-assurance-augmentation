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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.*;
import lombok.extern.jackson.Jacksonized;

/**
 * A message bus consists of a logical name for a cluster, a namespace, and an endpoint type. For example, Kafka, REST, gRPC. Message bus endpoint is used for notifications.
 */
@Data
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({ "accessEndpoints", "clusterName", "id", "messageDataTopicIds", "messageStatusTopicIds", "name", "nameSpace", "notificationTopicIds"})
public class MessageBusResponseDto {

    @Singular private List<String> accessEndpoints;
    private String clusterName;
    private Integer id;
    @Singular private List<Integer> messageDataTopicIds;
    @Singular private List<Integer> messageStatusTopicIds;
    private String name;
    private String nameSpace;
    @Singular private List<Integer> notificationTopicIds;

}

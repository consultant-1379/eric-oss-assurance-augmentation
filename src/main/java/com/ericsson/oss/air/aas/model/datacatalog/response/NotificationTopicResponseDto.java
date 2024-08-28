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
 * A topic used for file notifications.
 */
@Data
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({ "encoding", "fileFormatIds", "id", "messageBus", "dataProviderType", "name", "specificationReference"})
public class NotificationTopicResponseDto {

    private EncodingEnum encoding;
    @Singular private List<Integer> fileFormatIds;
    private Integer id;
    private MessageBusResponseDto messageBus;
    private DataProviderTypeResponseDto dataProviderType;
    private String name;
    private String specificationReference;

}

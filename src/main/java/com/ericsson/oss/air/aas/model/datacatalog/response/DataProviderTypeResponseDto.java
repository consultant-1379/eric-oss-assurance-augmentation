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
 * Represents a specific type of data source. For example, Ericsson Network Manager (ENM) or calculated data source.
 */
@Data
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({ "dataSpace", "dataCategoryType", "id", "providerTypeId", "providerVersion", "notificationTopicIds", "messageDataTopicIds"})
public class DataProviderTypeResponseDto{

    private DataSpaceResponseDto dataSpace;
    private DataCategoryResponseDto dataCategoryType;
    private Integer id;
    private String providerTypeId;
    private String providerVersion;
    @Singular private List<Integer> notificationTopicIds;
    @Singular private List<Integer> messageDataTopicIds;

}

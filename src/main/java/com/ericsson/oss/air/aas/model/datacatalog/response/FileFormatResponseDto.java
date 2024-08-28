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
 * A file format consists of a specific Data Provider Type, Data Service, Topic, subscription, Input Data Specification (IDS) and, Data Space for
 * data type as file. For example, XML, JSON of a specific Data Service.
 */
@Data
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({ "id", "bulkDataRepository", "dataService", "notificationTopic", "dataType" })
public class FileFormatResponseDto {

    private Integer id;
    private BulkDataRepositoryResponseDto bulkDataRepository;
    private DataServiceResponseDto dataService;
    private NotificationTopicResponseDto notificationTopic;
    private DataTypeResponseDto dataType;

}

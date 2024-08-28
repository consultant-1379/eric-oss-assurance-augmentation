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
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.*;
import lombok.extern.jackson.Jacksonized;

/**
 * An endpoint for landing and fetching the files. A Bulk Data Repository (BDR) endpoint must comprise a logical name
 * for a cluster, a namespace, and an endpoint type. For example, Kafka, REST, gRPC.
 */
@Data
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({ "accessEndpoints", "clusterName", "fileFormatIds", "id", "name", "nameSpace", "fileRepoType"})
public class BulkDataRepositoryResponseDto {

    @Singular private Set<String> accessEndpoints;
    private String clusterName;
    @Singular private List<Integer> fileFormatIds;
    private Integer id;
    private String name;
    private String nameSpace;
    private FileRepoResponseType fileRepoType;


}

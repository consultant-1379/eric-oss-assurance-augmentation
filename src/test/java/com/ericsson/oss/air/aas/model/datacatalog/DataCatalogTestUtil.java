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

package com.ericsson.oss.air.aas.model.datacatalog;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DataCatalogTestUtil {

    public static final ObjectMapper mapper = new ObjectMapper();

    public static final String DATA_SPACE_4G = "4G";
    public static final String DATA_CATEGORY_NAME = "CM_EXPORTS1";
    public static final String PROVIDER_VERSION = "Vv101";
    public static final String PROVIDER_TYPE_ID = "vv101";
    public static final String TOPIC_NAME = "topic102";
    public static final String SPECIFICATION_REFERENCE = "SpecRef101";
    public static final String DATA_SERVICE_NAME = "dataservicename102";
    public static final String DATA_SERVICE_INSTANCE_NAME = "dsinst101";
    public static final String CONTROL_ENDPOINT = "http://localhost:8082";
    public static final String SCHEMA_NAME = "SCH2";
    public static final String SCHEMA_VERSION = "2";
    public static final String MEDIUM_TYPE = "stream";
    public static final String PARAMETER_NAME = "pd101";
    public static final String MESSAGE_BUS_NAME = "mb";
    public static final String CLUSTER_NAME = "c1";
    public static final String NAMESPACE_NAME = "2g";
    public static final int BUS_ID = 1;

}

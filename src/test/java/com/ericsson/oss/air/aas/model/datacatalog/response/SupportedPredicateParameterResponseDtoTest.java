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

import static com.ericsson.oss.air.aas.model.datacatalog.DataCatalogTestUtil.mapper;
import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;

class SupportedPredicateParameterResponseDtoTest {

    @Test
    void build_IgnoreUnknownProperties() throws JsonProcessingException {
        assertNotNull(mapper.readValue("{\"id\":1,\"foobar\":\"something\"}", SupportedPredicateParameterResponseDto.class));
    }

}
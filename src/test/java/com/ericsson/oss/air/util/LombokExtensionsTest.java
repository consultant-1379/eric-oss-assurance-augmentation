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

package com.ericsson.oss.air.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashMap;

import org.junit.jupiter.api.Test;

class LombokExtensionsTest {

    @Test
    void toJsonString() {

        final HashMap<String, String> map = new HashMap<>();
        map.put("key", "value");

        assertEquals("{\"key\":\"value\"}", LombokExtensions.toJsonString(map));

    }

    @Test
    void toJsonString_fail_sneakyThrow() {

        assertThrows(Throwable.class, () -> LombokExtensions.toJsonString(new Object()));

    }
}
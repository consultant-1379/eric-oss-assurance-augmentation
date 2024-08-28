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

import static org.junit.jupiter.api.Assertions.*;

import com.ericsson.oss.air.aas.model.datacatalog.EncodingEnum;
import org.junit.jupiter.api.Test;

class EncodingEnumTest {

    @Test
    void fromValue_Valid() {
        final String stringValue = "avro";
        final EncodingEnum encodingEnum = EncodingEnum.fromValue(stringValue);

        assertEquals(EncodingEnum.AVRO, encodingEnum);
        assertEquals(stringValue.toUpperCase(), encodingEnum.toString());
    }

    @Test
    void fromValue_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> EncodingEnum.fromValue("foobar"));
    }
}
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class FileRepoResponseTypeTest {

    @Test
    void fromValue_Valid() {
        final String stringValue = "s3";
        final FileRepoResponseType FileRepoResponseType = com.ericsson.oss.air.aas.model.datacatalog.response.FileRepoResponseType.fromValue(stringValue);

        assertEquals(FileRepoResponseType, FileRepoResponseType);
        assertEquals(stringValue.toUpperCase(), FileRepoResponseType.toString());
    }

    @Test
    void fromValue_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> FileRepoResponseType.fromValue("foobar"));
    }
}
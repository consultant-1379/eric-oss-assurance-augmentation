/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class DigestTest {

    @Test
    void getDigestAsHex() {
        final String[] input = { "CoreServiceOwnerSPSubnet_smf_session_management_n1_snssai_apn_1" };

        final String expected = new Digest().getDigestAsHex(input);
        final String digest = new Digest().getDigestAsHex(input);

        assertEquals(40, digest.length());

        // ensure that the digest is reproducible. It is a hashing algorithm so it must be reproducible.
        assertEquals(expected, digest);

        assertEquals(digest, digest.toLowerCase());
    }

    @Test
    void getDigestAsHex_MD5() {

        final String digest = Digest.withAlgorithm(Digest.Algorithm.MD5)
                .getDigestAsHex("CoreServiceOwnerSPSubnet_smf_session_management_n1_snssai_apn_1");

        assertEquals(32, digest.length());

        assertEquals(digest, digest.toLowerCase());
    }

    @Test
    void getDigestAsHex_SHA256() {

        final String digest = Digest.withAlgorithm(Digest.Algorithm.SHA256)
                .getDigestAsHex("CoreServiceOwnerSPSubnet_smf_session_management_n1_snssai_apn_1");

        assertEquals(64, digest.length());

        assertEquals(digest, digest.toLowerCase());
    }
}

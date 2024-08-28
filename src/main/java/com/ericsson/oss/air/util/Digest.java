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

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

import lombok.SneakyThrows;

/**
 * Utility class for generating message digests as fixed-length hex strings from strings and string arrays.  By default, the digest is created using
 * the SHA-1 algorithm, which will result in a 40-character hex string.
 */
public class Digest {

    /**
     * Supported algorithm types for this {@code Digest} type.
     */
    public enum Algorithm {
        MD5,
        SHA1,
        SHA256
    }

    private static final Algorithm DEFAULT_ALGO = Algorithm.SHA1;

    /*
     * (non-javadoc)
     *
     * Inner class that provides the digest implementation.
     */
    private static class DigestImpl {

        private final Algorithm algorithm;

        DigestImpl(final Algorithm algorithm) {
            this.algorithm = algorithm;
        }

        /*
         * (non-javadoc)
         *
         * Returns a byte [] containing the digest generated from the provided input strings.
         */
        @SneakyThrows
        byte[] getDigest(final String... inputStrings) {

            final MessageDigest digest = MessageDigest.getInstance(this.algorithm.name());

            final String rawInput = String.join("", inputStrings);
            digest.update(rawInput.getBytes(StandardCharsets.UTF_8));
            return digest.digest();
        }

        /*
         * (non-javadoc)
         *
         * Formats the digest returned from getDigest() as a hex string.  This uses the default format of HexFormat, which
         * returns a hexadecimal string containing no delimiters and all lowercase characters.
         */
        String getDigestAsHex(final String... inputStrings) {
            return HexFormat.of().formatHex(this.getDigest(inputStrings));
        }
    }

    private final Algorithm algorithm;

    /**
     * Constructs a {@code Digest} instance with the default SHA-1 algorithm.
     */
    public Digest() {
        this(DEFAULT_ALGO);
    }

    /*
     * (non-javadoc)
     *
     * Constructs a Digest instance with the specified algorithm.
     */
    private Digest(final Algorithm algorithm) {
        this.algorithm = algorithm;
    }

    /**
     * Returns a digest of the provided input strings as a fixed length hexadecimal string.
     *
     * @param inputStrings digest input
     * @return fixed length hexadecimal string representation of the generated digest
     */
    public String getDigestAsHex(final String... inputStrings) {
        return new DigestImpl(this.algorithm).getDigestAsHex(inputStrings);
    }

    /**
     * Returns a {@code Digest} instance with the specified algorithm.
     *
     * @param algorithm algorithm to use with the {@code  Digest} instance
     * @return a {@code Digest} instance with the specified algorithm
     */
    public static Digest withAlgorithm(final Algorithm algorithm) {
        return new Digest(algorithm);
    }
}

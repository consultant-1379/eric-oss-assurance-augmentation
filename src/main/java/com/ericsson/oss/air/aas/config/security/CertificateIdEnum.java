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

package com.ericsson.oss.air.aas.config.security;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Enumeration of the certificate IDs that are used by the Certificate Reloader library to identify the files to
 * monitor as a part of a configurable file pattern location.
 */
@Getter
@RequiredArgsConstructor
public enum CertificateIdEnum {

    ROOTCA("rootca", "Root Certificate Authority"),
    PMCA("pmca", "PM Server Certificate Authority"),
    SERVER("server", "Embedded Server"),
    LOG("log", "Log Transformer"),
    KAFKA("kafka", "Kafka");

    private final String id;

    private final String displayName;

    @Override
    public String toString() {
        return this.id;
    }
}


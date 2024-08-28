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

package com.ericsson.oss.air.producer;

import lombok.Value;

/**
 * Loads TLS configuration properties from the file
 */
@Value
public class TlsProperties {

    Boolean enabled;
    String trustStoreLocation;
    String trustStorePassword;
    String trustStoreType;
    String keyStoreLocation;
    String keyStorePassword;
    String keyStoreType;

}

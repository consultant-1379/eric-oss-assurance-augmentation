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

/**
 * Interface to be implemented by reloaders that intend to reload security configuration for a given service/client.
 */
public interface SecurityConfigurationReloader {

    /**
     * Initializes the reloader.
     */
    void init();

    /**
     * Reloads the security configuration.
     */
    void reload();
}

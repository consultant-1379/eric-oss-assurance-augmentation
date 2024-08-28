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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

class SecurityConfigurationRegistryTest {

    @Test
    void register_NullReloader_ThrowsNullPointerException() {

        final SecurityConfigurationRegistry registry = new SecurityConfigurationRegistry();

        assertThrows(NullPointerException.class, () -> registry.register(null));
    }

    @Test
    void reloadConfiguration_Valid() {

        final SecurityConfigurationRegistry registry = new SecurityConfigurationRegistry();

        final AtomicInteger count = new AtomicInteger();

        final SecurityConfigurationReloader reloader = new SecurityConfigurationReloader() {
            @Override
            public void init() {
                return;
            }

            @Override
            public void reload() {
                count.incrementAndGet();
            }
        };

        registry.register(reloader);
        registry.reloadConfiguration();

        assertEquals(1, count.get());
    }
}
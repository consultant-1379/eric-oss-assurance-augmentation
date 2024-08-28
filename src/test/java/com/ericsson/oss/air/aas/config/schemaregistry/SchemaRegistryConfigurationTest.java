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

package com.ericsson.oss.air.aas.config.schemaregistry;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.security.NoSuchAlgorithmException;
import java.util.Optional;

import javax.net.ssl.SSLContext;

import com.ericsson.oss.air.aas.config.security.SecurityConfigurationRegistry;
import com.ericsson.oss.air.aas.config.security.SslContextConfigurator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SchemaRegistryConfigurationTest {

    private static final String TEST_BASE_URL = "http://localhost:8081";

    @Mock
    private SslContextConfigurator sslContextConfigurator;

    @Mock
    private SecurityConfigurationRegistry configurationRegistry;

    private SchemaRegistryConfiguration unsecuredSchemaRegistryConfiguration;

    private SchemaRegistryConfiguration securedSchemaRegistryConfiguration;

    @BeforeEach
    void setUp(){
        this.unsecuredSchemaRegistryConfiguration = new SchemaRegistryConfiguration(Optional.empty(), Optional.empty());
        this.unsecuredSchemaRegistryConfiguration.setSchemaRegistryUrl(TEST_BASE_URL);

        this.securedSchemaRegistryConfiguration = new SchemaRegistryConfiguration( Optional.of(this.sslContextConfigurator), Optional.of(this.configurationRegistry));
        this.securedSchemaRegistryConfiguration.setSchemaRegistryUrl(TEST_BASE_URL);
    }


    @Test
    void init_Unsecured(){

        this.unsecuredSchemaRegistryConfiguration.init();

        verify(this.configurationRegistry, times(0)).register(this.unsecuredSchemaRegistryConfiguration);
        assertNotNull(this.unsecuredSchemaRegistryConfiguration.getSchemaRegistryClient());
    }

    @Test
    void init_Secured() throws NoSuchAlgorithmException {

        when(this.sslContextConfigurator.getSslContext()).thenReturn(Optional.of(SSLContext.getDefault()));

        this.securedSchemaRegistryConfiguration.init();

        verify(this.configurationRegistry, times(1)).register(this.securedSchemaRegistryConfiguration);
        assertNotNull(this.securedSchemaRegistryConfiguration.getSchemaRegistryClient());

    }

    @Test
    void init_Secured_EmptySslContext()  {

        when(this.sslContextConfigurator.getSslContext()).thenReturn(Optional.empty());

        this.securedSchemaRegistryConfiguration.init();

        verify(this.configurationRegistry, times(1)).register(this.securedSchemaRegistryConfiguration);
        assertNotNull(this.securedSchemaRegistryConfiguration.getSchemaRegistryClient());

    }

    @Test
    void reload_Unsecured(){
        this.unsecuredSchemaRegistryConfiguration.reload();
        assertNull(this.unsecuredSchemaRegistryConfiguration.getSchemaRegistryClient());

    }

    @Test
    void reload_Secured() throws NoSuchAlgorithmException {

        when(this.sslContextConfigurator.getSslContext()).thenReturn(Optional.of(SSLContext.getDefault()));

        this.securedSchemaRegistryConfiguration.reload();

        assertNotNull(this.securedSchemaRegistryConfiguration.getSchemaRegistryClient());
    }


}

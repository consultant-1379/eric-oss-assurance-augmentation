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

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;

import lombok.NonNull;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

/**
 * This class is used to register and to trigger the reloading of the registered {@link SecurityConfigurationReloader}'s.
 */
@Component
@ConditionalOnBean(TlsConfiguration.class)
public class SecurityConfigurationRegistry {

    private final Collection<SecurityConfigurationReloader> reloaderList = new ConcurrentLinkedQueue<>();

    /**
     * Registers a {@link SecurityConfigurationReloader} into this registry.
     *
     * @param reloader an instance of {@link SecurityConfigurationReloader}
     */
    public void register(@NonNull final SecurityConfigurationReloader reloader){
        this.reloaderList.add(reloader);
    }

    /**
     * Triggers the reloading of all the registered {@link SecurityConfigurationReloader}'s.
     */
    public void reloadConfiguration(){
        this.reloaderList.forEach(SecurityConfigurationReloader::reload);
    }


}

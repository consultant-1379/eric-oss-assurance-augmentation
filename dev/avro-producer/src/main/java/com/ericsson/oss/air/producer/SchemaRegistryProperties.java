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
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.util.ObjectUtils;

/**
 * Loads schema registry configuration properties from the file
 */
@ConfigurationProperties(prefix = "dmm.schema-registry")
@ConstructorBinding
@Value
public class SchemaRegistryProperties {

    String url;
    TlsProperties tls;

    public TlsProperties getTls() {
        if (ObjectUtils.isEmpty(this.tls)) {
            return new TlsProperties(false, null, null, null, null, null, null);
        }

        return this.tls;
    }
}

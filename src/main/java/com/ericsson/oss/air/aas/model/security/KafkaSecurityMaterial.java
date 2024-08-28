/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.aas.model.security;

import com.ericsson.adp.security.certm.certificatewatcher.KeyStoreItem;
import com.ericsson.adp.security.certm.certificatewatcher.TrustStoreItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents the required security items needed to enable mTLS Kafka communication
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class KafkaSecurityMaterial {

    private KeyStoreItem kafkaKeyStoreItem;
    private KeyStoreItem serverKeyStoreItem;
    private TrustStoreItem trustStoreItem;
}

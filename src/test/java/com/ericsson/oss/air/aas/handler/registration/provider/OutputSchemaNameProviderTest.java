/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.aas.handler.registration.provider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class OutputSchemaNameProviderTest {

    private final OutputSchemaNameProvider outputSchemaNameProvider = new OutputSchemaNameProvider();

    @Test
    void generate_defaultLength_pmsch() {
        this.outputSchemaNameProvider.setAnalyticsTarget("pmsch");

        final String ardqId = "CoreSliceOwner";
        final String inputSchemaName = "up_payload_dnn_slice_1";
        final String expected = "CoreSliceOwner_up_payload_dnn_slice_1";

        final String actual = this.outputSchemaNameProvider.generate(ardqId, inputSchemaName);

        assertFalse(actual.startsWith("aas_"));
        assertEquals(expected, actual);
    }

    @Test
    void generate_longSourceLength_pmsch() {
        this.outputSchemaNameProvider.setAnalyticsTarget("pmsch");

        final String ardqId = "CoreServiceOwnerSPSubnet";
        final String inputSchemaName = "smf_session_management_n1_snssai_apn_1";
        final String expected = "aas_9c1d1dbd69fdba177514533b4e755147cc67141f";  // as a hash, it is predictable

        final String actual = this.outputSchemaNameProvider.generate(ardqId, inputSchemaName);

        assertTrue(actual.startsWith("aas_"));
        assertEquals(expected, actual);
    }

    @Test
    void generate_defaultLength_otherAnalyticsTarget() {
        this.outputSchemaNameProvider.setAnalyticsTarget("VM");

        final String ardqId = "CoreSliceOwner";
        final String inputSchemaName = "up_payload_dnn_slice_1";
        final String expected = "CoreSliceOwner_up_payload_dnn_slice_1";

        final String actual = this.outputSchemaNameProvider.generate(ardqId, inputSchemaName);

        assertFalse(actual.startsWith("aas_"));
        assertEquals(expected, actual);
    }

    @Test
    void generate_longSourceLength_otherAnalyticsTarget() {
        this.outputSchemaNameProvider.setAnalyticsTarget("VM");

        final String ardqId = "CoreServiceOwnerSPSubnet";
        final String inputSchemaName = "smf_session_management_n1_snssai_apn_1";
        final String expected = "CoreServiceOwnerSPSubnet_smf_session_management_n1_snssai_apn_1";

        final String actual = this.outputSchemaNameProvider.generate(ardqId, inputSchemaName);

        assertFalse(actual.startsWith("aas_"));
        assertEquals(expected, actual);
    }
}

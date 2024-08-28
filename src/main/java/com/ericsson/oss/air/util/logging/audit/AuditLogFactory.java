/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.util.logging.audit;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Responsible for generating {@link AuditLogger}'s.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AuditLogFactory {

    /**
     * Creates a {@link AuditLogger} for the provided class.
     *
     * @param clazz the class that needs to emit audit log events
     * @return a {@link AuditLogger}
     */
    public static AuditLogger getLogger(final Class<?> clazz) {
        return new AuditLogger(clazz);
    }
}

/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

/**
 * This package contains the classes to log auditable events. To log an auditable event, the client class needs an audit logger.
 * For this example, the client class is CoreApplication.class:
 *
 *<pre>
 * private static final AuditLogger AUDIT_LOGGER = AuditLogFactory.getLogger(CoreApplication.class);
 *</pre>
 *
 * Audit loggers provide the standard logging methods. See {@link com.ericsson.oss.air.util.logging.audit.AuditLogger}.
 * <p/>
 * Example usage of an audit logger:
 *
 * <pre>
 * AUDIT_LOGGER.info("AAS starting");
 * </pre>
 *
 */
package com.ericsson.oss.air.util.logging.audit;
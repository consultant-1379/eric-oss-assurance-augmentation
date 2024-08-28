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

import java.util.Optional;

/**
 * Base type for all combinators that return a type {@code R} object wrapped in an {@code Optional} created from one or many
 * security artifacts.
 */
public interface SecurityArtifactCombinator<T, R> {

    /**
     * Combines the provided security artifacts of type {@code T} into an object of type {@code R}.
     *
     * @param artifacts the security material to be combined into the resultant {@code R} object
     * @return an {@code Optional} of {@code R}
     */
    Optional<R> combine(final T... artifacts);
}

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

package com.ericsson.oss.air.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;

/**
 * A Lombok extension class to provide utility methods which will be used by Lombok ExtensionMethod annotation. <br />
 * <br />
 * Usage example:
 * <pre>
 * {@code @ExtensionMethod({ LombokExtensions.class })
 * public class ExampleClass {
 * }
 * } </pre>
 */
public class LombokExtensions {

    private LombokExtensions() {
    }

    /**
     * A static method to serialize an Object to a json String. This method should only be used by an Object that can be serialized to JsonString <br />
     * <br />
     * Usage example:
     * <pre>
     * {@code @ExtensionMethod({ LombokExtensions.class })
     * public class ExampleClass {
     *     public ExampleClass() {
     *     }
     *     public String toJsonString(final Map<String, String> map) {
     *         return map.toJsonString();
     *     }
     * }
     * } </pre>
     *
     * @param <T>    the type parameter
     * @param object A object
     * @return input object in json string format
     */
    @SneakyThrows
    public static <T> String toJsonString(final T object) {

        return new ObjectMapper().writeValueAsString(object);

    }
}
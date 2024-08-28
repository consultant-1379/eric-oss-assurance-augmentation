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

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;

import com.ericsson.oss.air.exception.http.problem.exception.HttpBadRequestProblemException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;

/**
 * A validation utility class to provide URL related support.
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UrlUtil {

    /**
     * This method validate input url string.
     * <p>
     * A valid URL string can be used to create an URL object and the server need to be resolvable by DNS.
     *
     * @param urlStr URL string to be validated
     */
    public static void validateUrl(final String urlStr) {

        if (ObjectUtils.isEmpty(urlStr)) {
            final String errorMsg = "Validation failed: Url cannot be empty! ";
            log.error(errorMsg);
            throw HttpBadRequestProblemException.builder().description(errorMsg).build();
        }

        try {
            // validate URL from string
            final URL url = new URL(urlStr);

            // validate if URL is available by DNS lookup
            InetAddress.getByName(url.getHost());

            log.debug("Url: [{}] is validated successfully ", url);
        } catch (final MalformedURLException | UnknownHostException e) {
            final String errorMsg = String.format("Validation failed: Url: [%s] is not available! ", urlStr);
            log.error(errorMsg + e.getMessage());
            log.debug(String.valueOf(e));
            throw HttpBadRequestProblemException.builder().description(errorMsg).build();
        }

    }

}

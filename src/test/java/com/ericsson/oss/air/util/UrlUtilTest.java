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

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.stream.Stream;

import com.ericsson.oss.air.exception.http.problem.exception.HttpBadRequestProblemException;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.springframework.http.HttpStatus;

class UrlUtilTest {

    private static final String VALID_EXAMPLE_URL = "http://localhost:9191/test";
    private WireMockServer wireMockServer;

    @BeforeEach
    public void startWireMockServer() {
        wireMockServer = new WireMockServer(9191);
        configureFor("localhost", 9191);
        wireMockServer.start();
    }

    @AfterEach
    public void stopWireMockServer() {
        wireMockServer.stop();
    }

    @Test
    void validateUrl_validLocalUrl_success() {
        stubFor(WireMock.get(urlEqualTo("/test"))
                .willReturn(aResponse().withStatus(HttpStatus.OK.value())));
        UrlUtil.validateUrl(VALID_EXAMPLE_URL);
    }

    @ParameterizedTest
    @MethodSource("provideUrls")
    @NullSource
    void validateUrl_invalidOrEmptyOrNullUrl_throwException(final String url) {
        assertThrows(HttpBadRequestProblemException.class,
                () -> UrlUtil.validateUrl(url));
    }

    private static Stream<Arguments> provideUrls() {
        return Stream.of(
                Arguments.of("127.0.0.1"),
                Arguments.of("")
        );
    }

}
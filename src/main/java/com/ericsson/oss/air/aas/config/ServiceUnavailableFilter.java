/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.aas.config;

import java.io.IOException;
import java.io.PrintWriter;

import com.ericsson.oss.air.aas.service.ConfigurationNotificationService;
import com.ericsson.oss.air.exception.http.problem.exception.HttpProblemException;
import com.ericsson.oss.air.exception.http.problem.exception.HttpServiceUnavailableException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Filters out incoming requests to AAS if AAS is unable to service incoming requests. If AAS is unable to service the
 * incoming request, then a 503 is returned.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ServiceUnavailableFilter extends OncePerRequestFilter {

    private static final String NOTIFICATIONS_CANNOT_BE_SENT = "AAS configuration notifications cannot be sent.";

    private final ConfigurationNotificationService notificationService;

    @Override
    protected void doFilterInternal(final HttpServletRequest servletRequest, final HttpServletResponse servletResponse, final FilterChain filterChain)
            throws IOException, ServletException {



        if (this.notificationService.isReady()) {

            log.info("HTTP request received: {}", servletRequest.getRequestURI());
            
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        log.warn("HTTP request received: {}. AAS not ready to accept requests:  Notification service not ready", servletRequest.getRequestURI());
        
        final HttpProblemException serviceUnavailable = HttpServiceUnavailableException.builder().description(NOTIFICATIONS_CANNOT_BE_SENT).build();
        servletResponse.setStatus(serviceUnavailable.getHttpStatus().value());
        servletResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);

        final ObjectMapper objectMapper = new ObjectMapper();
        final PrintWriter printWriter = servletResponse.getWriter();
        final String printedString = objectMapper.writeValueAsString(serviceUnavailable.getProblem());

        printWriter.print(printedString);
        printWriter.flush();

    }
}

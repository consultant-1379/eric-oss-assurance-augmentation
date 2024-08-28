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

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.PrintWriter;

import com.ericsson.oss.air.aas.service.ConfigurationNotificationService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

@ExtendWith(MockitoExtension.class)
class ServiceUnavailableFilterTest {

    @Mock
    private ConfigurationNotificationService notificationService;

    @InjectMocks
    private ServiceUnavailableFilter filter;

    @Mock
    private HttpServletRequest servletRequest;

    @Mock
    private HttpServletResponse servletResponse;

    @Mock
    private FilterChain filterChain;

    @Mock
    private PrintWriter printWriter;

    @Test
    void doFilter_NotificationServiceIsReady_PassThrough() throws ServletException, IOException {

        when(this.notificationService.isReady()).thenReturn(true);

        this.filter.doFilterInternal(this.servletRequest, this.servletResponse, this.filterChain);

        verify(this.filterChain, times(1)).doFilter(this.servletRequest, this.servletResponse);

    }

    @Test
    void doFilter_NotificationServiceIsNotReady_ServiceUnavailable() throws ServletException, IOException {

        when(this.notificationService.isReady()).thenReturn(false);
        when(this.servletResponse.getWriter()).thenReturn(this.printWriter);

        this.filter.doFilterInternal(this.servletRequest, this.servletResponse, this.filterChain);

        final String expectedBody = "{\"type\":\"about:blank\",\"title\":\"Service Unavailable\",\"status\":503,\"detail\":\"AAS configuration notifications cannot be sent.\",\"instance\":null}";

        verify(this.filterChain, never()).doFilter(this.servletRequest, this.servletResponse);
        verify(this.servletResponse, times(1)).setStatus(HttpStatus.SERVICE_UNAVAILABLE.value());
        verify(this.servletResponse, times(1)).setContentType(MediaType.APPLICATION_JSON_VALUE);
        verify(this.printWriter, times(1)).print(expectedBody);
        verify(this.printWriter, times(1)).flush();
    }
}
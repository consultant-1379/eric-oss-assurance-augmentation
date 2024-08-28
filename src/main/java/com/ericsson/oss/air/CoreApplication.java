/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air;

import com.ericsson.oss.air.util.logging.audit.AuditLogFactory;
import com.ericsson.oss.air.util.logging.audit.AuditLogger;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Core Application, the starting point of the application.
 */
@SpringBootApplication(exclude = { UserDetailsServiceAutoConfiguration.class })
public class CoreApplication {

    private static final AuditLogger AUDIT_LOGGER = AuditLogFactory.getLogger(CoreApplication.class);

    /**
     * Logs audit event for starting AAS.
     */
    @PostConstruct
    public void init() {
        AUDIT_LOGGER.info("AAS starting");
    }

    /**
     * Logs audit event for shutting down AAS.
     */
    @PreDestroy
    public void shutdown() {
        AUDIT_LOGGER.info("AAS shutting down");
    }

    /**
     * Main entry point of the application.
     *
     * @param args Command line arguments
     */
    public static void main(final String[] args) {
        SpringApplication.run(CoreApplication.class, args);
    }

    /**
     * Configuration bean for Web MVC.
     *
     * @return WebMvcConfigurer
     */
    @Bean
    public WebMvcConfigurer webConfigurer() {
        return new WebMvcConfigurer() {
        };
    }

    /**
     * Making a RestTemplate, using the RestTemplateBuilder, to use for consumption of RESTful interfaces.
     *
     * @param restTemplateBuilder RestTemplateBuilder instance
     * @return RestTemplate
     */
    @Bean
    public RestTemplate restTemplate(final RestTemplateBuilder restTemplateBuilder) {
        return restTemplateBuilder.build();
    }

    /**
     * Disabling automatic Flyway migration on startup.
     *
     * @return FlywayMigrationStrategy
     */
    @Bean
    public FlywayMigrationStrategy flywayMigrationStrategy() {
        return flyway -> {
        };
    }

}

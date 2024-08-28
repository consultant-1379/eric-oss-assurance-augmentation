/*******************************************************************************
 * COPYRIGHT Ericsson 2021
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

package com.ericsson.oss.air;

import com.ericsson.oss.air.producer.SchemaRegistryService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

/**
 * Core Application, the starting point of the application.
 */
@SpringBootApplication(exclude = HibernateJpaAutoConfiguration.class)
@Slf4j
public class CoreApplication implements CommandLineRunner {

    @Autowired
    AvroRecordParser parser;

    @Autowired
    SchemaRegistryService schemaRegistryService;

    /**
     * Main entry point of the application.
     *
     * @param args Command line arguments
     */
    public static void main(final String[] args) {
        SpringApplication.run(CoreApplication.class, args);
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

    @Override
    @SneakyThrows
    public void run(final String... args) {
        log.info("Start producing records");
        this.parser.produceAvroRecords();

    }
}

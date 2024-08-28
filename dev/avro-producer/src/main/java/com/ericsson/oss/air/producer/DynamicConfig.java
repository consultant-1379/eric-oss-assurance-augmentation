/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.producer;

import java.util.List;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ObjectUtils;

@Configuration
@Data
@Slf4j
public class DynamicConfig {

    private String schemaRegistryUrl;

    private String kafkaServer;

    private String topic;

    private String datafile;

    public DynamicConfig(final ApplicationArguments args) {

        log.info("Updating dynamic config by arguments");

        final List<String> schemaRegistry = args.getOptionValues("schemaRegistry");
        if (schemaRegistry != null) {
            if (schemaRegistry.size() != 1) {
                throw new RuntimeException("Error: option --schemaRegistry requires a schema registry url");
            }
            this.schemaRegistryUrl = schemaRegistry.get(0);
            log.info("Schema Registry URL: {}", this.schemaRegistryUrl);
        }

        final List<String> kafkaServerArgs = args.getOptionValues("kafka");
        if (kafkaServerArgs != null) {
            if (kafkaServerArgs.size() != 1) {
                throw new RuntimeException("Error: option --kafka requires a kafka server url");
            }
            this.kafkaServer = kafkaServerArgs.get(0);
            log.info("Kafka Server {}", this.kafkaServer);
        }
        final List<String> topicArgs = args.getOptionValues("topic");
        if (topicArgs != null) {
            if (topicArgs.size() != 1) {
                throw new RuntimeException("Error: option --topic requires a kafka topic");
            }
            this.topic = topicArgs.get(0);
            log.info("Target topic: {}", this.topic);
        }

        final List<String> fileArgs = args.getOptionValues("file");
        if (fileArgs != null) {
            if (fileArgs.size() != 1) {
                throw new RuntimeException("Error: option --file requires a data file");
            }
            this.datafile = fileArgs.get(0);
            log.info("Data Source file: {}", this.datafile);
        } else {
            throw new RuntimeException("--file is required to run AvroProducer");
        }

    }

    @Autowired
    public void setupSRUrl(@Value("${dmm.schemaRegistry.url}") final String schemaRegistryUrl) {
        if (ObjectUtils.isEmpty(this.schemaRegistryUrl)) {
            this.schemaRegistryUrl = schemaRegistryUrl;
        }
    }

    @Autowired
    public void setKafkaServer(final KafkaProperties kafkaProperties) {
        if (ObjectUtils.isEmpty(this.kafkaServer)) {
            this.kafkaServer = kafkaProperties.getBootstrapServers();
        }
        if (ObjectUtils.isEmpty(this.topic)) {
            this.topic = kafkaProperties.getTopic();
        }

    }
}

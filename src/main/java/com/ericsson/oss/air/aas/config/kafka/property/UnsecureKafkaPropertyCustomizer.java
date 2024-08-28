/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.aas.config.kafka.property;

import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

import com.ericsson.oss.air.aas.config.kafka.KafkaConfiguredEvent;
import com.ericsson.oss.air.aas.config.security.TlsConfiguration;

import lombok.RequiredArgsConstructor;

/**
 * Unsecure implementation of {@link KafkaPropertyCustomizer}
 */
@Configuration
@RequiredArgsConstructor
@ConditionalOnMissingBean(TlsConfiguration.class)
public class UnsecureKafkaPropertyCustomizer implements KafkaPropertyCustomizer {

    private final CommonKafkaCustomProperties commonKafkaCustomProperties;

    private final ApplicationEventPublisher eventPublisher;

    /**
     * Publishes a {@link KafkaConfiguredEvent} to indicate that AAS' Kafka configuration has been set up successfully.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        this.eventPublisher.publishEvent(new KafkaConfiguredEvent(this));
    }

    @Override
    public Map<String, Object> kafkaConsumerProperties() {
        return this.commonKafkaCustomProperties.getConsumerProperties();
    }

    @Override
    public Map<String, Object> kafkaProducerProperties() {
        return this.commonKafkaCustomProperties.getProducerProperties();
    }

    @Override
    public Map<String, Object> kafkaAdminProperties() {
        return this.commonKafkaCustomProperties.getAdminProperties();
    }

    @Override
    public Map<String, Object> apKafkaProducerProperties() {
        return this.commonKafkaCustomProperties.getApProducerProperties();
    }

    @Override
    public Map<String, Object> apKafkaConsumerProperties() {
        return this.commonKafkaCustomProperties.getApConsumerProperties();
    }
}

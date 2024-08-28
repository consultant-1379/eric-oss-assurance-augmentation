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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;

import com.ericsson.oss.air.aas.config.kafka.KafkaConfiguredEvent;

@SpringBootTest(classes = { CommonKafkaCustomProperties.class, UnsecureKafkaPropertyCustomizer.class })
@RecordApplicationEvents
@ActiveProfiles("test")
class UnsecureKafkaPropertyCustomizerTest {

    @Autowired
    private ApplicationEventPublisher publisher;

    @Autowired
    private ApplicationEvents applicationEvents;

    @Autowired
    private CommonKafkaCustomProperties commonKafkaCustomProperties;

    @Autowired
    private UnsecureKafkaPropertyCustomizer unsecureKafkaPropertyCustomizer;

    @Test
    void init() {

        this.unsecureKafkaPropertyCustomizer.init();

        final Optional<ApplicationEvent> actualEvent = this.applicationEvents.stream()
                .filter(event -> event instanceof KafkaConfiguredEvent)
                .findFirst();

        assertTrue(actualEvent.isPresent());
    }

    @Test
    void init_EventListenerAnnotation() throws NoSuchMethodException {

        final Class resourceClass = UnsecureKafkaPropertyCustomizer.class;
        final Method initMethod = resourceClass.getMethod("init");

        final EventListener eventAnnotation = initMethod.getAnnotation(EventListener.class);

        assertNotNull(eventAnnotation);
        assertEquals(ApplicationReadyEvent.class, eventAnnotation.value()[0]);
    }

    @Test
    void kafkaConsumerProperties() {

        final Map<String, Object> actualMap = this.unsecureKafkaPropertyCustomizer.kafkaConsumerProperties();

        assertEquals(this.commonKafkaCustomProperties.getConsumerProperties(), actualMap);
    }

    @Test
    void kafkaProducerProperties() {

        final Map<String, Object> actualMap = this.unsecureKafkaPropertyCustomizer.kafkaProducerProperties();

        assertEquals(this.commonKafkaCustomProperties.getProducerProperties(), actualMap);
    }

    @Test
    void kafkaAdminProperties() {

        final Map<String, Object> actualMap = this.unsecureKafkaPropertyCustomizer.kafkaAdminProperties();

        assertEquals(this.commonKafkaCustomProperties.getAdminProperties(), actualMap);
    }

    @Test
    void apKafkaProducerProperties() {

        final Map<String, Object> actualMap = this.unsecureKafkaPropertyCustomizer.apKafkaProducerProperties();

        assertEquals(this.commonKafkaCustomProperties.getApProducerProperties(), actualMap);
    }

    @Test
    void apKafkaConsumerProperties() {

        final Map<String, Object> actualMap = this.unsecureKafkaPropertyCustomizer.apKafkaConsumerProperties();

        assertEquals(this.commonKafkaCustomProperties.getApConsumerProperties(), actualMap);
    }
}
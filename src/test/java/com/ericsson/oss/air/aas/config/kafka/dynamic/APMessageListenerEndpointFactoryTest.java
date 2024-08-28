/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.aas.config.kafka.dynamic;

import static java.util.Collections.singletonList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import static com.ericsson.oss.air.aas.handler.RegistrationTestUtil.SCHEMA_SUBJECT_OBJ;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.config.KafkaListenerEndpoint;

import com.ericsson.oss.air.aas.config.kafka.property.KafkaPropertyCustomizer;
import com.ericsson.oss.air.aas.handler.AugmentationProcessor;

@ExtendWith(MockitoExtension.class)
class APMessageListenerEndpointFactoryTest {

    @Mock
    private AugmentationProcessor augmentationProcessor;

    @Mock
    private KafkaPropertyCustomizer kafkaPropertyCustomizer;

    @InjectMocks
    private APMessageListenerEndpointFactory factory;

    @Test
    void test_createKafkaListenerEndpoint() {

        final KafkaListenerEndpoint kafkaListenerEndpoint = this.factory.createKafkaListenerEndpoint("ConsumerId",
                                                                                                     "topic",
                                                                                                     SCHEMA_SUBJECT_OBJ,
                                                                                                     singletonList(this.augmentationProcessor));

        final APMessageListener messageListener = this.factory.getMessageListener("ConsumerId");

        assertEquals("ConsumerId", kafkaListenerEndpoint.getId());
        assertEquals(List.of("topic"), new ArrayList<>(kafkaListenerEndpoint.getTopics()));
        assertNotNull(messageListener);
    }

    @Test
    void getMessageListener_noMatchedMessageListener_null() {
        final APMessageListener messageListener = this.factory.getMessageListener("RandomID");
        assertNull(messageListener);
    }
}
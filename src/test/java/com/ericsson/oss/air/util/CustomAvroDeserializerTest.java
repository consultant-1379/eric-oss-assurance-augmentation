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

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import com.ericsson.oss.air.aas.generated.model.notification.ArdqNotificationType;
import com.ericsson.oss.air.aas.generated.model.notification.ArdqRegistrationNotification;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.common.errors.SerializationException;
import org.junit.jupiter.api.Test;

public class CustomAvroDeserializerTest {
    private CustomAvroDeserializer<GenericRecord> deserializer;

    @Test
    void test_deserialize() {
        ArdqRegistrationNotification notification = ArdqRegistrationNotification.newBuilder().setArdqId("testId")
                .setArdqNotificationType(ArdqNotificationType.CREATE)
                .setDeprecatedInputSchemas(List.of("someSchemaId")).build();
        CustomAvroSerializer<ArdqRegistrationNotification> serializer = new CustomAvroSerializer<>();
        byte[] msg = serializer.serialize("test", notification);

        CustomAvroDeserializer<ArdqRegistrationNotification> customAvroDeserializer = new CustomAvroDeserializer<>(
                ArdqRegistrationNotification.class);

        assertEquals(notification, customAvroDeserializer.deserialize("test", msg));
        customAvroDeserializer.close();
    }

    @Test
    void test_deserializeWithNull() {
        deserializer = new CustomAvroDeserializer<>(GenericRecord.class);
        assertNull(deserializer.deserialize("test", null));
    }

    @Test
    void test_deserializeWithException() {
        deserializer = new CustomAvroDeserializer<>(GenericRecord.class);
        final byte[] msg = "Hello Testing".getBytes();

        assertThrows(SerializationException.class, () -> {
            deserializer.deserialize("test", msg);
        });
    }
}

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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

import jakarta.xml.bind.DatatypeConverter;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.EncoderFactory;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;

/**
 * Custom avro serializer for notification kafka messages, which are avro schemas
 *
 * @param <T> generic type
 */
@Slf4j
public class CustomAvroSerializer<T extends GenericRecord> implements Serializer<T> {

    @Override
    public void configure(final Map<String, ?> configs, final boolean isKey) {
        // no-op
    }

    @Override
    public void close() {
        // no-op
    }

    @Override
    public byte[] serialize(final String topic, final T data) {
        // null needs to be treated specially since null in kafka has a special meaning for
        // deletion in a topic with the compact retention policy. Therefore, we will return
        // a null value in kafka, instead of an Avro encoded null
        if (data == null) {
            return null; //NOSONAR
        }
        try {
            log.debug("data='{}'", data);

            final byte[] result = streamData(data);
            log.debug("serialized data='{}'", DatatypeConverter.printHexBinary(result));
            return result;
        } catch (IOException ex) {
            throw new SerializationException("Can't serialize data='" + data + "' for topic='" + topic + "'", ex);
        }
    }

    private byte[] streamData(final T data) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        final BinaryEncoder binaryEncoder =
                EncoderFactory.get().binaryEncoder(byteArrayOutputStream, null);
        DatumWriter<GenericRecord> datumWriter = new GenericDatumWriter<>(data.getSchema());
        datumWriter.write(data, binaryEncoder);
        binaryEncoder.flush();
        byteArrayOutputStream.close();
        return byteArrayOutputStream.toByteArray();
    }

}

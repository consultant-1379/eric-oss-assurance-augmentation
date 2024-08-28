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

import java.util.Arrays;
import java.util.Map;

import jakarta.xml.bind.DatatypeConverter;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;

/**
 * Custom avro deserializer for the notification kafka messages, which are avro schemas
 *
 * @param <T> generic type
 */
@Slf4j
public class CustomAvroDeserializer<T extends GenericRecord> implements Deserializer<T> {

    protected final Class<T> targetType;

    /**
     * Creates an instance of custom avro deserializer with target type
     *
     * @param targetType the target type
     */
    public CustomAvroDeserializer(final Class<T> targetType) {
        this.targetType = targetType;
    }

    @Override
    public void configure(final Map configs, final boolean isKey) {
        // no-op
    }

    @Override
    public void close() {
        // no-op
    }

    @Override
    public T deserialize(final String topic, final byte[] data) {
        try {
            T result = null;

            if (data != null) {
                log.debug("data='{}'", DatatypeConverter.printHexBinary(data));

                DatumReader<GenericRecord> datumReader =
                        new SpecificDatumReader<>(targetType.getDeclaredConstructor().newInstance().getSchema());
                final Decoder decoder = DecoderFactory.get().binaryDecoder(data, null);

                result = (T) datumReader.read(null, decoder);
                log.debug("deserialized data='{}'", result);
            }
            return result;
        } catch (Exception ex) {
            throw new SerializationException(
                    "Can't deserialize data '" + Arrays.toString(data) + "' from topic '" + topic + "'", ex);
        }
    }

}

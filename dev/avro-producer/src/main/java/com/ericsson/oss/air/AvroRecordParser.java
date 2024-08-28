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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.ericsson.oss.air.producer.DynamicConfig;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.io.JsonEncoder;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

@Component
@NoArgsConstructor
@Slf4j
public class AvroRecordParser {

    private KafkaTemplate apKafkaTemplate;

    private DynamicConfig dynamicConfig;

    private SchemaDAO schemaDAO;

    @Autowired
    public AvroRecordParser(final SchemaDAO schemaDAO, final DynamicConfig dynamicConfig,
                            @Qualifier("kfTemplate") final KafkaTemplate apKafkaTemplate) {
        this.schemaDAO = schemaDAO;
        this.dynamicConfig = dynamicConfig;
        this.apKafkaTemplate = apKafkaTemplate;
    }

    public static GenericRecord createRecord(final Schema schema, final String jsonString) {
        try {
            final Decoder decoder = DecoderFactory.get().jsonDecoder(schema, jsonString);
            final DatumReader<GenericRecord> reader = new GenericDatumReader<>(schema);
            return reader.read(null, decoder);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Stream<ProducerRecord<String, GenericRecord>> getProducerRecordStream(final String recordFilePath) throws IOException {
        final Path path = Paths.get(recordFilePath);
        final Stream<String> lines = Files.lines(path);
        return lines.map(str -> {
            if (ObjectUtils.isEmpty(str)) {
                return null;
            }
            final JSONObject obj = new JSONObject(str);
            final String schemaSubject = obj.getString("subject");
            final GenericRecord genericRecord = createRecord(this.schemaDAO.getSchema(schemaSubject),
                    obj.getJSONObject("data").toString());

            final ProducerRecord<String, GenericRecord> pmRecord = new ProducerRecord<>(this.dynamicConfig.getTopic(), genericRecord);
            // Populate Headers
            pmRecord.headers().add("schemaSubject", schemaSubject.getBytes(StandardCharsets.UTF_8));
            pmRecord.headers().add("schemaID", this.schemaDAO.getSchemaId(schemaSubject).toString().getBytes(StandardCharsets.UTF_8));
            pmRecord.headers().add("schemaVersion", this.schemaDAO.getVersion(schemaSubject).toString().getBytes(StandardCharsets.UTF_8));

            return pmRecord;
        });
    }

    public String avroToJson(final Schema schema, final GenericRecord record) throws IOException {

        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        final DatumWriter<GenericRecord> writer = new GenericDatumWriter<>(schema);
        final JsonEncoder encoder = EncoderFactory.get().jsonEncoder(schema, outputStream, false);

        writer.write(record, encoder);
        encoder.flush();
        outputStream.close();

        return outputStream.toString();
    }

    @SneakyThrows
    public void produceAvroRecords() {
        final String recordFilePath = this.dynamicConfig.getDatafile();
        final Stream<ProducerRecord<String, GenericRecord>> producerRecordStream = this.getProducerRecordStream(recordFilePath);
        final AtomicReference<Integer> counter = new AtomicReference<>(0);

        log.info("Start producing Avro Records to topic {}", this.dynamicConfig.getTopic());

        producerRecordStream.forEach(producerRecord -> {
            if (ObjectUtils.isEmpty(producerRecord)) {
                return;
            }

            log.info("Sending Records: {} ", producerRecord.value());
            final String headerString = Arrays.stream(producerRecord.headers().toArray())
                    .map(header -> header.key() + ": " + new String(header.value(), StandardCharsets.UTF_8))
                    .collect(Collectors.joining(", "));
            log.info("With headers: {}", headerString);
            this.apKafkaTemplate.send(producerRecord);
            counter.updateAndGet(v -> v + 1);
        });

        this.apKafkaTemplate.flush();
        log.info("Sends {} records", counter);
    }

}

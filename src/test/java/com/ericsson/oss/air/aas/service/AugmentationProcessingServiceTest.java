/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.aas.service;

import static com.ericsson.oss.air.aas.service.schema.SchemaRegistryServiceTest.NOT_FOUND_EXCEPTION_MESSAGE;
import static com.ericsson.oss.air.aas.service.schema.SchemaRegistryServiceTest.SR_NOT_EXTENDED_REST_EXCEPTION;
import static com.ericsson.oss.air.aas.service.schema.SchemaRegistryServiceTest.SR_NOT_FOUND_REST_EXCEPTION;
import static com.ericsson.oss.air.util.logging.TestLoggingUtils.EXCEPTION_MSG;
import static com.ericsson.oss.air.util.logging.TestLoggingUtils.assertAuditEvent;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.ConnectException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.ericsson.oss.air.aas.config.kafka.APKafkaProperties;
import io.micrometer.core.instrument.Counter;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.DisconnectException;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.core.KafkaTemplate;

@ExtendWith(MockitoExtension.class)
class AugmentationProcessingServiceTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private APKafkaProperties apKafkaProperties;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private KafkaTemplate apKafkaTemplate;

    @Mock
    private Counter outputRecordsCounter;

    @InjectMocks
    private AugmentationProcessingService augProcService;

    private Logger log;

    private ListAppender<ILoggingEvent> listAppender;

    @BeforeEach
    public void setUp() {
        this.log = (Logger) LoggerFactory.getLogger(AugmentationProcessingService.class);
        this.log.setLevel(Level.INFO);

        this.listAppender = new ListAppender<>();
        this.listAppender.start();

        this.log.addAppender(this.listAppender);
    }

    @AfterEach
    public void tearDown() throws Exception {
        this.listAppender.stop();
    }

    @Test
    void sendGenericDataRecord_success() {
        final String userSchema = "{\"namespace\": \"example.avro\", \"type\": \"record\", " +
                "\"name\": \"schemaName\", \"fields\": [{\"name\": \"testName\", \"type\": \"string\"}]}";

        final Schema schema = new Schema.Parser().parse(userSchema);
        final GenericRecord outputRecord = new GenericData.Record(schema);
        outputRecord.put("testName", "correctName");

        final Headers headers = new RecordHeaders();
        headers.add("schemaSubject", "5G.CORE.PM_COUNTERS.OLD".getBytes(StandardCharsets.UTF_8));
        headers.add("schemaID", "251".getBytes(StandardCharsets.UTF_8));
        final ProducerRecord<String, GenericRecord> outgoingRecord = new ProducerRecord<>("topic", null, "key", outputRecord, headers);

        when(this.apKafkaProperties.getAugmentationProcessing().getTopic()).thenReturn("topic");

        this.augProcService.sendRecord(outputRecord, headers, "key");
        verify(this.apKafkaTemplate, times(1)).send(outgoingRecord);
        verify(this.apKafkaProperties, times(2)).getAugmentationProcessing();
        verify(this.outputRecordsCounter, times(1)).increment();
    }

    @Test
    void sendGenericDataRecord_failure_NonConnectivityFailure() {
        final String userSchema = "{\"namespace\": \"example.avro\", \"type\": \"record\", " +
                "\"name\": \"schemaName\", \"fields\": [{\"name\": \"testName\", \"type\": \"string\"}]}";

        final Schema schema = new Schema.Parser().parse(userSchema);
        final GenericRecord outputRecord = new GenericData.Record(schema);
        outputRecord.put("testName", "correctName");

        final Headers headers = new RecordHeaders();
        headers.add("schemaSubject", "5G.CORE.PM_COUNTERS.OLD".getBytes(StandardCharsets.UTF_8));
        headers.add("schemaID", "251".getBytes(StandardCharsets.UTF_8));
        final ProducerRecord<String, GenericRecord> outgoingRecord = new ProducerRecord<>("topic", null, "key", outputRecord, headers);

        when(this.apKafkaProperties.getAugmentationProcessing().getTopic()).thenReturn("topic");
        when(this.apKafkaTemplate.send(outgoingRecord)).thenThrow(RuntimeException.class);

        assertThrows(RuntimeException.class, () -> this.augProcService.sendRecord(outputRecord, headers, "key"));
        verify(this.apKafkaTemplate, times(1)).send(outgoingRecord);
        verify(this.apKafkaProperties, times(2)).getAugmentationProcessing();
        verify(this.outputRecordsCounter, never()).increment();
        assertTrue(this.listAppender.list.isEmpty());

    }

    @Test
    void sendGenericDataRecord_failure_KafkaConnectivityFailure() {
        final Exception exception = new KafkaException(EXCEPTION_MSG, new DisconnectException());

        final String userSchema = "{\"namespace\": \"example.avro\", \"type\": \"record\", " +
                "\"name\": \"schemaName\", \"fields\": [{\"name\": \"testName\", \"type\": \"string\"}]}";

        final Schema schema = new Schema.Parser().parse(userSchema);
        final GenericRecord outputRecord = new GenericData.Record(schema);
        outputRecord.put("testName", "correctName");

        final Headers headers = new RecordHeaders();
        headers.add("schemaSubject", "5G.CORE.PM_COUNTERS.OLD".getBytes(StandardCharsets.UTF_8));
        headers.add("schemaID", "251".getBytes(StandardCharsets.UTF_8));
        final ProducerRecord<String, GenericRecord> outgoingRecord = new ProducerRecord<>("topic", null, "key", outputRecord, headers);

        when(this.apKafkaProperties.getAugmentationProcessing().getTopic()).thenReturn("topic");
        when(this.apKafkaTemplate.send(outgoingRecord)).thenThrow(exception);

        assertThrows(KafkaException.class, () -> this.augProcService.sendRecord(outputRecord, headers, "key"));
        verify(this.apKafkaTemplate, times(1)).send(outgoingRecord);
        verify(this.apKafkaProperties, times(2)).getAugmentationProcessing();
        verify(this.outputRecordsCounter, never()).increment();

        final List<ILoggingEvent> loggingEventList = this.listAppender.list;

        assertFalse(loggingEventList.isEmpty());
        assertEquals(1, loggingEventList.size());
        assertAuditEvent(this.listAppender.list.get(0), Level.ERROR, "Cannot connect to Kafka: ", exception);

    }

    @Test
    void sendGenericDataRecord_failure_SchemaRegistryFailure_NonAuditLog() {
        final Exception exception = new SerializationException(EXCEPTION_MSG, SR_NOT_EXTENDED_REST_EXCEPTION);

        final String userSchema = "{\"namespace\": \"example.avro\", \"type\": \"record\", " +
                "\"name\": \"schemaName\", \"fields\": [{\"name\": \"testName\", \"type\": \"string\"}]}";

        final Schema schema = new Schema.Parser().parse(userSchema);
        final GenericRecord outputRecord = new GenericData.Record(schema);
        outputRecord.put("testName", "correctName");

        final Headers headers = new RecordHeaders();
        headers.add("schemaSubject", "5G.CORE.PM_COUNTERS.OLD".getBytes(StandardCharsets.UTF_8));
        headers.add("schemaID", "251".getBytes(StandardCharsets.UTF_8));
        final ProducerRecord<String, GenericRecord> outgoingRecord = new ProducerRecord<>("topic", null, "key", outputRecord, headers);

        when(this.apKafkaProperties.getAugmentationProcessing().getTopic()).thenReturn("topic");
        when(this.apKafkaTemplate.send(outgoingRecord)).thenThrow(exception);

        assertThrows(SerializationException.class, () -> this.augProcService.sendRecord(outputRecord, headers, "key"));
        verify(this.apKafkaTemplate, times(1)).send(outgoingRecord);
        verify(this.apKafkaProperties, times(2)).getAugmentationProcessing();
        verify(this.outputRecordsCounter, never()).increment();
        assertTrue(this.listAppender.list.isEmpty());
    }

    @Test
    void sendGenericDataRecord_failure_SchemaRegistryFailure_AuditLog_RestClientException() {
        final Exception exception = new SerializationException(EXCEPTION_MSG, SR_NOT_FOUND_REST_EXCEPTION);

        final String userSchema = "{\"namespace\": \"example.avro\", \"type\": \"record\", " +
                "\"name\": \"schemaName\", \"fields\": [{\"name\": \"testName\", \"type\": \"string\"}]}";

        final Schema schema = new Schema.Parser().parse(userSchema);
        final GenericRecord outputRecord = new GenericData.Record(schema);
        outputRecord.put("testName", "correctName");

        final Headers headers = new RecordHeaders();
        headers.add("schemaSubject", "5G.CORE.PM_COUNTERS.OLD".getBytes(StandardCharsets.UTF_8));
        headers.add("schemaID", "251".getBytes(StandardCharsets.UTF_8));
        final ProducerRecord<String, GenericRecord> outgoingRecord = new ProducerRecord<>("topic", null, "key", outputRecord, headers);

        when(this.apKafkaProperties.getAugmentationProcessing().getTopic()).thenReturn("topic");
        when(this.apKafkaTemplate.send(outgoingRecord)).thenThrow(exception);

        assertThrows(SerializationException.class, () -> this.augProcService.sendRecord(outputRecord, headers, "key"));
        verify(this.apKafkaTemplate, times(1)).send(outgoingRecord);
        verify(this.apKafkaProperties, times(2)).getAugmentationProcessing();
        verify(this.outputRecordsCounter, never()).increment();

        final List<ILoggingEvent> loggingEventList = this.listAppender.list;

        assertFalse(loggingEventList.isEmpty());
        assertEquals(1, loggingEventList.size());
        assertAuditEvent(loggingEventList.get(0), Level.ERROR, "Schema Registry request failed: 404 Not Found", SR_NOT_FOUND_REST_EXCEPTION,
                NOT_FOUND_EXCEPTION_MESSAGE);
    }

    @Test
    void sendGenericDataRecord_failure_SchemaRegistryFailure_AuditLog_IOException() {
        final Exception exception = new SerializationException(EXCEPTION_MSG, new ConnectException());

        final String userSchema = "{\"namespace\": \"example.avro\", \"type\": \"record\", " +
                "\"name\": \"schemaName\", \"fields\": [{\"name\": \"testName\", \"type\": \"string\"}]}";

        final Schema schema = new Schema.Parser().parse(userSchema);
        final GenericRecord outputRecord = new GenericData.Record(schema);
        outputRecord.put("testName", "correctName");

        final Headers headers = new RecordHeaders();
        headers.add("schemaSubject", "5G.CORE.PM_COUNTERS.OLD".getBytes(StandardCharsets.UTF_8));
        headers.add("schemaID", "251".getBytes(StandardCharsets.UTF_8));
        final ProducerRecord<String, GenericRecord> outgoingRecord = new ProducerRecord<>("topic", null, "key", outputRecord, headers);

        when(this.apKafkaProperties.getAugmentationProcessing().getTopic()).thenReturn("topic");
        when(this.apKafkaTemplate.send(outgoingRecord)).thenThrow(exception);

        assertThrows(SerializationException.class, () -> this.augProcService.sendRecord(outputRecord, headers, "key"));
        verify(this.apKafkaTemplate, times(1)).send(outgoingRecord);
        verify(this.apKafkaProperties, times(2)).getAugmentationProcessing();
        verify(this.outputRecordsCounter, never()).increment();

        final List<ILoggingEvent> loggingEventList = this.listAppender.list;

        assertFalse(loggingEventList.isEmpty());
        assertEquals(1, loggingEventList.size());
        assertAuditEvent(loggingEventList.get(0), Level.ERROR, "Schema Registry request failed: ", exception);
    }

}
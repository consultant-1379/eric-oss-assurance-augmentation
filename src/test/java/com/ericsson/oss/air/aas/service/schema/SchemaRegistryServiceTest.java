/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.aas.service.schema;

import static com.ericsson.oss.air.util.logging.TestLoggingUtils.assertAuditEvent;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.ericsson.oss.air.aas.config.schemaregistry.SchemaRegistryConfiguration;
import com.ericsson.oss.air.exception.SchemaRegistryHttpClientErrorException;
import com.ericsson.oss.air.exception.SchemaRegistryHttpServerErrorException;
import io.confluent.kafka.schemaregistry.ParsedSchema;
import io.confluent.kafka.schemaregistry.avro.AvroSchema;
import io.confluent.kafka.schemaregistry.client.SchemaMetadata;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import org.apache.avro.SchemaParseException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

@ExtendWith(MockitoExtension.class)
public class SchemaRegistryServiceTest {

    private static final String SUBJECT = "nose_test_topic_p1_r3_pw";

    private static final ParsedSchema PARSED_SCHEMA = new AvroSchema(avroSchemaString(SUBJECT));

    private static final int EXPECTED_VERSION = 1;

    private static final String EXCEPTION_MESSAGE = "Tulips have not bloomed yet";

    public static final String NOT_FOUND_EXCEPTION_MESSAGE = EXCEPTION_MESSAGE + "; error code: 404";

    private static final String NOT_EXTENDED_EXCEPTION_MESSAGE = EXCEPTION_MESSAGE + "; error code: 510";

    public static final RestClientException SR_NOT_FOUND_REST_EXCEPTION = new RestClientException(EXCEPTION_MESSAGE, 404, 404);

    public static final RestClientException SR_NOT_EXTENDED_REST_EXCEPTION = new RestClientException(EXCEPTION_MESSAGE, 510, 510);

    @Mock
    private SchemaRegistryClient schemaRegistryClient;

    @Mock
    private SchemaRegistryConfiguration schemaRegistryConfiguration;

    @InjectMocks
    private SchemaRegistryService schemaRegistryService;

    private Logger log;

    private ListAppender<ILoggingEvent> listAppender;

    @BeforeEach
    public void setUp() {
        this.log = (Logger) LoggerFactory.getLogger(SchemaRegistryService.class);
        this.log.setLevel(Level.ERROR);

        this.listAppender = new ListAppender<>();
        this.listAppender.start();

        this.log.addAppender(this.listAppender);
    }

    @AfterEach
    public void tearDown() throws Exception {
        this.listAppender.stop();
    }

    @Test
    void registerMethod_ReturnSchemaVersion() throws RestClientException, IOException {

        when(this.schemaRegistryConfiguration.getSchemaRegistryClient()).thenReturn(this.schemaRegistryClient);
        when(this.schemaRegistryClient.register(SUBJECT, PARSED_SCHEMA)).thenReturn(EXPECTED_VERSION);
        assertEquals(EXPECTED_VERSION, this.schemaRegistryService.register(SUBJECT, PARSED_SCHEMA));
    }

    @Test
    void registerMethod_ThrowsSchemaRegistryHttpServerErrorException() throws RestClientException, IOException {

        when(this.schemaRegistryConfiguration.getSchemaRegistryClient()).thenReturn(this.schemaRegistryClient);
        when(this.schemaRegistryClient.register(SUBJECT, PARSED_SCHEMA)).thenThrow(SR_NOT_EXTENDED_REST_EXCEPTION);
        assertThrows(SchemaRegistryHttpServerErrorException.class, () -> this.schemaRegistryService.register(SUBJECT, PARSED_SCHEMA));

        assertTrue(this.listAppender.list.isEmpty());
    }

    @Test
    void registerMethod_ThrowsSchemaRegistryHttpClientErrorException() throws RestClientException, IOException {

        when(this.schemaRegistryConfiguration.getSchemaRegistryClient()).thenReturn(this.schemaRegistryClient);
        when(this.schemaRegistryClient.register(SUBJECT, PARSED_SCHEMA)).thenThrow(SR_NOT_FOUND_REST_EXCEPTION);
        assertThrows(SchemaRegistryHttpClientErrorException.class, () -> this.schemaRegistryService.register(SUBJECT, PARSED_SCHEMA));

        final List<ILoggingEvent> loggingEventList = this.listAppender.list;

        assertEquals(1, loggingEventList.size());
        assertAuditEvent(loggingEventList.get(0), Level.ERROR, "Schema Registry request failed: 404 Not Found", SR_NOT_FOUND_REST_EXCEPTION,
                NOT_FOUND_EXCEPTION_MESSAGE);
    }

    @Test
    void getVersionMethod_returnSchemaId() throws IOException, RestClientException {

        when(this.schemaRegistryConfiguration.getSchemaRegistryClient()).thenReturn(this.schemaRegistryClient);
        when(this.schemaRegistryClient.getVersion(SUBJECT, PARSED_SCHEMA)).thenReturn(EXPECTED_VERSION);
        assertEquals(EXPECTED_VERSION, schemaRegistryService.getVersion(SUBJECT, new AvroSchema(avroSchemaString(SUBJECT))));
    }

    @Test
    void getVersionMethod_ThrowsSchemaRegistryHttpServerErrorException() throws IOException, RestClientException {

        when(this.schemaRegistryConfiguration.getSchemaRegistryClient()).thenReturn(this.schemaRegistryClient);
        when(this.schemaRegistryClient.getVersion(SUBJECT, PARSED_SCHEMA)).thenThrow(SR_NOT_EXTENDED_REST_EXCEPTION);
        assertThrows(SchemaRegistryHttpServerErrorException.class,
                () -> schemaRegistryService.getVersion(SUBJECT, new AvroSchema(avroSchemaString(SUBJECT))));
    }

    @Test
    void getVersionMethod_ThrowsSchemaRegistryHttpClientErrorException() throws IOException, RestClientException {

        when(this.schemaRegistryConfiguration.getSchemaRegistryClient()).thenReturn(this.schemaRegistryClient);
        when(this.schemaRegistryClient.getVersion(SUBJECT, PARSED_SCHEMA)).thenThrow(SR_NOT_FOUND_REST_EXCEPTION);
        assertThrows(SchemaRegistryHttpClientErrorException.class,
                () -> schemaRegistryService.getVersion(SUBJECT, new AvroSchema(avroSchemaString(SUBJECT))));

        final List<ILoggingEvent> loggingEventList = this.listAppender.list;

        assertEquals(1, loggingEventList.size());
        assertAuditEvent(loggingEventList.get(0), Level.ERROR, "Schema Registry request failed: 404 Not Found", SR_NOT_FOUND_REST_EXCEPTION,
                NOT_FOUND_EXCEPTION_MESSAGE);
    }

    @Test
    void isSchemaFound_returnTrue() throws IOException, RestClientException {
        when(this.schemaRegistryConfiguration.getSchemaRegistryClient()).thenReturn(this.schemaRegistryClient);
        when(this.schemaRegistryClient.getId(SUBJECT, PARSED_SCHEMA)).thenReturn(1);
        assertTrue(this.schemaRegistryService.isSchemaFound(SUBJECT, new AvroSchema(avroSchemaString(SUBJECT))));
    }

    @Test
    void isSchemaFound_returnFalse() throws IOException, RestClientException {
        when(this.schemaRegistryConfiguration.getSchemaRegistryClient()).thenReturn(this.schemaRegistryClient);
        when(this.schemaRegistryClient.getId(SUBJECT, PARSED_SCHEMA)).thenThrow(SR_NOT_FOUND_REST_EXCEPTION);
        assertFalse(this.schemaRegistryService.isSchemaFound(SUBJECT, new AvroSchema(avroSchemaString(SUBJECT))));

        final List<ILoggingEvent> loggingEventList = this.listAppender.list;

        assertEquals(1, loggingEventList.size());
        assertAuditEvent(loggingEventList.get(0), Level.ERROR, "Schema Registry request failed: 404 Not Found", SR_NOT_FOUND_REST_EXCEPTION,
                NOT_FOUND_EXCEPTION_MESSAGE);
    }

    @Test
    void isSchemaFound_ThrowsSchemaRegistryHttpServerErrorException() throws IOException, RestClientException {
        when(this.schemaRegistryConfiguration.getSchemaRegistryClient()).thenReturn(this.schemaRegistryClient);
        when(this.schemaRegistryClient.getId(SUBJECT, PARSED_SCHEMA)).thenThrow(SR_NOT_EXTENDED_REST_EXCEPTION);
        assertThrows(RestClientException.class, () -> this.schemaRegistryService.isSchemaFound(SUBJECT, new AvroSchema(avroSchemaString(SUBJECT))));

        assertTrue(this.listAppender.list.isEmpty());
    }

    @Test
    void isSchemaFound_ThrowsSchemaRegistryHttpClientErrorException() throws IOException, RestClientException {

        final RestClientException exception = new RestClientException(EXCEPTION_MESSAGE, 400, 400);

        when(this.schemaRegistryConfiguration.getSchemaRegistryClient()).thenReturn(this.schemaRegistryClient);
        when(this.schemaRegistryClient.getId(SUBJECT, PARSED_SCHEMA)).thenThrow(exception);
        assertThrows(RestClientException.class, () -> this.schemaRegistryService.isSchemaFound(SUBJECT, new AvroSchema(avroSchemaString(SUBJECT))));

        final List<ILoggingEvent> loggingEventList = this.listAppender.list;

        assertEquals(1, loggingEventList.size());
        assertAuditEvent(loggingEventList.get(0), Level.ERROR, "Schema Registry request failed: 400 Bad Request", exception,
                EXCEPTION_MESSAGE + "; error code: 400");
    }

    @Test
    void parseSchemaMethod_success() {
        final String avroSchema = avroSchemaString("valid");
        final Optional<AvroSchema> parsedSchema = schemaRegistryService.parseSchema(avroSchema);

        assertEquals(avroSchema, parsedSchema.get().toString());
    }

    @Test
    void parseSchemaMethod_emptySchema_throwException() {
        final String avroSchema = avroSchemaString("");
        final Optional<AvroSchema> parsedSchema = schemaRegistryService.parseSchema(avroSchema);

        assertEquals(Optional.empty(), parsedSchema);
    }

    @Test
    void getLatestSchemaMetadata_success() throws RestClientException, IOException {
        final String avroSchema = avroSchemaString(SUBJECT);

        final SchemaMetadata schemaMetadata = new SchemaMetadata(1, 1, avroSchema);

        when(this.schemaRegistryConfiguration.getSchemaRegistryClient()).thenReturn(this.schemaRegistryClient);
        when(this.schemaRegistryClient.getLatestSchemaMetadata(SUBJECT)).thenReturn(schemaMetadata);

        final Optional<SchemaMetadata> latestSchemaMetadata = schemaRegistryService.getLatestSchemaMetadata(SUBJECT);
        assertEquals(schemaMetadata, latestSchemaMetadata.get());
    }

    @Test
    void getLatestSchemaMetadata_ThrowsSchemaParseException() throws RestClientException, IOException {

        when(this.schemaRegistryConfiguration.getSchemaRegistryClient()).thenReturn(this.schemaRegistryClient);
        when(this.schemaRegistryClient.getLatestSchemaMetadata(SUBJECT)).thenThrow(SchemaParseException.class);

        final Optional<SchemaMetadata> latestSchemaMetadata = schemaRegistryService.getLatestSchemaMetadata(SUBJECT);
        assertEquals(Optional.empty(), latestSchemaMetadata);
    }

    @Test
    void getLatestSchemaMetadata_ThrowsSchemaRegistryHttpServerErrorException() throws RestClientException, IOException {

        when(this.schemaRegistryConfiguration.getSchemaRegistryClient()).thenReturn(this.schemaRegistryClient);
        when(this.schemaRegistryClient.getLatestSchemaMetadata(SUBJECT)).thenThrow(SR_NOT_EXTENDED_REST_EXCEPTION);
        assertThrows(SchemaRegistryHttpServerErrorException.class,
                () -> schemaRegistryService.getLatestSchemaMetadata(SUBJECT));

        assertTrue(this.listAppender.list.isEmpty());
    }

    @Test
    void getLatestSchemaMetadata_ThrowsSchemaRegistryHttpClientErrorException() throws RestClientException, IOException {

        when(this.schemaRegistryConfiguration.getSchemaRegistryClient()).thenReturn(this.schemaRegistryClient);
        when(this.schemaRegistryClient.getLatestSchemaMetadata(SUBJECT)).thenThrow(SR_NOT_FOUND_REST_EXCEPTION);
        assertThrows(SchemaRegistryHttpClientErrorException.class,
                () -> schemaRegistryService.getLatestSchemaMetadata(SUBJECT));

        final List<ILoggingEvent> loggingEventList = this.listAppender.list;

        assertEquals(1, loggingEventList.size());
        assertAuditEvent(loggingEventList.get(0), Level.ERROR, "Schema Registry request failed: 404 Not Found", SR_NOT_FOUND_REST_EXCEPTION,
                NOT_FOUND_EXCEPTION_MESSAGE);
    }

    @Test
    void getLatestSchema_success() throws RestClientException, IOException {
        final String avroSchema = avroSchemaString(SUBJECT);
        final SchemaMetadata schemaMetadata = new SchemaMetadata(1, 1, avroSchema);

        when(this.schemaRegistryConfiguration.getSchemaRegistryClient()).thenReturn(this.schemaRegistryClient);
        when(this.schemaRegistryClient.getLatestSchemaMetadata(SUBJECT)).thenReturn(schemaMetadata);

        final Optional<AvroSchema> latestAvroSchema = schemaRegistryService.getLatestSchema(SUBJECT);
        assertEquals(avroSchema, latestAvroSchema.get().toString());
    }

    @Test
    void getLatestSchema_null_returnEmpty() throws RestClientException, IOException {
        final String avroSchema = avroSchemaString(SUBJECT);
        final SchemaMetadata schemaMetadata = new SchemaMetadata(1, 1, avroSchema);

        when(this.schemaRegistryConfiguration.getSchemaRegistryClient()).thenReturn(this.schemaRegistryClient);
        when(this.schemaRegistryClient.getLatestSchemaMetadata(SUBJECT)).thenReturn(null);

        final Optional<AvroSchema> latestAvroSchema = schemaRegistryService.getLatestSchema(SUBJECT);
        assertEquals(Optional.empty(), latestAvroSchema);
    }

    private static String avroSchemaString(final String name) {
        return "{\"type\":\"record\",\"name\":\"" + name + "\"," + "\"fields\":[{\"name\":\"name\",\"type\":\"string\"},"
                + "{\"name\":\"secondFiled\",\"type\":\"int\"}]}";
    }
}
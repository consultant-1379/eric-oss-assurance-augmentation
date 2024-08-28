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

package com.ericsson.oss.air.aas.config.kafka.dynamic;

import static com.ericsson.oss.air.aas.handler.RegistrationTestUtil.SCHEMA_NAMESPACE;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Collections;

import com.ericsson.oss.air.aas.handler.AugmentationProcessor;
import com.ericsson.oss.air.aas.model.record.SchemaSubject;
import io.micrometer.core.instrument.Counter;
import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class APMessageListenerTest {

    private static final String INPUT_SCHEMA_NAME = "inputSchemaName";

    private static final String FIELD_1 = "field1";

    private static final String ARDQ_TYPE = "CORE";

    private static final Schema INPUT_SCHEMA = SchemaBuilder
            .record(INPUT_SCHEMA_NAME)
            .namespace("5G." + ARDQ_TYPE + ".PM_COUNTERS").fields()
            .name(FIELD_1).type().optional().stringType()
            .endRecord();

    private static final Schema BAD_FORMAT_INPUT_SCHEMA = SchemaBuilder
            .record(INPUT_SCHEMA_NAME)
            .namespace("5G|" + ARDQ_TYPE + "|PM_COUNTERS").fields()
            .name(FIELD_1).type().optional().stringType()
            .endRecord();

    public static final SchemaSubject SCHEMA_SUBJECT = SchemaSubject.parse("5G.CORE.PM_COUNTERS" + "." + INPUT_SCHEMA_NAME);
    public static final SchemaSubject NOT_MATCHED_SCHEMA_SUBJECT_OBJ = SchemaSubject.parse(SCHEMA_NAMESPACE + "." + "invalidName");
    public static final SchemaSubject NOT_MATCHED_NS_SCHEMA_SUBJECT_OBJ = SchemaSubject.parse("4G.CORE.PM_COUNTERS" + "." + INPUT_SCHEMA_NAME);

    private APMessageListener apMessageListener;

    @Mock
    private Counter augmentedInputRecordsCounter;

    @Mock
    private Counter nonAugmentedInputRecordsCounter;


    @BeforeEach
    void setUp() {
        this.apMessageListener = new APMessageListener(new ArrayList<>(), SCHEMA_SUBJECT,
                this.augmentedInputRecordsCounter, this.nonAugmentedInputRecordsCounter);
    }

    @Test
    void test_onMessage_AugmentIncomingRecord() {

        final AugmentationProcessor augmentationProcessor = mock(AugmentationProcessor.class);

        final APMessageListener apMessageListener = new APMessageListener(Collections.singletonList(augmentationProcessor), SCHEMA_SUBJECT,
                this.augmentedInputRecordsCounter, this.nonAugmentedInputRecordsCounter);

        final String inputSchema = "{\"type\":\"record\",\"name\":\"inputSchemaName\",\"namespace\":\"5G.CORE.PM_COUNTERS\",\"fields\":[{\"name\":\"field1\",\"type\":\"string\"}]}";

        final var inputRecord = new GenericData.Record(new Schema.Parser().parse(inputSchema));
        final ConsumerRecord<String, GenericRecord> record = new ConsumerRecord<>("topic", 0, 0, "Key", inputRecord);

        doNothing().when(augmentationProcessor).apply(record);
        apMessageListener.onMessage(record);

        verify(augmentationProcessor, times(1)).apply(record);
        verify(this.augmentedInputRecordsCounter, times(1)).increment();
        verify(this.nonAugmentedInputRecordsCounter, times(0)).increment();
    }

    @Test
    void test_onMessage_DoNotAugmentIncomingRecord() {

        final AugmentationProcessor augmentationProcessor = mock(AugmentationProcessor.class);

        final APMessageListener apMessageListener = new APMessageListener(Collections.singletonList(augmentationProcessor),
                NOT_MATCHED_NS_SCHEMA_SUBJECT_OBJ, this.augmentedInputRecordsCounter, this.nonAugmentedInputRecordsCounter);

        final String inputSchema = "{\"type\":\"record\",\"name\":\"inputSchemaName\",\"namespace\":\"5G.CORE.PM_COUNTERS\",\"fields\":[{\"name\":\"field1\",\"type\":\"string\"}]}";

        final var inputRecord = new GenericData.Record(new Schema.Parser().parse(inputSchema));
        final ConsumerRecord<String, GenericRecord> record = new ConsumerRecord<>("topic", 0, 0, "Key", inputRecord);

        apMessageListener.onMessage(record);

        verify(augmentationProcessor, times(0)).apply(record);
        verify(this.augmentedInputRecordsCounter, never()).increment();
        verify(this.nonAugmentedInputRecordsCounter, times(1)).increment();
    }

    @Test
    void testValidSchemaInformation_true() {
        assertTrue(this.apMessageListener.validSchemaInformation(INPUT_SCHEMA, SCHEMA_SUBJECT));
    }

    @Test
    void testValidSchemaInformation_false_wrongFormat() {
        assertFalse(this.apMessageListener.validSchemaInformation(BAD_FORMAT_INPUT_SCHEMA, SCHEMA_SUBJECT));
    }

    @Test
    void testValidSchemaInformation_false_nameNotMatch() {
        assertFalse(this.apMessageListener.validSchemaInformation(INPUT_SCHEMA, NOT_MATCHED_SCHEMA_SUBJECT_OBJ));
    }

    @Test
    void testValidSchemaInformation_false_nameSpaceNotMatch() {
        assertFalse(
                this.apMessageListener.validSchemaInformation(INPUT_SCHEMA, NOT_MATCHED_NS_SCHEMA_SUBJECT_OBJ));
    }

}
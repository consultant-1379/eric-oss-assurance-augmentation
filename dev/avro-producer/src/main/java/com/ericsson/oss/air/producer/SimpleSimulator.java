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

package com.ericsson.oss.air.producer;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.Scanner;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SimpleSimulator {

    final String amf_str = "amf_mobility_networkslice.json";

    @Value("${data.period}")
    private Integer period;
    @Value("${data.rate}")
    private Integer rate;

    @Value("${kafka.topic}")
    private String topic;

    @Autowired
    @Qualifier("kfTemplate")
    private KafkaTemplate<String, Object> apKafkaTemplate;

    @Autowired
    private ApplicationContext applicationContext;

    public void sayHi() {
        log.info("Simple Simulator say hi");
    }

    private String openFile(final String file) {

        final InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(file);
        if (inputStream != null) {
            final Scanner scanner = new Scanner(inputStream).useDelimiter("\\A");
            final String content = scanner.hasNext() ? scanner.next() : "";

            try {
                inputStream.close();
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }

            return content;
        } else {
            System.out.println("Resource not found.");
        }
        return null;
    }

    private GenericRecord createRecord(final String schemaStr, final int index, final long startTime) {

        final Schema schema = new Schema.Parser().parse(schemaStr);
        final long epochSecond = Instant.now().getEpochSecond();

        final GenericRecord outputRecord = new GenericData.Record(schema);
        outputRecord.put("dnPrefix", "dnPrefix");
        outputRecord.put("ropEndTime", Long.toString(epochSecond));
        outputRecord.put("ropBeginTime", "1684340176");

        outputRecord.put("snssai", "11-1");
        outputRecord.put("nodeFDN", "pcc-amf2");
        outputRecord.put("moFdn", "[snssai=11-1]");

        final GenericData.Record pmCounters = new GenericData.Record(schema.getField("pmCounters").schema().getTypes().get(1));
        pmCounters.put("VS_NS_NbrRegisteredSub_5GS", 15);

        outputRecord.put("pmCounters", pmCounters);

        outputRecord.put("apn", index + "_message_" + startTime);

        return outputRecord;

    }

    public void init() throws InterruptedException {

        final long startTime = Instant.now().getEpochSecond();

        final int messagePerSecond = this.rate;
        final int testPeriod = this.period;

        log.info("Start simulating traffic");
        log.info("Testing period: {} seconds", testPeriod);
        log.info("Number of message per second: {}", messagePerSecond);

        final String schema = this.openFile(this.amf_str);
        log.info("Schema loaded: {}", schema);

        int index = 0;
        for (int j = 0; j < testPeriod; j++) {
            final long expectedDeliveryTime = startTime + j;
            final long currentTime = Instant.now().getEpochSecond();

            log.info("Simulator life time {}s", j);
            if (expectedDeliveryTime > currentTime) {
                log.info("Sleep 1s");
                Thread.sleep(1000);
            }

            log.info("Sending {} records to topic [{}]", messagePerSecond, this.topic);
            for (int i = 0; i < messagePerSecond; i++) {
                index++;
                final GenericRecord outputRecord = this.createRecord(schema, index, startTime);
                this.apKafkaTemplate.send(this.topic, outputRecord);
            }
            this.apKafkaTemplate.flush();

        }

        log.info("Simulation finished");
        final int killBreak = 2000;
        log.info("Closing process in {} millisecond", killBreak);

        Thread.sleep(killBreak);
        ((ConfigurableApplicationContext) this.applicationContext).close();

    }
}

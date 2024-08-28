# Avro Producer

The Avro Producer serves a straightforward function: **it sends messages to Kafka in Avro format**, adhering to the Confluent.io style.

This involves communicating with the Schema Registry to fetch the corresponding schema, serializing the data to Avro, and transmitting it to Kafka. The message header also incorporates the schema subject, schema version, schema ID, and the schema itself.

## Building and Running the Avro Producer

### Run Avro Producer using Spring Boot Maven plugin

```shell
# Send CORE PM records
mvn spring-boot:run -Dspring-boot.run.arguments="--file=../pm-records/records.avro"

# Send RAN PM records
mvn spring-boot:run -Dspring-boot.run.arguments="--topic=eric-oss-3gpp-pm-xml-ran-parser-ebsn --file=../pm-records/ran-records.avro"
```


### Run Avro Producer as Java jar
The tool can be built by executing `mvn clean package` in this current directory.

```shell
mvn clean package
java -jar ./target/eric-oss-avro-producer.jar --file=../pm-records/records.avro
java -jar ./target/eric-oss-avro-producer.jar --file=../pm-records/ran-records.avro --topic=eric-oss-3gpp-pm-xml-ran-parser-ebsn --schemaRegistry=http://localhost:8081 --kafka=localhost:9092

```

The default configuration is located in /src/main/resources/application.yaml. It can be easily overridden, e.g.

```
java -jar ./target/eric-oss-avro-producer.jar --file=../pm-records/records.avro --topic=eric-oss-3gpp-pm-xml-core-parser- --schemaRegistry=http://localhost:8081 --kafka=localhost:9092

```

The `--file` argument is essential for the Avro Producer, indicating the data file's path. Each line holds a single data entry, with 'subject' containing the schema subject of that record, and 'data' containing the actual data from the file. The data must align with the schemas.

Here is an example of the content within a data file:

```json
{"subject": "AMF.Core.PM_COUNTERS.AMF_Mobility_NetworkSlice_1",      "data": {"dnPrefix":{"string":""}, "nodeFDN":"PCC00011-AMF", "elementType":null, "moFdn":"/system/mm/network-slice[snssai=3-9]",                                                                          "snssai":{"string":"3-9"},      "snssai_sst":{"string":"3"}, "snssai_sd":{"string":"9"},  "ropBeginTime":"2024-01-25T18:00:00+00:00", "ropEndTime":"2024-01-25T18:15:00+00:00", "suspect":{"boolean":false}, "apn":null,                          "pmCounters":{"AMF.Core.PM_COUNTERS.pmMetricsSchema":{"VS_NS_NbrRegisteredSub_5GS":{"int":23}}}}}
{"subject":"UPF.Core.PM_COUNTERS.up_payload_dnn_slice_1",            "data":{"dnPrefix":{"string":""},  "nodeFDN":"PCG00031-UPF", "elementType":null, "moFdn":"/pcupe:user-plane/pcupdnn:dnns/pcupdnn:dnn[name=dnnB.ericsson.se]/pcupdnn:slices/pcupdnn:slice[name=3-000009]", "snssai":{"string":"3-000009"}, "snssai_sst":{"string":"3"}, "snssai_sd":{"string":"9"},  "ropBeginTime":"2024-01-25T18:15:00+00:00", "ropEndTime":"2024-01-25T18:30:00+00:00", "suspect":{"boolean":false}, "apn":{"string":"dnnB.ericsson.se"}, "pmCounters":{"UPF.Core.PM_COUNTERS.pmMetricsSchema":{"ul_ipv4_received_bytes":{"int":10800},      "ul_ipv6_received_bytes":{"int":1080},  "dl_ipv4_received_bytes":{"int":21600},  "dl_ipv6_received_bytes":{"int":2160},  "ul_ipv4_drop_packets":{"int":108},  "dl_ipv4_drop_packets":{"int":216},  "ul_ipv6_drop_packets":{"int":108},  "dl_ipv6_drop_packets":{"int":216},  "ul_unstr_drop_packets":{"int":108},  "dl_unstr_drop_packets":{"int":216},  "ul_unstr_received_bytes":{"int":108},  "dl_unstr_received_bytes":{"int":216}}}}}
{"subject":"SMF.Core.PM_COUNTERS.smf_nsmf_pdu_session_snssai_apn_1", "data":{"dnPrefix":{"string":""},  "nodeFDN":"PCC00020-SMF", "elementType":null, "moFdn":"/epg:epg/pgw/ns[name=3-000009]/apn[name=dnnC.ericsson.se]",                                                     "snssai":{"string":"3-000009"}, "snssai_sst":{"string":"3"}, "snssai_sd":{"string":"9"},  "ropBeginTime":"2024-01-25T18:15:00+00:00", "ropEndTime":"2024-01-25T18:30:00+00:00", "suspect":{"boolean":false}, "apn":{"string":"dnnC.ericsson.se"}, "pmCounters":{"SMF.Core.PM_COUNTERS.pmMetricsSchema":{"create_sm_context_resp_succ":{"int":13440}, "create_sm_context_req":{"int":26240}}}}}

```

#### Run Avro Producer in a kubectl container

```shell
# Run avro-producer in AAS pod

CURRENT_NS=$(kubectl config view --minify --output 'jsonpath={..namespace}')
AAS_POD_ID=$(kubectl get pods -l app.kubernetes.io/name=eric-oss-assurance-augmentation -o=jsonpath='{.items[0].metadata.name}')
kubectl cp ./target/eric-oss-avro-producer.jar $CURRENT_NS/$AAS_POD_ID:/tmp/
kubectl cp ../pm-records/records.avro $CURRENT_NS/$AAS_POD_ID:/tmp/
kubectl cp ../pm-records/ran-records.avro $CURRENT_NS/$AAS_POD_ID:/tmp/

kubectl exec $AAS_POD_ID -- java -jar /tmp/eric-oss-avro-producer.jar --file=/tmp/records.avro --schemaRegistry=http://eric-schema-registry-sr:8081 --kafka=eric-oss-dmm-kf:9092

```

### Data simulation for PMSC

The Avro Producer can simulate traffic in our partial integration test environment. The folder `../pm-record` contains sample records for CORE and RAN, ready for use. However, PMSC calculations are based solely on record timestamps.

**Therefore, to ensure accurate PMSC performance calculations, please update the ropBeginTime and ropEndTime to your current timestamp in the provided sample records.**


### Build-in Profiles

The default configuration is for executing this tool in a k8s cluster with TLS globally disabled, Postgres, Kafka and a Wiremock server for CARDQ, DC
and SR.

In addition to the default configuration, There are 3 defined profiles.

1. local: this profile is for running the tool locally with TLS disabled and our containerized development environment, e.g. docker-compose.yml in the
   parent directory. See [the dev directory README](../README.md)
2. local-tls: this profile is for running the tool locally with TLS enabled and our containerized development environment for testing secure
   connections. Uses the environment variable ROOT_WRITE_PATH or defaults to /tmp for the root directory of the generated keystore and truststore
   files.
3. tls: this profile is for running the tool in a k8s cluster with TLS globally enabled, Postgres, Kafka and a wiremock server for CARDQ, DC and SR.

For example, if you executing this tool on your machine, you can use the local profile, e.g.

```
java -jar -Dspring.profiles.active=local ./target/eric-oss-avro-producer.jar --file=../pm-records/simple_record_mocker_env.avro
```

The available configuration parameters as shown by showing the properties with the tls profile.

```
dmm:
  schemaRegistry:
    url: http://eric-schema-registry-sr:8081
    tls:
      enabled: true
      trustStoreLocation: /tmp/rootca/truststore.p12
      trustStorePassword: password
      trustStoreType: PKCS12
      keyStoreLocation: /tmp/server/keystore.p12
      keyStorePassword: password
      keyStoreType: PKCS12
  kafka:
    bootstrapServers: eric-oss-dmm-kf:9093
    topic: eric-oss-3gpp-pm-xml-core-parser-
    tls:
      enabled: true
      trustStoreLocation: /tmp/rootca/truststore.p12
      trustStorePassword: password
      trustStoreType: PKCS12
      keyStoreLocation: /tmp/kafka/keystore.p12
      keyStorePassword: password
      keyStoreType: PKCS12

debug: true
```

# Example

## Testing the Augmentation Processing Flow

1. Run the containerized dev environment. See [the dev directory README](../README.md)

2. Create the following ARDQ registration:

```
{
    "ardqId": "cardq",
    "ardqUrl": "http://localhost:8080",
    "rules": [
        {
            "inputSchema": "5G|PM_COUNTERS|AMF_Mobility_NetworkSlice_1",
            "fields": [
            {
                "output": "nsi",
                "input": [
                    "snssai",
                    "nodeFDN"
                ]
            }
            ]
        }
    ]
}
```

3. Build and run the Avro Producer jar. See above. There is a sample AMF Mobility NetworkSlice data stored under `../pm-records/AMF_Mobility_NetworkSlice_1.avro`

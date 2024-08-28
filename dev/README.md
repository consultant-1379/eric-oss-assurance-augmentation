# Dev Environment

[TOC]

## Install and setup podman

### Install [Podman](https://podman.io/getting-started/installation) if docker is not available for you

Install podman using brew if you are using MacOS or following
this [instruction](https://github.com/containers/podman/blob/main/docs/tutorials/podman-for-windows.md) if you are using windows

```
brew install podman
```

Next, create and start your first Podman machine:

```
podman machine init, e.g. podman machine init --cpus=4 --disk-size=60 --memory=6096 -v $HOME:$HOME
podman machine start
```

### Install [podman-compose](https://github.com/containers/podman-compose) if docker-compose is not available for you

podman-compose is an implementation of [Compose Spec](https://compose-spec.io/) with [Podman](https://podman.io/) backend. Install the latest stable
version from PyPI:

```
pip3 install podman-compose
```

### Start dependencies

#### Without mTLS

```shell
docker compose up
# or
podman-compose up
```

#### With mTLS

##### Step 1 Create Required Certificates

```shell
./create-certs.sh certs

docker compose -f docker-compose.yml -f docker-compose.tls.yml up
# or
podman-compose -f docker-compose.yml -f docker-compose.tls.yml up
```

The mocked REST APIs are simultaneously available over https at the port 8443 for mTLS and over http at the port 8080. Kafka is available at the port 9092 for insecure connections and the port 9093 for mTLS connections.

### Stop dependencies

```
docker compose down
# or
podman-compose down
```

### How `create-certs.sh` works

`create-certs.sh certs` will createa all required certificates under certs folder `certs`

```
certs
├── aas
│  ├── kafka
│  │  └── keystore
│  │     ├── server.pem
│  │     └── serverRSA.pem
│  ├── rootca
│  │  └── truststore
│  │     └── rootCA.pem
│  └── server
│     └── keystore
│        ├── server.pem
│        └── serverRSA.pem
├── kafka
│  ├── kafka_credentials
│  ├── keystore.jks
│  └── truststore.jks
└── wiremock
   ├── keystore.jks
   └── truststore.jks

```

based on the generated keys and certificates described on [CSAC AAS mTLS Knowledge Sharing](https://eteamspace.internal.ericsson.com/display/AAP/CSAC+and+AAS+mTLS+Knowledge+Sharing).

To activate mTLS for Kafka, a file is created at this location: `dev/certs/kafka/kafka_credentials`. This file will contain the passwords for both the truststore and keystore.

##### Verify Kafka Connection by Kafka Client (Optional)
You can check that Kafka is set up for mTLS correctly by creating a file called `client.properties` in `dev/certs/kafka` with this content:

```
security.protocol=SSL
ssl.truststore.location=truststore.jks
ssl.truststore.password=password
ssl.keystore.location=keystore.jks
ssl.keystore.password=password
ssl.key.password=password
```

Then download [Kafka](https://archive.apache.org/dist/kafka/3.1.2/kafka_2.13-3.1.2.tgz) and unzip it. Run two terminals with the working directory as `dev/certs/kafka/`:

```
% ~/Documents/aas/kafka_2.13-3.1.2/bin/kafka-console-consumer.sh --bootstrap-server localhost:9093 --consumer.config client.properties -topic testingtime --from-beginning
```

```
% ~/Documents/aas/kafka_2.13-3.1.2/bin/kafka-console-producer.sh --bootstrap-server localhost:9093 --producer.config client.properties -topic testingtime
```

Write a message in the console producer terminal and it should be consumed by the console consumer set up in the other terminal.

##### AAS Certificates

Under directory `dev/certs/aas`, it contains all required certificates to run AAS.
In this example, `adp-certificate.discovery.root-read-path` is `dev/certs/aas`. See [CSAC and AAS mTLS Knowledge Sharing](https://eteamspace.internal.ericsson.com/display/SABSS/SA+-+CSAC+and+AAS+mTLS+Knowledge+Sharing) for further details on how my /opt/certs directory is set up to work with the [2PP Certificate Reloader library](https://gerrit-gamma.gic.ericsson.se/plugins/gitiles/adp-chassis/java/adp-chassis-library/+/master/adp-components/certm/certificate-watcher/README.md). The certificate ID for Kafka keystore is "kafka".


---

## Supported Scenario for Wiremock server

The required Data Catalog and Schema Registry REST APIs are mocked to create the ARDQ registration:

```
{
    "ardqId": "cardq",
    "ardqUrl": "...",
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

The CARDQ REST API is mocked to augment incoming PM Avro records for this registration using the tool available from https://confluence-oss.seli.wh.rnd.internal.ericsson.com/display/AAP/Setup+Small+Integration+Test+Environment+for+AAS

e.g. after registration, run:

```shell
java -jar ./eric-oss-avro-producer-1.166.0-SNAPSHOT.jar --file=../pm-records/AMF_Mobility_NetworkSlice_1.avro --file=../pm-records/records.avro --schemaRegistry=http://localhost:8080 --kafka=localhost:9092

# Or

mvn spring-boot:run -Dspring-boot.run.arguments="--file=../pm-records/AMF_Mobility_NetworkSlice_1.avro --file=../pm-records/records.avro --schemaRegistry=http://localhost:8080 --kafka=localhost:9092"

```
---

## Start AAS application

- The dev folder contains two application properties files: `application-prod.yaml` and `application-tls.yaml`, prepared for use with the aforementioned dependencies.
- Note: `application-tls.yaml` is using port 8089, while `application-prod.yaml` uses port 8083.
- To execute the *CoreApplication*, either use the IDE or run the jar file via the command line. Specify `-Dspring.config.location=./dev/` as Java options and set active profiles. This allows you to choose one of the provided properties to operate AAS.


#### Windows users

Common issues and their solutions during the `docker compose up` step

- `Error: short-name resolution enforced but cannot prompt without a TTY
  exit code: 125`
    - Solution: Prepend the image names in the `docker-compose.yml` file with `docker.io` like this: `image: docker.io/wiremock/wiremock`
- `Error: initializing source docker://wiremock/wiremock:latest: pinging container registry registry-1.docker.io: Get "https://registry-1.docker.io/v2/": dial tcp: lookup registry-1.docker.io: Temporary failure in name resolution
  exit code: 125`
    - Solution: Podman uses a WSL instance that is not able to resolve the DNS servers for any requests. Edit the resolve.conf and wsl.conf files as
      mentioned
      here: https://confluence-oss.seli.wh.rnd.internal.ericsson.com/pages/viewpage.action?spaceKey=IDUN&title=Environment+set-up+for+microservice+development+using+WSL2+and+Docker+Desktop
        - ```
          # NOTE: This will delete existing config
          sudo rm -rf /etc/resolv.conf && echo 'nameserver 193.181.14.10
          nameserver 193.181.14.11
          nameserver 8.8.8.8' | sudo tee /etc/resolv.conf
          ```
        - ```
          # NOTE: This will delete existing config
          sudo rm -rf /etc/wsl.conf && echo '[network]
          generateResolvConf = false' | sudo tee /etc/wsl.conf
          ```



## Install and import postman collection

### Install [Postman](https://www.postman.com/downloads/)

Install postman using brew if you are using MacOS

```
brew install --cask postman

```

### Import postman collection

#### Generic instructions on how to import postman collection

[Importing and exporting data](https://learning.postman.com/docs/getting-started/importing-and-exporting-data/)

#### Import Wiremock collection for Data Catalog, Schema Registry and PMSC

1. Open Postman
2. Click _File_ -> _Import_ -> _Upload Files_, then choose the collection file: **_WiremockTests.postman_collection.json_**

```
-rw-r--r--  1 <USERNAME>  staff   1602 10 Nov 16:19 README.md
-rw-r--r--@ 1 <USERNAME>  staff  10830 10 Nov 16:18 WiremockTests.postman_collection.json
-rw-r--r--  1 <USERNAME>  staff    741 28 Oct 17:28 docker-compose.yml
drwxr-xr-x  5 <USERNAME>  staff    160 10 Nov 15:56 mocker

```

3. Locate folder: _WiremockTests_
4. Select the query and execute it

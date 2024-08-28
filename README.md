# Assurance Augmentation Service (AAS)

The Assurance Augmentation Service (AAS) augments performance metric (PM) data with context information not available in source PM data provided by the parser.

## Contact Information

#### Team Members

##### AAS

[Team Swordform](https://confluence-oss.seli.wh.rnd.internal.ericsson.com/display/IDUN/Team+Swordform) is currently the acting development team working on AAS. For support, please contact Team Swordform <a href="mailto:PDLPDLSWOR@pdl.internal.ericsson.com">PDLPDLSWOR@pdl.internal.ericsson.com</a>.

##### CI Pipeline

The CI Pipeline aspect of the Microservice Chassis is now owned, developed and maintained by [Team Hummingbirds](https://confluence-oss.seli.wh.rnd.internal.ericsson.com/display/ACE/Hummingbirds+Home) in the DE (Development Environment) department of PDU OSS.

AAS is a hybrid project, meaning that all build-related configuration is maintained centrally by Team Hummingbirds.

#### Email

Guardians for this project can be reached at Team Swordform <a href="mailto:PDLPDLSWOR@pdl.internal.ericsson.com">PDLPDLSWOR@pdl.internal.ericsson.com</a>.

## Maven Dependencies

The chassis has the following Maven dependencies:

- Spring Boot Start Parent version 2.5.2.
- Spring Boot Starter Web.
- Spring Boot Actuator.
- Spring Cloud Sleuth.
- Spring Boot Started Test.
- JaCoCo Code Coverage Plugin.
- Sonar Maven Plugin.
- Spotify Dockerfile Maven Plugin.
- Common Logging utility for logback created by Vortex team.
- Properties for spring cloud version and java are as follows.

```
<version.spring-cloud>2020.0.3</version.spring-cloud>
```

## Build And Run AAS Locally

### Local set up instructions

1. Follow the instructions listed on https://confluence-oss.seli.wh.rnd.internal.ericsson.com/pages/viewpage.action?pageId=539700183 to set up your local development environment.
2. Ensure that Java 11 is installed.

### Run AAS via Command-line

1. In your terminal, switch to the root directory of where you cloned AAS, e.g. `~/gerrit/eric-oss-assurance-augmentation`
2. Run `mvn clean install -DskipTests=true` to build AAS.
3. Copy and paste application-prod.yaml from the charts directory into a local config directory, e.g. `cp ./charts/eric-oss-assurance-augmentation/config/application-prod.yaml ~/config/aas`.
4. Run docker-compose file in `./dev` folder.
5. Run `java -jar target/eric-oss-assurance-augmentation-<BUILT-VERSION-NUMBER>-SNAPSHOT.jar --spring.config.additional-location=file:///<YOUR-PATH-TO-THE-CONFIG>/application-prod.yaml --spring.profiles.active=prod` to start AAS. e.g. `java -jar target/eric-oss-assurance-augmentation-1.10.0-SNAPSHOT.jar --spring.config.additional-location=file:///Users/<SIGNUM>/config/aas/application-prod.yaml --spring.profiles.active=prod --logging.config=classpath:logback-plain-text.xml`.

### Run AAS via IntelliJ

1. Import AAS as an existing Maven project into IntelliJ.
2. Click top menu 'Edit Configurations' under 'Run'.
3. Add an 'Application' for 'CoreApplication' then in the right panel under 'Build and run', add `--spring.config.additional-location=file:///<YOUR-PATH-TO-THE-CONFIG>/application-prod.yaml --spring.profiles.active=prod --logging.config=classpath:logback-plain-text.xml` for 'CLI argument to your application'.
4. Click 'Apply' and then 'OK'.
5. Right-click on com.ericsson.oss.air.CoreApplication.java and select 'Run CoreApplication.main()'.

### Run AAS via Eclipse

1. Import AAS as an existing Maven project into Eclipse.
2. Install Lombok in your Eclipse by first navigating to `~/.m2/repository/org/projectlombok/lombok/<VERSION>` in your terminal.
3. Execute `java -jar lombok-<VERSION>.jar`.
4. Follow the instructions on the pop-up window and restart Eclipse.
5. Right-click on com.ericsson.oss.air.CoreApplication.java and select 'Run As' > 'Run Configurations'.
6. In the `Run Configurations` pop-up, switch to the Arguments tab. Input `--spring.config.additional-location=file:///<YOUR-PATH-TO-THE-CONFIG>/application-prod.yaml --spring.profiles.active=prod` into the 'Program Arguments' text box.
7. Click 'Apply' and then 'Run'.

### Local Development Environment Setup

1. Install the IDE of choice. Both Eclipse and IDEA IntelliJ are supported.
1. Follow the instructions listed on https://confluence-oss.seli.wh.rnd.internal.ericsson.com/pages/viewpage.action?pageId=539700183 to set up your local development environment.
1. Ensure that Java 11 and Maven 3.6+ are installed.

#### Running With Local Dependencies

AAS has the following runtime dependencies:

- PostgreSQL
- kafka/zookeeper
- ARDQ

These dependencies can be started from `dev/docker-compose.yaml' as follows:

```bash
$ docker-compose -f dev/docker-compose.yml up -d
```

This will start all dependencies in the background. To stop the dependencies, execute

```bash
$ docker-compose -f dev/docker-compose.yml stop
```

##### PostgreSQL

The PostgreSQL (pgsql) is a default deployment that is accessible on 'localhost:5432'.

##### Kafka

The kafka broker is deployed with default settings and is accessible on 'localhost:29092'

### Building with Bob

#### Bob Ruleset

Bob requires a locally available rule set. As the build files are managed centrally, a copy of the common ruleset must be downloaded to the eric-oss-assurance-augmentation/ci folder in the local environment. The file can be obtained from the build artifacts associated with any jenkins build of AAS here: [AAS PCR Jenkins Build](https://fem1s11-eiffel216.eiffel.gic.ericsson.se:8443/jenkins/view/Hybrid/job/eric-oss-assurance-augmentation_PreCodeReview_Hybrid/).

#### Local Build Instructions

If the developer wishes to manually build the application in the local workstation, the `bob -r ci/common_ruleset2.0.yaml clean init-dev build image package-local` command can be used once BOB is configured in the workstation.
Note: The `mvn clean install` command will be required before running the bob command above.
See the "Containerization and Deployment to Kubernetes cluster" section for more details on deploying the built application.

Stub jar files are necessary to allow contract tests to run. The stub jars are stored in JFrog (Artifactory).
To allow the contract test to access and retrieve the stub jars, the .bob.env file must be configured as follows.

```
SELI_ARTIFACTORY_REPO_USER=<LAN user id>
SELI_ARTIFACTORY_REPO_PASS=<JFrog encripted LAN PWD or API key>
HOME=<path containing .m2, e.g. /c/Users/<user>/>
```

To retrieve an encrypted LAN password or API key, login to [JFrog](https://arm.seli.gic.ericsson.se) and select "Edit Profile".
For info in setting the .bob.env file see [Bob 2.0 User Guide](https://gerrit-gamma.gic.ericsson.se/plugins/gitiles/adp-cicd/bob/+/refs/heads/master/USER_GUIDE_2.0.md).

## Containerization and Deployment to Kubernetes cluster.

Following artifacts contains information related to building a container and enabling deployment to a Kubernetes cluster:

- [charts](charts/) folder - used by BOB to lint, package and upload helm chart to helm repository.
  - Once the project is built in the local workstation using the `bob clean init-dev build image package-local` command, a packaged helm chart is available in the folder `.bob/eric-oss-assurance-augmentation-internal/` folder.
    This chart can be manually installed in Kubernetes using `helm install` command. [P.S. required only for Manual deployment from local workstation]
- [Dockerfile](Dockerfile) - used by Spotify dockerfile maven plugin to build docker image.
  - The base image for the chassis application is `sles-jdk8` available in `armdocker.rnd.ericsson.se`.

### Deploy AAS in local k8s cluster

1. Download and install [Rancher Desktop](https://docs.rancherdesktop.io/getting-started/installation/) application.
2. Make sure you select `containerd` option in `General > Settings > Container Engine` tab.
3. Open a new terminal and set the Kubernetes context to `rancher-desktop`
   ```
   kubectl config use-context rancher-desktop
   ```
4. Create namespace
   ```
   kubectl create ns test
   ```
5. Login to ARM docker registry. Use your ECN/local machine password for login.
   ```
   nerdctl login armdocker.rnd.ericsson.se --username <SIGNUM>
   ```
6. Create a docker-registry secret that will be used for pulling the images.
   ```
   kubectl create secret docker-registry k8s-registry --docker-server=armdocker.rnd.ericsson.se --docker-username=<SIGNUM> --docker-password=<PASSWORD> --docker-email=<XXXXXXX@ericsson.com> --namespace=test
   ```
7. Install ADP document Database(DDB).

   ```shell
   #Add helm repository to pull Document Database chart.
   helm repo add adp https://arm.rnd.ki.sw.ericsson.se/artifactory/proj-adp-gs-all-helm --username <SIGNUM>
   helm repo update

   #Create postgresql user credential secret.
   kubectl create secret generic eric-oss-assurance-augmentation-db-secret --from-literal=pguserid=aas --from-literal=pgpasswd=custompwd --from-literal=super-pwd=superpwd --from-literal=super-user=postgres --from-literal=metrics-pwd=metricspwd --from-literal=replica-user=replicauser --from-literal=replica-pwd=replicapwd --namespace=test

   #Install Document Database using the latest release version.
   helm install aas-ddb adp/eric-data-document-database-pg --version 8.8.0+31 --set global.pullSecret=k8s-registry,global.security.tls.enabled=false,credentials.kubernetesSecretName=eric-oss-assurance-augmentation-db-secret,postgresDatabase=aasdb,nameOverride=eric-oss-assurance-augmentation-db,credentials.keyForUserId=pguserid,credentials.keyForUserPw=pgpasswd --namespace=test

   # DDB can be installed with the Backup and Restore Orchestrator (BRO) enabled. This requires setting the `brAgent.enabled` parameter to `true` (the default is `false`). Below are examples of installing DDB with BRO enabled.
   # option 1. To install application level BRO that only our agents can connect to (own BRO)
   helm install <RELEASE_NAME> adp/eric-data-document-database-pg --version <DDB_VERSION> --namespace <DDB_NAMESPACE> --set global.pullSecret=k8s-registry,global.security.tls.enabled=false,credentials.kubernetesSecretName=eric-oss-assurance-augmentation-db-secret,postgresDatabase=aasdb,nameOverride=eric-oss-assurance-augmentation-db,credentials.keyForUserId=pguserid,credentials.keyForUserPw=pgpasswd,brAgent.enabled=true,security.tls.brAgent.enabled=false,global.adpBR.brLabelKey=<GLOBAL-BR-LABEL-KEY>,brAgent.brLabelValue=<BR_AGENT_LABEL>
   
   # option 2. To install application level BRO that only our agents can connect to (own BRO)
   helm install <RELEASE_NAME> adp/eric-data-document-database-pg --version <DDB_VERSION> --namespace <DDB_NAMESPACE> --set global.pullSecret=k8s-registry,global.security.tls.enabled=false,credentials.kubernetesSecretName=eric-oss-assurance-augmentation-db-secret,postgresDatabase=aasdb,nameOverride=eric-oss-assurance-augmentation-db,credentials.keyForUserId=pguserid,credentials.keyForUserPw=pgpasswd,brAgent.enabled=true,security.tls.brAgent.enabled=false

   ```
   For information related to DDB installation,
   see [Document Database PG Service User Guide.](https://adp.ericsson.se/marketplace/document-database-pg/documentation/8.8.0/dpi/service-user-guide)

   For information related to Backup and Restore Orchestrator (BRO) installation
   see [Backup and Restore Orchestrator Service Deployment Guide](https://adp.ericsson.se/marketplace/backup-and-restore-orchestrator/documentation/development/dpi/service-user-guide#deployment)

   > If you want to install the latest development versions of AAS, please run the following command. Alternatively, you can follow steps 8 to 10 to build AAS using the bob command provided below.

   ```shell
   helm repo add drop https://arm.seli.gic.ericsson.se/artifactory/proj-eric-oss-drop-helm --username <SIGNUM>
   helm repo update
   helm install aas drop/eric-oss-assurance-augmentation --devel --set appArmorProfile.type=unconfined,log.streamingMethod=indirect,global.pullSecret=k8s-registry --set global.security.tls.enabled=false --namespace=test
   ```

8. Build the AAS project in bob vm.

   ```
   #Download or copy common_ruleset2.0.yaml from Jenkins. E.g.
   [common_ruleset2.0.yaml](https://fem1s11-eiffel216.eiffel.gic.ericsson.se:8443/jenkins/job/eric-oss-assurance-augmentation_PreCodeReview_Hybrid/802/execution/node/3/ws/ci/common_ruleset2.0.yaml)

   #Save the downloaded common_ruleset2.0.yaml under ci directory.
   cd ~/gerrit/eric-oss-assurance-augmentation/ci
   ls common_ruleset2.0.yaml

   #Bob build.
   cd ~/gerrit/eric-oss-assurance-augmentation
   bob -r ci/common_ruleset2.0.yaml clean init-dev build image package-local
   ```

9. Copy the AAS docker image built from bob vm to local environment.

   ```
   #Save docker image as tar file. This command needs to be run inside bob vm.
   docker save armdocker.rnd.ericsson.se/proj-eric-oss-dev/eric-oss-assurance-augmentation > aas-image.tar

   #Load the saved docker image to local environment. This command needs to be run outside bob vm(in local env).
   nerdctl --namespace=k8s.io load --input aas-image.tar
   ```

10. Install AAS.

    ```
    # Untar the packaged helm chart in your project's .bob/eric-oss-assurance-augmentation-internal/ folder.
    tar -xvf eric-oss-assurance-augmentation.tgz
    cd eric-oss-assurance-augmentation

    #Helm install with default logging
    helm install aas --set appArmorProfile.type=unconfined,global.pullSecret=k8s-registry -n test .

    # Helm install with indirect (stdout) logging
    helm install aas --set appArmorProfile.type=unconfined,log.streamingMethod=indirect,global.pullSecret=k8s-registry -n test .
    ```

## Source

The [src](src/) folder of the java project contains a core spring boot application, a controller for health check and an interceptor for helping with logging details like user name.
The folder also contains corresponding java unit tests.

```
src
├── main
│   ├── java
│   │   ├── com
│   │   │ └── ericsson
│   │   │     └── oss
│   │   │         └── air
│   │   │         ├── controller
│   │   │         │   ├── package-info.java
│   │   │         │   └── health
│   │   │         │       ├── HealthCheck.java
│   │   │         │       └── package-info.java
│   │   │         ├── CoreApplication.java
│   │   │         └── package-info.java
│   │   └── META-INF
│   │       └── MANIFEST.MF
│   └── resources
│       ├── jmx
│       │   ├── jmxremote.access
│       │   └── jmxremote.password
│       ├── v1
│       │   ├── index.html
│       │   └── eric-oss-assurance-augmentation-openapi.yaml
│       ├── application.yaml
│       ├── logback-json.xml
│       └── bootstrap.yml
└── test
    └── java
        └── com
            └── ericsson
                └── oss
                    └── air
                        ├── aas
                        │   └── api
                        │       └── contract
                        │           └── ArdqRegistrationApiBase.java
                        ├── controller
                        │   └── health
                        │       ├── HealthCheckTest.java
                        │       └── package-info.java
                        ├── CoreApplicationTest.java
                        └── package-info.java
```

### To enable debug logs

- During kubernetes deployment:

  - Run the following command for an active pod:

    `kubectl get configmaps eric-oss-assurance-augmentation-log-config -n test -o yaml | sed -E 's/severity": .*/severity": "debug"/' | kubectl replace -f -`

- During local developer testing:

  - Set `<root level="DEBUG">` in the specific logback file of your choice. Example for the logback file can be found in this project's src/main/resources/ folder.

  Note: Setting <root level="DEBUG"> produce a lot of debug-level logs. Unless you are troubleshooting a problem with the application context start up (bean instantiation) it is not recommended to set <root level="DEBUG">.
  To produce specific debug logs, pick a specific logger and set its log level to debug in logback file. Example `<logger name="com.ericsson" level="debug" />`

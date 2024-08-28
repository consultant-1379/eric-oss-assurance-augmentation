# Development Helm Charts

## sipTlsChart

This chart contains all the services needed to install SIP-TLS.

## wiremock

This chart allows developers to deploy our wiremock server in a k8s cluster. If global.security.tls.enabled is set to true, then the mocked REST APIs will be simultaneously available over https at the port 8443 for mTLS and over http at the port 8080.

This chart is a subchart of testChart.

## testChart

This chart contains AAS with all of its dependencies excluding postgres. Postgres is excluded because it should always be deployed with global.security.tls.enabled set to false. global.security.tls.enabled needs to be true if this chart is deployed for testing SIP-TLS support.

## Script

The script `install_with_mtls.sh` is available for installing AAS with mTLS enabled. This script also installs SIP-TLS charts and Postgres.

Before running this script, it's necessary to set the environment variables.

```shell

export SIGNUM=signum
export S_PASSWORD=password
export WORK_EMAIL=email@ericsson.com

```

Run
```shell

./install_with_mtls.sh <NAMESPACE>

```

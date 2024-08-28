#!/bin/bash
set -Eeuo pipefail

namespace="$1"

echo "kubectl create namespace $namespace"
kubectl create namespace "$namespace"

echo -e "\nkubectl create secret docker-registry k8s-registry --namespace $namespace --docker-server=armdocker.rnd.ericsson.se --docker-username=${SIGNUM} --docker-password=${S_PASSWORD} --docker-email=${WORK_EMAIL}"
kubectl create secret docker-registry k8s-registry --namespace "$namespace" --docker-server=armdocker.rnd.ericsson.se --docker-username=${SIGNUM} --docker-password=${S_PASSWORD} --docker-email=${WORK_EMAIL}


echo -e "\nkubectl create secret generic eric-oss-assurance-augmentation-db-secret --namespace $namespace --from-literal=pguserid=aas --from-literal=pgpasswd=custompwd --from-literal=super-pwd=superpwd --from-literal=super-user=postgres --from-literal=metrics-pwd=metricspwd --from-literal=replica-user=replicauser --from-literal=replica-pwd=replicapwd"
kubectl create secret generic eric-oss-assurance-augmentation-db-secret --namespace "$namespace" --from-literal=pguserid=aas --from-literal=pgpasswd=custompwd --from-literal=super-pwd=superpwd --from-literal=super-user=postgres --from-literal=metrics-pwd=metricspwd --from-literal=replica-user=replicauser --from-literal=replica-pwd=replicapwd

echo -e "\nhelm install aas-ddb adp/eric-data-document-database-pg --namespace $namespace --version 8.8.0+31 --set global.pullSecret=k8s-registry,global.security.tls.enabled=false,credentials.kubernetesSecretName=eric-oss-assurance-augmentation-db-secret,postgresDatabase=aasdb,nameOverride=eric-oss-assurance-augmentation-db,credentials.keyForUserId=pguserid,credentials.keyForUserPw=pgpasswd"
helm install aas-ddb adp/eric-data-document-database-pg --namespace "$namespace" --version 8.8.0+31 --set global.pullSecret=k8s-registry,global.security.tls.enabled=false,credentials.kubernetesSecretName=eric-oss-assurance-augmentation-db-secret,postgresDatabase=aasdb,nameOverride=eric-oss-assurance-augmentation-db,credentials.keyForUserId=pguserid,credentials.keyForUserPw=pgpasswd

echo -e "\nhelm install --namespace $namespace sip-tls sipTlsChart"
helm install --namespace "$namespace" sip-tls sipTlsChart

echo -e "\nhelm install small --namespace $namespace --set global.security.tls.enabled=true testChart"
helm install --namespace "$namespace" --set global.security.tls.enabled=true testchart testChart


echo -e "\nWait 600s before enable AAS"
sleep 600
kubectl scale --namespace "$namespace" deployment eric-oss-assurance-augmentation --replicas=1

echo -e "\nNS: $namespace"
echo "Done :)"

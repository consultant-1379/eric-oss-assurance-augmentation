#!/bin/sh

MICROCBO_IMAGE_NAME=${MICROCBO_IMAGE_NAME:-"armdocker.rnd.ericsson.se/proj-ldc/common_base_os_micro_release/sles"}
MICROCBO_IMAGE="${MICROCBO_IMAGE_NAME}:${CBO_VERSION}"
echo "MICROCBO_IMAGE: $MICROCBO_IMAGE"

container=$(buildah from "${MICROCBO_IMAGE}")
echo "container path: $container"

rootdir=$(buildah mount "${container}")
echo "rootdir path: $rootdir"

application_test_file=${rootdir}/opt/application/springboot/scripts/application-test.yaml

java ${JAVA_OPTS} -jar "$1" --spring.config.additional-location=file:${application_test_file} --spring.profiles.active=test
#
# COPYRIGHT Ericsson 2022
#
#
#
# The copyright to the computer program(s) herein is the property of
#
# Ericsson Inc. The programs may be used and/or copied only with written
#
# permission from Ericsson Inc. or in accordance with the terms and
#
# conditions stipulated in the agreement/contract under which the
#
# program(s) have been supplied.
#

#!/bin/sh

exec java ${JAVA_OPTS} -Dcom.sun.management.jmxremote=true \
    -Dcom.sun.management.jmxremote.port=1099 -Dcom.sun.management.jmxremote.authenticate=true \
    -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.rmi.port=1099 \
    -Dcom.sun.management.jmxremote.password.file=/jmx/jmxremote.password -Dcom.sun.management.jmxremote.access.file=/jmx/jmxremote.access \
    -jar eric-oss-assurance-augmentation-app.jar  \
    --spring.config.additional-location=file:///config/application-prod.yaml --spring.profiles.active=prod
{{/*----------------------------------- DB functions ----------------------------------*/}}

{{/*
Define the createSchema, the global.createSchema variable can have a value of true, false, or recreate.
The createSchema value will only be true if global.createSchema is also true.
*/}}
{{- define "eric-oss-assurance-augmentation.createSchema" -}}
{{- $g := fromJson (include "eric-oss-assurance-augmentation.global" .) -}}
{{- eq ( $g.createSchema | toString) "true"  -}}
{{- end -}}

{{/*
Define the database URL. If 'jdbcUrl' is not provided, build the database URL using the vendor, host, port, and dbName.
- We currently only support postgresql as vendor. This hardcoded value will persist until we include the EDB driver.
*/}}
{{- define "eric-oss-assurance-augmentation.databaseUrl" -}}
  {{- if .Values.database.jdbcUrl -}}
    "{{ index .Values "database" "jdbcUrl" }}{{ include "eric-oss-assurance-augmentation.db.parameters" . }}"
  {{- else -}}
    "jdbc:postgresql://{{ index .Values "database" "host" }}:{{ index .Values "database" "port" }}/{{ index .Values "database" "dbName" }}{{ include "eric-oss-assurance-augmentation.db.parameters" . }}"
  {{- end -}}
{{- end -}}

{{/*
Prepare any postgres parameters for the jdbc url

The optional helm values for this template are:
1.  .Values.database.jdbcUrl - jdbc url specified by the user, no default value
2.  .Values.database.generic - a map of key values pairs which will be added to the jdbc url as parameters
3.  .Values.database.sslMode - this determines what the sslMode is disable, require, verify-ca, verify-full. Defaults to "disable"
4.  .Values.database.serverCertSecret - the name of the secret containing the server cert, defaults to "edb-server-cert"
5.  .Values.database.rootCertPath - the path to the server cert within the secret, defaults to "root.crt"
6.  .Values.database.clientCertSecret - the name of the secret containing the client cert and key, defaults to "aug-edb-client-cert"
7.  .Values.database.clientCertKey - the key of the client key data item in the secret, defaults to "tls.key"
8.  .Values.database.clientKeyPath - the relative path to the client key in pod, defaults to "tls.key"
9.  .Values.database.clientCertRoot - the key of the client cert data item in the secret, defaults to "tls.crt"
10. .Values.database.clientCertPath - the relative path to the client cert in pod, defaults to "tls.crt"
11. .Values.database.connectionTimeout - the value for the connectTimeout parameter of the jdbc url

*/}}
{{- define "eric-oss-assurance-augmentation.db.parameters" -}}
  {{- $jdbcUrl := default "" .Values.database.jdbcUrl -}}
  {{- $start := ternary "&" "?" (contains "?" $jdbcUrl) -}}

  {{- print $start -}}

  {{- if .Values.database.connectionTimeout -}}
    {{- printf "connectTimeout=%s&" ( toString .Values.database.connectionTimeout ) -}}
  {{- end -}}

  {{- if ( include "eric-oss-assurance-augmentation.db.ssl-enabled" . ) -}}
    {{- $sslMode := ternary "" ( printf "&sslmode=%s" (include "eric-oss-assurance-augmentation.db.ssl-enabled" . )) (empty (include "eric-oss-assurance-augmentation.db.ssl-enabled" .) ) -}}
    {{- $mode := printf "ssl=true%s" $sslMode -}}
    {{- $sslrootcert := printf "&sslrootcert=%s%s" (include "eric-oss-assurance-augmentation.db._value-path-to-server-cert" .) (include "eric-oss-assurance-augmentation.db._value-relative-path-to-ca-crt" .) -}}

    {{- printf "%s%s" $mode $sslrootcert -}}

    {{- if eq "true" ( include "eric-oss-assurance-augmentation.db.is-mtls" . ) -}}
      {{- $secret := lookup "v1" "Secret" .Release.Namespace ( include "eric-oss-assurance-augmentation.db._value-client-cert-secret-name" . ) -}}

      {{- $clientCertPresent := not (empty (get $secret.data ( include "eric-oss-assurance-augmentation.db._value-client-cert-secret-item-cert-key" . ))) -}}
      {{- $clientCert := ternary (printf "&sslcert=%s%s" (include "eric-oss-assurance-augmentation.db._value-path-to-client-cert" . ) ( include "eric-oss-assurance-augmentation.db._value-relative-path-to-client-cert" . )) "" $clientCertPresent -}}

      {{- $clientKeyPresent := not (empty (get $secret.data ( include "eric-oss-assurance-augmentation.db._value-client-cert-secret-item-key-key" . ))) -}}
      {{- $clientKey := ternary (printf "&sslkey=%s%s" (include "eric-oss-assurance-augmentation.db._value-path-to-client-cert" . ) ( include "eric-oss-assurance-augmentation.db._value-relative-path-to-client-key" .)) "" $clientKeyPresent -}}

      {{- printf "%s%s" $clientCert $clientKey -}}
    {{- end -}}
  {{- end -}}
{{- end -}}



{{/*
Determine if the communication is going to be mTLS
The presence/absence of the secret containing the client certificate will determine this.

*/}}
{{- define "eric-oss-assurance-augmentation.db.is-mtls" -}}
{{- $secret := lookup "v1" "Secret" .Release.Namespace ( include "eric-oss-assurance-augmentation.db._value-client-cert-secret-name" . ) -}}
{{- not (empty (get $secret "data")) -}}
{{- end -}}

{{/*
Determine if SSL communication should be enabled or not.
If enabled the sslmode is returned from this helper function.

The If check in helpers follows the truthy concept
- Empty string is false.
- Any other string is true.

The optional helm values for this template:
1  .Values.database.sslMode - this determines what the sslMode is disable, require, verify-ca, verify-full. Defaults to "disable"
*/}}

{{- define "eric-oss-assurance-augmentation.db.ssl-enabled" -}}
{{- if .Values.database.sslMode -}}
  {{- if (or (contains "disable" (lower .Values.database.sslMode)) (empty .Values.database.sslMode)) -}}
    {{- printf "" -}}
  {{- else -}}
    {{- $sslMode := lower .Values.database.sslMode -}}
    {{- if (has $sslMode (tuple "require" "verify-ca" "verify-full")) -}}
      {{- $sslMode -}}
    {{- else -}}
      {{- fail (printf "%s is not a valid sslMode" $sslMode) -}}
    {{- end -}}
  {{- end -}}
{{- else -}}
  {{- print "disable" -}}
{{- end -}}
{{- end -}}


{{/*
Define the volume for the server certificate data from the secret.
If ssl is enabled a volume containing the server certificate is required.

The optional helm values for this template are:
1  .Values.database.sslMode - this determines what the sslMode is disable, require, verify-ca, verify-full. Defaults to "disable"
2. .Values.database.serverCertSecret - the name of the secret containing the server cert, defaults to "edb-server-cert"
3. .Values.database.rootCertPath - the path is the relative path of the file to map the key to, defaults to "root.crt"
4. .Values.database.serverCertKey - the item name in the secret, defaults to "ca.crt"

*/}}
{{- define "eric-oss-assurance-augmentation.db.server-cert-volume" -}}
{{- if ( include "eric-oss-assurance-augmentation.db.ssl-enabled" . ) -}}
- name: db-server-cert-volume
  secret:
    items:
      - key: {{ include "eric-oss-assurance-augmentation.db._value-server-cert-secret-item-cert-key" . }}
        path: {{ (include "eric-oss-assurance-augmentation.db._value-relative-path-to-ca-crt" .) }}
    secretName: {{ include "eric-oss-assurance-augmentation.db._value-server-cert-secret-name" . }}
{{- end -}}
{{- end -}}


{{/*
Define the volume for the client certificate data from the secret.

The optional helm values for this template are:
1. .Values.database.clientCertKey - the key of the client key data item in the secret, defaults to "tls.key"
2. .Values.database.clientKeyPath - the path to the client key in pod, defaults to "tls.key"
3. .Values.database.clientCertRoot - the key of the client cert data item in the secret, defaults to "tls.crt"
4. .Values.database.clientCertSecret - the name of the secret containing the client cert and key, defaults to "aug-edb-client-cert"
5. .Values.database.clientCertPath - the path to the client cert in pod, defaults to "tls.crt"

*/}}
{{- define "eric-oss-assurance-augmentation.db.client-cert-volume" -}}
{{- if and ( include "eric-oss-assurance-augmentation.db.ssl-enabled" . ) (eq "true" ( include "eric-oss-assurance-augmentation.db.is-mtls" . )) -}}
- name: db-client-cert-volume
  secret:
    items:
    - key: {{ include "eric-oss-assurance-augmentation.db._value-client-cert-secret-item-cert-key" . }}
      path: {{ include "eric-oss-assurance-augmentation.db._value-relative-path-to-client-cert" . }}
    - key: {{ include "eric-oss-assurance-augmentation.db._value-client-cert-secret-item-key-key" . }}
      path: {{ include "eric-oss-assurance-augmentation.db._value-relative-path-to-client-key" . }}
    secretName: {{ include "eric-oss-assurance-augmentation.db._value-client-cert-secret-name" . }}
{{- end -}}
{{- end -}}

{{/*
Define the volume mount for the server certificate

*/}}
{{- define "eric-oss-assurance-augmentation.db.server-cert-volume-mount" -}}
{{- if ( include "eric-oss-assurance-augmentation.db.ssl-enabled" . ) -}}
- name: db-server-cert-volume
  mountPath: {{ include "eric-oss-assurance-augmentation.db._value-path-to-server-cert" . }}
{{- end -}}
{{- end -}}


{{/*
Define the volume mount for the client certificate

The optional helm values for this template:
1. .Values.database.clientCertSecret - the name of the secret containing the client cert and key, defaults to "aug-edb-client-cert"

*/}}
{{- define "eric-oss-assurance-augmentation.db.client-cert-volume-mount" -}}
{{- if and ( include "eric-oss-assurance-augmentation.db.ssl-enabled" . ) (eq "true" ( include "eric-oss-assurance-augmentation.db.is-mtls" . )) -}}
- name: db-client-cert-volume
  mountPath: {{ include "eric-oss-assurance-augmentation.db._value-path-to-client-cert" . }}
{{- end -}}
{{- end -}}


{{/*
Get the value for the server cert secret cert item key
This is an optional value with a default
*/}}
{{- define "eric-oss-assurance-augmentation.db._value-server-cert-secret-item-cert-key" -}}
{{- $serverCertKey := "ca.crt" -}}
{{- if .Values.database -}}
  {{- if .Values.database.serverCertKey -}}
    {{- $serverCertKey = .Values.database.serverCertKey -}}
  {{- end -}}
{{- end -}}
{{- printf "%s" $serverCertKey -}}
{{- end -}}

{{/*
Get the value for the client cert secret cert item key
This is an optional value with a default
*/}}
{{- define "eric-oss-assurance-augmentation.db._value-client-cert-secret-item-cert-key" -}}
{{- $clientCertKey := "tls.crt" -}}
{{- if .Values.database -}}
  {{- if .Values.database.clientCertRoot -}}
    {{- $clientCertKey = .Values.database.clientCertRoot -}}
  {{- end -}}
{{- end -}}
{{- printf "%s" $clientCertKey -}}
{{- end -}}

{{/*
Get the value for the client cert secret key item key
This is an optional value with a default
*/}}
{{- define "eric-oss-assurance-augmentation.db._value-client-cert-secret-item-key-key" -}}
{{- $clientKeyKey := "tls.key" -}}
{{- if .Values.database -}}
  {{- if .Values.database.clientCertKey -}}
    {{- $clientKeyKey = .Values.database.clientCertKey -}}
  {{- end -}}
{{- end -}}
{{- printf "%s" $clientKeyKey -}}
{{- end -}}


{{/*
Get the value for the name of the server certificate secret
This is an optional value with a default
*/}}
{{- define "eric-oss-assurance-augmentation.db._value-server-cert-secret-name" -}}
{{- $serverCertSecretName := "edb-server-cert" -}}
{{- if .Values.database -}}
  {{- if .Values.database.serverCertSecret -}}
    {{- if (kindIs "string" .Values.database.serverCertSecret) -}}
      {{- $serverCertSecretName = .Values.database.serverCertSecret -}}
    {{- end -}}
  {{- end -}}
{{- end -}}
{{- printf "%s" $serverCertSecretName -}}
{{- end -}}

{{/*
Get the value for the name of the client certificate secret
This is an optional value with a default
*/}}
{{- define "eric-oss-assurance-augmentation.db._value-client-cert-secret-name" -}}
{{- default (printf "aug-edb-client-cert") .Values.database.clientCertSecret -}}
{{- end -}}


{{/*
Path to the server cert.
*/}}
{{- define "eric-oss-assurance-augmentation.db._value-path-to-server-cert" -}}
  {{- print "/run/secrets/db/server/" -}}
{{- end -}}

{{/*
Path to the client cert and key
*/}}
{{- define "eric-oss-assurance-augmentation.db._value-path-to-client-cert" -}}
  {{- print "/run/secrets/db/client/" -}}
{{- end -}}


{{/*
Get the value for the relative path to the CA root crt in the pod filesystem
This is an optional value with a default
*/}}
{{- define "eric-oss-assurance-augmentation.db._value-relative-path-to-ca-crt" -}}
{{- default "root.crt" .Values.database.rootCertPath -}}
{{- end -}}

{{/*
Get the value for the relative path to the Client certificate in the secret
This is an optional value with a default
*/}}
{{- define "eric-oss-assurance-augmentation.db._value-relative-path-to-client-cert" -}}
{{- default "tls.crt" .Values.database.clientCertPath -}}
{{- end -}}

{{/*
Get the value for the relative path to the Client Key in the secret
This is an optional value with a default
*/}}
{{- define "eric-oss-assurance-augmentation.db._value-relative-path-to-client-key" -}}
{{- default "tls-key.pk8" .Values.database.clientKeyPath -}}
{{- end -}}

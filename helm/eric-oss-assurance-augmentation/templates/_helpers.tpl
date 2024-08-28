{{/* vim: set filetype=mustache: */}}
{{/*
Expand the name of the chart.
*/}}
{{- define "eric-oss-assurance-augmentation.name" }}
  {{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Create chart version as used by the chart label.
*/}}
{{- define "eric-oss-assurance-augmentation.version" }}
{{- printf "%s" .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Expand the name of the chart.
*/}}
{{- define "eric-oss-assurance-augmentation.fullname" -}}
{{- if .Values.fullnameOverride -}}
  {{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" -}}
{{- else -}}
  {{- $name := default .Chart.Name .Values.nameOverride -}}
  {{- printf "%s" $name | trunc 63 | trimSuffix "-" -}}
{{- end -}}
{{- end -}}

{{/*
Create chart name and version as used by the chart label.
*/}}
{{- define "eric-oss-assurance-augmentation.chart" }}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Create a map from ".Values.global" with defaults if missing in values file.
This hides defaults from values file.
*/}}
{{ define "eric-oss-assurance-augmentation.global" }}
  {{- $globalDefaults := dict "security" (dict "tls" (dict "enabled" true)) -}}
  {{- $globalDefaults := merge $globalDefaults (dict "security" (dict "tls" (dict "trustedInternalRootCa" (dict "secret" "eric-sec-sip-tls-trusted-root-cert")))) -}}
  {{- $globalDefaults := merge $globalDefaults (dict "nodeSelector" (dict)) -}}
  {{- $globalDefaults := merge $globalDefaults (dict "registry" (dict "pullSecret" "eric-oss-assurance-augmentation-secret")) -}}
  {{- $globalDefaults := merge $globalDefaults (dict "externalIPv4" (dict "enabled")) -}}
  {{- $globalDefaults := merge $globalDefaults (dict "externalIPv6" (dict "enabled")) -}}
  {{- $globalDefaults := merge $globalDefaults (dict "createSchema" true) -}}
  {{ if .Values.global }}
    {{- mergeOverwrite $globalDefaults .Values.global | toJson -}}
  {{ else }}
    {{- $globalDefaults | toJson -}}
  {{ end }}
{{ end }}

{{/*
Create image pull secrets for global (outside of scope)
*/}}
{{- define "eric-oss-assurance-augmentation.pullSecret.global" -}}
{{- $pullSecret := "" -}}
{{- if .Values.global -}}
  {{- if .Values.global.pullSecret -}}
    {{- $pullSecret = .Values.global.pullSecret -}}
  {{- end -}}
  {{- end -}}
{{- print $pullSecret -}}
{{- end -}}

{{/*
Create image pull secret, service level parameter takes precedence
*/}}
{{- define "eric-oss-assurance-augmentation.pullSecret" -}}
{{- $pullSecret := (include "eric-oss-assurance-augmentation.pullSecret.global" . ) -}}
{{- if .Values.imageCredentials -}}
  {{- if .Values.imageCredentials.pullSecret -}}
    {{- $pullSecret = .Values.imageCredentials.pullSecret -}}
  {{- end -}}
{{- end -}}
{{- print $pullSecret -}}
{{- end -}}

{{- define "eric-oss-assurance-augmentation.mainImagePath" -}}
    {{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
    {{- $registryUrl := (index $productInfo "images" "eric-oss-assurance-augmentation" "registry") -}}
    {{- $repoPath := (index $productInfo "images" "eric-oss-assurance-augmentation" "repoPath") -}}
    {{- $name := (index $productInfo "images" "eric-oss-assurance-augmentation" "name") -}}
    {{- $tag := (index $productInfo "images" "eric-oss-assurance-augmentation" "tag") -}}
    {{- if .Values.global -}}
        {{- if .Values.global.registry -}}
            {{- if .Values.global.registry.url -}}
                {{- $registryUrl = .Values.global.registry.url -}}
            {{- end -}}
            {{- if not (kindIs "invalid" .Values.global.registry.repoPath) -}}
                {{- $repoPath = .Values.global.registry.repoPath -}}
            {{- end -}}
        {{- end -}}
    {{- end -}}
    {{- if .Values.imageCredentials -}}
        {{- if not (kindIs "invalid" .Values.imageCredentials.repoPath) -}}
            {{- $repoPath = .Values.imageCredentials.repoPath -}}
        {{- end -}}
        {{- if (index .Values "imageCredentials" "eric-oss-assurance-augmentation") -}}
            {{- if (index .Values "imageCredentials" "eric-oss-assurance-augmentation" "registry") -}}
                {{- if (index .Values "imageCredentials" "eric-oss-assurance-augmentation" "registry" "url") -}}
                    {{- $registryUrl = (index .Values "imageCredentials" "eric-oss-assurance-augmentation" "registry" "url") -}}
                {{- end -}}
            {{- end -}}
            {{- if not (kindIs "invalid" (index .Values "imageCredentials" "eric-oss-assurance-augmentation" "repoPath")) -}}
                {{- $repoPath = (index .Values "imageCredentials" "eric-oss-assurance-augmentation" "repoPath") -}}
            {{- end -}}
        {{- end -}}
        {{- if not (kindIs "invalid" .Values.imageCredentials.repoPath) -}}
            {{- $repoPath = .Values.imageCredentials.repoPath -}}
        {{- end -}}
    {{- end -}}
    {{- if $repoPath -}}
        {{- $repoPath = printf "%s/" $repoPath -}}
    {{- end -}}
    {{- printf "%s/%s%s:%s" $registryUrl $repoPath $name $tag -}}
{{- end -}}

{{- define "eric-oss-assurance-augmentation.testImagePath" }}
    {{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
    {{- $registryUrl := (index $productInfo "images" "eric-oss-assurance-augmentationTest" "registry") -}}
    {{- $repoPath := (index $productInfo "images" "eric-oss-assurance-augmentationTest" "repoPath") -}}
    {{- $name := (index $productInfo "images" "eric-oss-assurance-augmentationTest" "name") -}}
    {{- $tag := (index $productInfo "images" "eric-oss-assurance-augmentationTest" "tag") -}}
    {{- if .Values.global -}}
        {{- if .Values.global.registry -}}
            {{- if .Values.global.registry.url -}}
                {{- $registryUrl = .Values.global.registry.url -}}
            {{- end -}}
            {{- if not (kindIs "invalid" .Values.global.registry.repoPath) -}}
                {{- $repoPath = .Values.global.registry.repoPath -}}
            {{- end -}}
        {{- end -}}
    {{- end -}}
    {{- if .Values.imageCredentials -}}
        {{- if not (kindIs "invalid" .Values.imageCredentials.repoPath) -}}
            {{- $repoPath = .Values.imageCredentials.repoPath -}}
        {{- end -}}
        {{- if (index .Values "imageCredentials" "eric-oss-assurance-augmentationTest") -}}
            {{- if (index .Values "imageCredentials" "eric-oss-assurance-augmentationTest" "registry") -}}
                {{- if (index .Values "imageCredentials" "eric-oss-assurance-augmentationTest" "registry" "url") -}}
                    {{- $registryUrl = (index .Values "imageCredentials" "eric-oss-assurance-augmentationTest" "registry" "url") -}}
                {{- end -}}
            {{- end -}}
            {{- if not (kindIs "invalid" (index .Values "imageCredentials" "eric-oss-assurance-augmentationTest" "repoPath")) -}}
                {{- $repoPath = (index .Values "imageCredentials" "eric-oss-assurance-augmentationTest" "repoPath") -}}
            {{- end -}}
        {{- end -}}
        {{- if not (kindIs "invalid" .Values.imageCredentials.repoPath) -}}
            {{- $repoPath = .Values.imageCredentials.repoPath -}}
        {{- end -}}
    {{- end -}}
    {{- if $repoPath -}}
        {{- $repoPath = printf "%s/" $repoPath -}}
    {{- end -}}
    {{- printf "%s/%s%s:%s" $registryUrl $repoPath $name $tag -}}
{{- end -}}

{{/*
Timezone variable
*/}}
{{- define "eric-oss-assurance-augmentation.timezone" }}
  {{- $timezone := "UTC" }}
  {{- if .Values.global }}
    {{- if .Values.global.timezone }}
      {{- $timezone = .Values.global.timezone }}
    {{- end }}
  {{- end }}
  {{- print $timezone | quote }}
{{- end -}}

{{/*
Common labels
*/}}
{{- define "eric-oss-assurance-augmentation.common-labels" }}
app.kubernetes.io/name: {{ include "eric-oss-assurance-augmentation.name" . }}
helm.sh/chart: {{ include "eric-oss-assurance-augmentation.chart" . }}
{{ include "eric-oss-assurance-augmentation.selectorLabels" . }}
app.kubernetes.io/version: {{ include "eric-oss-assurance-augmentation.version" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end -}}

{{/*
Create a user defined label (DR-D1121-068, DR-D1121-060)
*/}}
{{ define "eric-oss-assurance-augmentation.config-labels" }}
  {{- $global := (.Values.global).labels -}}
  {{- $service := .Values.labels -}}
  {{- include "eric-oss-assurance-augmentation.mergeLabels" (dict "location" .Template.Name "sources" (list $global $service)) }}
{{- end }}

{{/*
Merged labels for Default, which includes Standard and Config
*/}}
{{- define "eric-oss-assurance-augmentation.labels" -}}
  {{- $common := include "eric-oss-assurance-augmentation.common-labels" . | fromYaml -}}
  {{- $config := include "eric-oss-assurance-augmentation.config-labels" . | fromYaml -}}
  {{- include "eric-oss-assurance-augmentation.mergeLabels" (dict "location" .Template.Name "sources" (list $common $config)) | trim }}
{{- end -}}

{{/*
Defaults labels with Log Streaming Method Label
*/}}
{{- define "eric-oss-assurance-augmentation.labels-and-logStream" -}}
  {{- $labels := include "eric-oss-assurance-augmentation.labels" . | fromYaml -}}
  {{- $logStream := include "eric-oss-assurance-augmentation.directStreamingLabel" . | fromYaml -}}
  {{- include "eric-oss-assurance-augmentation.mergeLabels" (dict "location" .Template.Name "sources" (list $labels $logStream)) }}
{{- end -}}

{{/*
Return the fsgroup set via global parameter if it's set, otherwise 10000
*/}}
{{- define "eric-oss-assurance-augmentation.fsGroup.coordinated" -}}
  {{- if .Values.global -}}
    {{- if .Values.global.fsGroup -}}
      {{- if .Values.global.fsGroup.manual -}}
        {{ .Values.global.fsGroup.manual }}
      {{- else -}}
        {{- if eq .Values.global.fsGroup.namespace true -}}
          # The 'default' defined in the Security Policy will be used.
        {{- else -}}
          10000
      {{- end -}}
    {{- end -}}
  {{- else -}}
    10000
  {{- end -}}
  {{- else -}}
    10000
  {{- end -}}
{{- end -}}

{{/*
Selector labels
*/}}
{{- define "eric-oss-assurance-augmentation.selectorLabels" -}}
app.kubernetes.io/name: {{ include "eric-oss-assurance-augmentation.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}

{{/*
Create the name of the service account to use
*/}}
{{- define "eric-oss-assurance-augmentation.serviceAccountName" -}}
  {{- if .Values.serviceAccount.create }}
    {{- default (include "eric-oss-assurance-augmentation.fullname" .) .Values.serviceAccount.name }}
  {{- else }}
    {{- default "default" .Values.serviceAccount.name }}
  {{- end }}
{{- end }}

{{/*
Create container level annotations
*/}}
{{- define "eric-oss-assurance-augmentation.container-annotations" }}
{{- $appArmorValue := .Values.appArmorProfile.type -}}
    {{- if .Values.appArmorProfile -}}
        {{- if .Values.appArmorProfile.type -}}
            {{- if eq .Values.appArmorProfile.type "localhost" -}}
                {{- $appArmorValue = printf "%s/%s" .Values.appArmorProfile.type .Values.appArmorProfile.localhostProfile }}
            {{- end}}
container.apparmor.security.beta.kubernetes.io/eric-oss-assurance-augmentation: {{ $appArmorValue | quote }}
        {{- end}}
    {{- end}}
{{- end}}

{{/*
Seccomp profile section (DR-1123-128)
*/}}
{{- define "eric-oss-assurance-augmentation.seccomp-profile" }}
    {{- if .Values.seccompProfile }}
      {{- if .Values.seccompProfile.type }}
          {{- if eq .Values.seccompProfile.type "Localhost" }}
              {{- if .Values.seccompProfile.localhostProfile }}
seccompProfile:
  type: {{ .Values.seccompProfile.type }}
  localhostProfile: {{ .Values.seccompProfile.localhostProfile }}
            {{- end }}
          {{- else }}
seccompProfile:
  type: {{ .Values.seccompProfile.type }}
          {{- end }}
        {{- end }}
      {{- end }}
{{- end }}

{{/*
Annotations for Product Name and Product Number (DR-D1121-064).
*/}}
{{- define "eric-oss-assurance-augmentation.product-info" }}
ericsson.com/product-name: {{ (fromYaml (.Files.Get "eric-product-info.yaml")).productName | quote }}
ericsson.com/product-number: {{ (fromYaml (.Files.Get "eric-product-info.yaml")).productNumber | quote }}
ericsson.com/product-revision: {{regexReplaceAll "(.*)[+|-].*" .Chart.Version "${1}" | quote }}
{{- end }}

{{/*
Create a user defined annotation (DR-D1121-065, DR-D1121-060)
*/}}
{{ define "eric-oss-assurance-augmentation.config-annotations" }}
  {{- $global := (.Values.global).annotations -}}
  {{- $service := .Values.annotations -}}
  {{- include "eric-oss-assurance-augmentation.mergeAnnotations" (dict "location" .Template.Name "sources" (list $global $service)) }}
{{- end }}

{{/*
Merged annotations for Default, which includes productInfo and config
*/}}
{{- define "eric-oss-assurance-augmentation.annotations" -}}
  {{- $productInfo := include "eric-oss-assurance-augmentation.product-info" . | fromYaml -}}
  {{- $config := include "eric-oss-assurance-augmentation.config-annotations" . | fromYaml -}}
  {{- include "eric-oss-assurance-augmentation.mergeAnnotations" (dict "location" .Template.Name "sources" (list $productInfo $config)) | trim }}
{{- end -}}

{{/*
Create prometheus info
*/}}
{{- define "eric-oss-assurance-augmentation.prometheus-vars" -}}
prometheus.io/path: {{ .Values.prometheus.path | quote }}
prometheus.io/port: {{ include "eric-oss-assurance-augmentation.service-port" . | quote }}
prometheus.io/scrape: {{ .Values.prometheus.scrape | quote }}
{{- end -}}

{{/*
Merged annotations with Prometheus
*/}}
{{- define "eric-oss-assurance-augmentation.prometheus" -}}
  {{- $prometheus := include "eric-oss-assurance-augmentation.prometheus-vars" . | fromYaml -}}
  {{- $annotations := include "eric-oss-assurance-augmentation.annotations" . | fromYaml -}}
  {{- include "eric-oss-assurance-augmentation.mergeAnnotations" (dict "location" .Template.Name "sources" (list $prometheus $annotations)) | trim }}
{{- end -}}

{{/*
Merged annotations with Prometheus and Container labels
*/}}
{{- define "eric-oss-assurance-augmentation.prometheus-and-container" -}}
  {{- $prometheus := include "eric-oss-assurance-augmentation.prometheus" . | fromYaml -}}
  {{- $container := include "eric-oss-assurance-augmentation.container-annotations" . | fromYaml -}}
  {{- include "eric-oss-assurance-augmentation.mergeAnnotations" (dict "location" .Template.Name "sources" (list $prometheus $container)) | trim }}
{{- end -}}

{{/*
Define the role reference for security policy
*/}}
{{- define "eric-oss-assurance-augmentation.securityPolicy.reference" -}}
  {{- if .Values.global -}}
    {{- if .Values.global.security -}}
      {{- if .Values.global.security.policyReferenceMap -}}
        {{ $mapped := index .Values "global" "security" "policyReferenceMap" "default-restricted-security-policy" }}
        {{- if $mapped -}}
          {{ $mapped }}
        {{- else -}}
          default-restricted-security-policy
        {{- end -}}
      {{- else -}}
        default-restricted-security-policy
      {{- end -}}
    {{- else -}}
      default-restricted-security-policy
    {{- end -}}
  {{- else -}}
    default-restricted-security-policy
  {{- end -}}
{{- end -}}

{{/*
Define the annotations for security policy
*/}}
{{- define "eric-oss-assurance-augmentation.securityPolicy.annotations" -}}
# Automatically generated annotations for documentation purposes.
{{- end -}}

{{/*
Define Pod Disruption Budget value taking into account its type (int or string)
*/}}
{{- define "eric-oss-assurance-augmentation.pod-disruption-budget" -}}
  {{- if kindIs "string" .Values.podDisruptionBudget.minAvailable -}}
    {{- print .Values.podDisruptionBudget.minAvailable | quote -}}
  {{- else -}}
    {{- print .Values.podDisruptionBudget.minAvailable | atoi -}}
  {{- end -}}
{{- end -}}

{{/*
Define upper limit for TerminationGracePeriodSeconds
*/}}
{{- define "eric-oss-assurance-augmentation.terminationGracePeriodSeconds" -}}
{{- if .Values.terminationGracePeriodSeconds -}}
  {{- toYaml .Values.terminationGracePeriodSeconds -}}
{{- end -}}
{{- end -}}

{{/*
Define tolerations to comply to DR-D1120-060
*/}}
{{- define "eric-oss-assurance-augmentation.tolerations" -}}
{{- if .Values.tolerations -}}
  {{- toYaml .Values.tolerations -}}
{{- end -}}
{{- end -}}

{{/*
Create a merged set of nodeSelectors from global and service level.
*/}}
{{ define "eric-oss-assurance-augmentation.nodeSelector" }}
  {{- $g := fromJson (include "eric-oss-assurance-augmentation.global" .) -}}
  {{- $global := $g.nodeSelector -}}
  {{- $service := .Values.nodeSelector -}}
  {{- include "eric-oss-assurance-augmentation.aggregatedMerge" (dict "context" "nodeSelector" "location" .Template.Name "sources" (list $global $service)) }}
{{ end }}

{{/*
    Define Image Pull Policy
*/}}
{{- define "eric-oss-assurance-augmentation.registryImagePullPolicy" -}}
    {{- $globalRegistryPullPolicy := "IfNotPresent" -}}
    {{- if .Values.global -}}
        {{- if .Values.global.registry -}}
            {{- if .Values.global.registry.imagePullPolicy -}}
                {{- $globalRegistryPullPolicy = .Values.global.registry.imagePullPolicy -}}
            {{- end -}}
        {{- end -}}
    {{- end -}}
    {{- print $globalRegistryPullPolicy -}}
{{- end -}}

{/*
Define JVM heap size (DR-D1126-010 | DR-D1126-011)
*/}}
{{- define "eric-oss-assurance-augmentation.jvmHeapSettings" -}}
    {{- $initRAM := "" -}}
    {{- $maxRAM := "" -}}
    {{/*
       ramLimit is set by default to 1.0, this is if the service is set to use anything less than M/Mi
       Rather than trying to cover each type of notation,
       if a user is using anything less than M/Mi then the assumption is its less than the cutoff of 1.3GB
       */}}
    {{- $ramLimit := 1.0 -}}
    {{- $ramComparison := 1.3 -}}

    {{- if not (index .Values "resources" "eric-oss-assurance-augmentation" "limits" "memory") -}}
        {{- fail "memory limit for eric-oss-assurance-augmentation is not specified" -}}
    {{- end -}}

    {{- if (hasSuffix "Gi" (index .Values "resources" "eric-oss-assurance-augmentation" "limits" "memory")) -}}
        {{- $ramLimit = trimSuffix "Gi" (index .Values "resources" "eric-oss-assurance-augmentation" "limits" "memory") | float64 -}}
    {{- else if (hasSuffix "G" (index .Values "resources" "eric-oss-assurance-augmentation" "limits" "memory")) -}}
        {{- $ramLimit = trimSuffix "G" (index .Values "resources" "eric-oss-assurance-augmentation" "limits" "memory") | float64 -}}
    {{- else if (hasSuffix "Mi" (index .Values "resources" "eric-oss-assurance-augmentation" "limits" "memory")) -}}
        {{- $ramLimit = (div (trimSuffix "Mi" (index .Values "resources" "eric-oss-assurance-augmentation" "limits" "memory") | float64) 1000) | float64  -}}
    {{- else if (hasSuffix "M" (index .Values "resources" "eric-oss-assurance-augmentation" "limits" "memory")) -}}
        {{- $ramLimit = (div (trimSuffix "M" (index .Values "resources" "eric-oss-assurance-augmentation" "limits" "memory")| float64) 1000) | float64  -}}
    {{- end -}}


    {{- if (index .Values "resources" "eric-oss-assurance-augmentation" "jvm") -}}
        {{- if (index .Values "resources" "eric-oss-assurance-augmentation" "jvm" "initialMemoryAllocationPercentage") -}}
            {{- $initRAM = (index .Values "resources" "eric-oss-assurance-augmentation" "jvm" "initialMemoryAllocationPercentage") -}}
            {{- $initRAM = printf "-XX:InitialRAMPercentage=%f" $initRAM -}}
        {{- else -}}
            {{- fail "initialMemoryAllocationPercentage not set" -}}
        {{- end -}}
        {{- if and (index .Values "resources" "eric-oss-assurance-augmentation" "jvm" "smallMemoryAllocationMaxPercentage") (index .Values "resources" "eric-oss-assurance-augmentation" "jvm" "largeMemoryAllocationMaxPercentage") -}}
            {{- if lt $ramLimit $ramComparison -}}
                {{- $maxRAM = (index .Values "resources" "eric-oss-assurance-augmentation" "jvm" "smallMemoryAllocationMaxPercentage") -}}
                {{- $maxRAM = printf "-XX:MaxRAMPercentage=%f" $maxRAM -}}
            {{- else -}}
                {{- $maxRAM = (index .Values "resources" "eric-oss-assurance-augmentation" "jvm" "largeMemoryAllocationMaxPercentage") -}}
                {{- $maxRAM = printf "-XX:MaxRAMPercentage=%f" $maxRAM -}}
            {{- end -}}
        {{- else -}}
            {{- fail "smallMemoryAllocationMaxPercentage | largeMemoryAllocationMaxPercentage not set" -}}
        {{- end -}}
    {{- else -}}
        {{- fail "jvm heap percentages are not set" -}}
    {{- end -}}
{{- printf "%s %s" $initRAM $maxRAM -}}
{{- end -}}

{{/*
    Define Service Port
*/}}
{{- define "eric-oss-assurance-augmentation.service-port" -}}
{{- $port := 8080 -}}
{{- $g := fromJson (include "eric-oss-assurance-augmentation.global" .) -}}
{{- if .Values.service.port -}}
    {{- $port = .Values.service.port -}}
{{- else if $g.security.tls.enabled }}
    {{- $port = 8443 -}}
{{- end -}}
{{- print $port -}}
{{- end -}}

{{/*
Define TLS variables
*/}}
{{ define "eric-oss-assurance-augmentation.tlsEnv" }}
- name: ERIC_LOG_TRANSFORMER_KEYSTORE
  value: /tmp/log/keystore.p12
- name: ERIC_LOG_TRANSFORMER_KEYSTORE_PW
  valueFrom:
    secretKeyRef:
      name: {{ .Values.keystore.creds.secret | default "eric-oss-assurance-augmentation-tls-keystore-creds-secret" }}
      key: {{ .Values.keystore.creds.passwdKey }}
- name: ERIC_LOG_TRANSFORMER_TRUSTSTORE
  value: /tmp/rootca/truststore.p12
- name: ERIC_LOG_TRANSFORMER_TRUSTSTORE_PW
  valueFrom:
    secretKeyRef:
      name: {{ .Values.keystore.creds.secret | default "eric-oss-assurance-augmentation-tls-keystore-creds-secret" }}
      key: {{ .Values.keystore.creds.passwdKey }}
- name: TLS_KEYSTORE_KEY_PASSWORD
  valueFrom:
    secretKeyRef:
      name: {{ .Values.keystore.creds.secret | default "eric-oss-assurance-augmentation-tls-keystore-creds-secret" }}
      key: {{ .Values.keystore.creds.passwdKey }}
- name: TLS_KEYSTORE_PASSWORD
  valueFrom:
    secretKeyRef:
      name: {{ .Values.keystore.creds.secret | default "eric-oss-assurance-augmentation-tls-keystore-creds-secret" }}
      key: {{ .Values.keystore.creds.passwdKey }}
{{ end }}



{{/*----------------------------------- Logging functions ----------------------------------*/}}

{{/*
Define the log streaming method (DR-470222-010)
*/}}
{{- define "eric-oss-assurance-augmentation.streamingMethod" -}}
{{- $streamingMethod := "direct" -}}
{{- if .Values.global -}}
  {{- if .Values.global.log -}}
      {{- if .Values.global.log.streamingMethod -}}
        {{- $streamingMethod = .Values.global.log.streamingMethod }}
      {{- end -}}
  {{- end -}}
{{- end -}}
{{- if .Values.log -}}
  {{- if .Values.log.streamingMethod -}}
    {{- $streamingMethod = .Values.log.streamingMethod }}
  {{- end -}}
{{- end -}}
{{- print $streamingMethod -}}
{{- end -}}

{{/*
Define the label needed for reaching eric-log-transformer (DR-470222-010)
*/}}
{{- define "eric-oss-assurance-augmentation.directStreamingLabel" -}}
{{- $streamingMethod := (include "eric-oss-assurance-augmentation.streamingMethod" .) -}}
{{- if or (eq "direct" $streamingMethod) (eq "dual" $streamingMethod) }}
logger-communication-type: "direct"
{{- end -}}
{{- end -}}

{{/*
Define logging environment variables (DR-470222-010)
*/}}
{{ define "eric-oss-assurance-augmentation.loggingEnv" }}
- name: POD_NAME
  valueFrom:
    fieldRef:
      fieldPath: metadata.name
- name: POD_UID
  valueFrom:
    fieldRef:
      fieldPath: metadata.uid
- name: CONTAINER_NAME
  value: eric-oss-assurance-augmentation
- name: NODE_NAME
  valueFrom:
    fieldRef:
      fieldPath: spec.nodeName
- name: NAMESPACE
  valueFrom:
    fieldRef:
      fieldPath: metadata.namespace
{{- $streamingMethod := (include "eric-oss-assurance-augmentation.streamingMethod" .) -}}
{{- $g := fromJson (include "eric-oss-assurance-augmentation.global" .) -}}
{{- if or (eq "direct" $streamingMethod) (eq "dual" $streamingMethod) -}}
  {{- if $g.security.tls.enabled }}
    {{- if eq "direct" $streamingMethod }}
- name: LOGBACK_CONFIG_FILE
  value: "classpath:logback-https.xml"
    {{- end }}
    {{- if eq "dual" $streamingMethod }}
- name: LOGBACK_CONFIG_FILE
  value: "classpath:logback-dual-sec.xml"
    {{- end }}
  {{- else }}
    {{- if eq "direct" $streamingMethod }}
- name: LOGBACK_CONFIG_FILE
  value: "classpath:logback-http.xml"
    {{- end }}
    {{- if eq "dual" $streamingMethod }}
- name: LOGBACK_CONFIG_FILE
  value: "classpath:logback-dual.xml"
    {{- end }}
  {{- end }}
- name: LOGSTASH_DESTINATION
  value: eric-log-transformer
{{- if $g.security.tls.enabled }}
- name: LOGSTASH_PORT
  value: "9443"
{{- else }}
- name: LOGSTASH_PORT
  value: "9080"
{{- end }}
{{- else if eq $streamingMethod "indirect" }}
- name: LOGBACK_CONFIG_FILE
  value: "classpath:logback-json.xml"
{{- else }}
  {{- fail ".log.streamingMethod unknown" }}
{{- end -}}
{{ end }}

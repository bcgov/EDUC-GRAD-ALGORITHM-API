###########################################################
#ENV VARS
###########################################################
envValue=$1
APP_NAME=$2
GRAD_NAMESPACE=$3
COMMON_NAMESPACE=$4
BUSINESS_NAMESPACE=$5
SPLUNK_TOKEN=$6
APP_LOG_LEVEL=$7
STUDENT_ASSESSMENT_NAMESPACE=$8

SPLUNK_URL="gww.splunk.educ.gov.bc.ca"
FLB_CONFIG="[SERVICE]
   Flush        1
   Daemon       Off
   Log_Level    info
   HTTP_Server   On
   HTTP_Listen   0.0.0.0
   Parsers_File parsers.conf
[INPUT]
   Name   tail
   Path   /mnt/log/*
   Exclude_Path *.gz,*.zip
   Parser docker
   Mem_Buf_Limit 20MB
   Buffer_Max_Size 1MB
[FILTER]
   Name record_modifier
   Match *
   Record hostname \${HOSTNAME}
[OUTPUT]
   Name   stdout
   Match  absolutely_nothing_bud
   Log_Level    off
[OUTPUT]
   Name  splunk
   Match *
   Host  $SPLUNK_URL
   Port  443
   TLS         On
   TLS.Verify  Off
   Message_Key $APP_NAME
   Splunk_Token $SPLUNK_TOKEN
"
PARSER_CONFIG="
[PARSER]
    Name        docker
    Format      json
"
###########################################################
#Setup for config-maps
###########################################################

if [ "$envValue" != "prod" ]
then
  ENABLE_STUDENT_ASSESSMENTS="true"
else
  ENABLE_STUDENT_ASSESSMENTS="false"
fi

echo Creating config map "$APP_NAME"-config-map
oc create -n "$BUSINESS_NAMESPACE"-"$envValue" configmap "$APP_NAME"-config-map \
  --from-literal=GRAD_TRAX_API="http://educ-grad-trax-api.$GRAD_NAMESPACE-$envValue.svc.cluster.local:8080/" \
  --from-literal=GRAD_STUDENT_API="http://educ-grad-student-api.$GRAD_NAMESPACE-$envValue.svc.cluster.local:8080/" \
  --from-literal=GRAD_COURSE_API="http://educ-grad-course-api.$GRAD_NAMESPACE-$envValue.svc.cluster.local:8080/" \
  --from-literal=ENABLE_SPLUNK_LOG_HELPER="true" \
  --from-literal=APP_LOG_LEVEL="$APP_LOG_LEVEL" \
  --from-literal=GRAD_ASSESSMENT_API="http://educ-grad-assessment-api.$GRAD_NAMESPACE-$envValue.svc.cluster.local:8080/" \
  --from-literal=GRAD_PROGRAM_API="http://educ-grad-program-api.$GRAD_NAMESPACE-$envValue.svc.cluster.local:8080/" \
  --from-literal=MAX_RETRY_ATTEMPTS="3" \
  --from-literal=GRAD_RULE_PROCESSOR_API="http://educ-rule-engine-api.$BUSINESS_NAMESPACE-$envValue.svc.cluster.local:8080/" \
  --from-literal=KEYCLOAK_TOKEN_URL="https://soam-$envValue.apps.silver.devops.gov.bc.ca/" \
  --from-literal=GRAD_STUDENT_GRADUATION_API="http://educ-grad-student-graduation-api.$GRAD_NAMESPACE-$envValue.svc.cluster.local:8080/" \
  --from-literal=STUDENT_ASSESSMENT_API="http://student-assessment-api-master.$STUDENT_ASSESSMENT_NAMESPACE-$envValue.svc.cluster.local:8080/" \
  --from-literal=NATS_MAX_RECONNECT=60 \
  --from-literal=NATS_URL="nats://nats.${COMMON_NAMESPACE}-${envValue}.svc.cluster.local:4222" \
  --from-literal=ENABLE_STUDENT_ASSESSMENTS=$ENABLE_STUDENT_ASSESSMENTS \
  --dry-run=client -o yaml | oc apply -f -

echo Creating config map "$APP_NAME"-flb-sc-config-map
oc create -n "$BUSINESS_NAMESPACE"-"$envValue" configmap "$APP_NAME"-flb-sc-config-map \
  --from-literal=fluent-bit.conf="$FLB_CONFIG" \
  --from-literal=parsers.conf="$PARSER_CONFIG" \
  --dry-run=client -o yaml | oc apply -f -

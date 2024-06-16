#!/bin/bash

if [ "$#" -ne 3 ]; then
    echo "Usage: $0 <slowness_type> <total_time_millis> <delay_interval>"
    exit 1
fi

SLOWNESS_TYPE=$1
TOTAL_TIME_MILLIS=$2
DELAY_INTERVAL=$3

POD_YAML=$(cat <<EOF
apiVersion: v1
kind: Pod
metadata:
  name: temp-curl-pod
spec:
  containers:
  - name: curl-container
    image: curlimages/curl:latest
    command: ["curl"]
    args: ["-X", "POST", "http://delivery-service:8080/slowness?type=${SLOWNESS_TYPE}&totalTimeMillis=${TOTAL_TIME_MILLIS}&delayIntervalMillis=${DELAY_INTERVAL}"]
  restartPolicy: Never
EOF
)

echo "$POD_YAML" | kubectl apply -f -
kubectl wait --for=condition=complete pod/temp-curl-pod
kubectl delete pod temp-curl-pod

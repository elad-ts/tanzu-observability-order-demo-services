#!/bin/bash

# Check if sufficient arguments are passed
if [ "$#" -ne 3 ]; then
    echo "Usage: $0 <slowness_type> <total_time_millis>"
    exit 1
fi

SLOWNESS_TYPE=$1
TOTAL_TIME_MILLIS=$2
DELAY_INTERVAL=$3

# Define pod YAML
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
    args: ["http://delivery-service:8080/slowness?type=${SLOWNESS_TYPE}&totalTimeMillis=${TOTAL_TIME_MILLIS}&delayIntervalMillis=${DELAY_INTERVAL}"]
  restartPolicy: Never
EOF
)

# Create the pod
echo "$POD_YAML" | kubectl apply -f -

# Wait for the pod to complete
kubectl wait --for=condition=complete pod/temp-curl-pod

# Clean up the pod
kubectl delete pod temp-curl-pod

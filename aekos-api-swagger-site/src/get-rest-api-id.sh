#!/bin/bash
cd `dirname $0`
stage=$1
if [ -z "$stage" ]; then
  echo "[ERROR] mandatory parameter 'stage' was no passed"
  echo "usage: $0 <stage>"
  echo "   eg: $0 dev"
  exit 1
fi
AWS_REGION=ap-southeast-2
aws \
  --region=$AWS_REGION \
  apigateway \
  get-rest-apis \
  --output json | ./extract-rest-api-id.js $stage

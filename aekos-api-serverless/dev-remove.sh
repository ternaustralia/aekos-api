#!/bin/bash
cd `dirname $0`
stage=$1
if [ -z "$stage" ]; then
  stage='dev'
fi
serverless remove \
  --stage=$stage \
  --region=ap-southeast-2 \
  --verbose

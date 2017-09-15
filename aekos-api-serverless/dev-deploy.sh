#!/bin/bash
cd `dirname $0`
stage=$1
if [ -z "$stage" ]; then
  stage='dev'
fi
serverless deploy \
  --stage=$stage \
  --region=ap-southeast-2

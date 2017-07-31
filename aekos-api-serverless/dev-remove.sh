#!/bin/bash
cd `dirname $0`
stage=$1
if [ -z "$stage" ]; then
  stage='dev'
fi
serverless remove \
  --stage=$stage \
  --region=us-west-1 \
  --verbose

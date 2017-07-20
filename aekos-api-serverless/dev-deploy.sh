#!/bin/bash
cd `dirname $0`
stage=$1
if [ -z "$stage" ]; then
  stage='dev'
fi
serverless deploy \
  --stage=$stage \
  --region=us-west-1

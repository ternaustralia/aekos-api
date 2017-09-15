#!/usr/bin/env bash
cd `dirname $0`
stage=$1
if [ -z "$stage" ]; then
  stage='dev'
fi
sls delete_domain \
  --stage=$stage \
  --region=ap-southeast-2

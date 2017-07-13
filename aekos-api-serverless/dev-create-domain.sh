#!/usr/bin/env bash
cd `dirname $0`
sls create_domain \
  --stage=dev \
  --region=us-west-1

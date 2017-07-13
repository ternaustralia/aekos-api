#!/usr/bin/env bash
cd `dirname $0`
sls delete_domain \
  --stage=dev \
  --region=us-west-1

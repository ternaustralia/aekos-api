#!/bin/bash
cd `dirname $0`
serverless deploy \
  --stage=dev \
  --region=us-west-1

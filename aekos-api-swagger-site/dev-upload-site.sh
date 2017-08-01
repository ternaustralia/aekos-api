#!/usr/bin/env bash
cd `dirname $0`
set -e
stage=$1
if [ -z "$stage" ]; then
  stage=dev
fi
./src/dev-get-swagger.sh $stage
node ./src/feed-generator.js
./src/dev-deploy-site.sh $stage

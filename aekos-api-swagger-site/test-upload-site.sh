#!/usr/bin/env bash
cd `dirname $0`
set -e
stage=test
./src/dev-get-swagger.sh $stage
./src/dev-deploy-site.sh $stage
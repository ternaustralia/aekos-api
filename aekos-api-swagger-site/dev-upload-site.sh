#!/usr/bin/env bash
cd `dirname $0`
set -e
./src/dev-get-swagger.sh
./src/dev-deploy-site.sh
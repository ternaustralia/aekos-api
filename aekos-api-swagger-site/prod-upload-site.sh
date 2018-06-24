#!/usr/bin/env bash
cd `dirname $0`
set -e
stage=prod
./dev-upload-site.sh $stage

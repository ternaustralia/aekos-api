#!/usr/bin/env bash
cd `dirname $0`
set -e
stage=test
./dev-upload-site.sh $stage

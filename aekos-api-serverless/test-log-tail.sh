#!/bin/bash
cd `dirname $0`
stage=test
./dev-log-tail.sh $1 $stage

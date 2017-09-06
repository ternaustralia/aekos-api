#!/bin/bash
cd `dirname $0`
export SLS_DEBUG=*
eventDataFile=$1
stage=$2
if [ -z "$stage" ]; then
  stage='dev'
fi
if [ -z "$eventDataFile" ]; then
  echo "[ERROR] the data file '$eventDataFile' doesn't exist"
  echo "Invokes the uber router using the supplied event data"
  echo "Usage: $0 <event-data-file>"
  echo "   eg: $0 test-data/v2-getTraitVocab-json.data.json"
  exit 1
fi
serverless invoke local \
  --stage=$stage \
  --function=uberRouter \
  --path=$eventDataFile

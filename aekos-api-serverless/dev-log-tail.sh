#!/bin/bash
cd `dirname $0`
functionName=$1
if [ -z "$functionName" ]; then
  echo "Tails to log of the supplied function."
  echo "The function name is one of the keys under 'functions' in serverless.yml."
  echo "Usage: $0 <function-name>"
  echo "   eg: $0 traitVocab-json"
  exit 1
fi
stage=$2
if [ -z "$stage" ]; then
  stage=dev
fi
functionName=`bash -c "echo $functionName | sed 's/\.js$//'"`
serverless logs \
  --function=$functionName \
  --stage=$stage \
  --region=us-west-1 \
  --tail

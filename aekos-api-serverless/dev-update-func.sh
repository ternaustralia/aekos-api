#!/bin/bash
cd `dirname $0`
FUNC=$1
stage=$2
if [ -z "$stage" ]; then
  stage='dev'
fi
if [ -z "$FUNC" ]; then
  echo "Updates a single function (much quicker than a full deploy)."
  echo "The function name is one of the keys under 'functions' in serverless.yml."
  echo "Usage: $0 <function-name>"
  echo "   eg: $0 traitVocab-json"
  exit 1
fi
FUNC=`bash -c "echo $FUNC | sed 's/\.\(j\|t\)s$//'"`
serverless deploy function \
  --function=$FUNC \
  --stage=$stage \
  --region=us-west-1

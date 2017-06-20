#!/bin/bash
cd `dirname $0`
FUNC=$1
if [ -z "$FUNC" ]; then
  echo "Tails to log of the supplied function."
  echo "The function name is one of the keys under 'functions' in serverless.yml."
  echo "Usage: $0 <function-name>"
  echo "   eg: $0 v1-getTraitVocab-json"
  exit 1
fi
FUNC=`bash -c "echo $FUNC | sed 's/\.js$//'"`
serverless logs \
  --function=$FUNC \
  --stage=dev \
  --region=us-west-1 \
  --tail

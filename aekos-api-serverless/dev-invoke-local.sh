#!/bin/bash
cd `dirname $0`
export SLS_DEBUG=*
FUNC=$1
if [ -z "$FUNC" ]; then
  echo "Invokes a single function."
  echo "The function name is one of the keys under 'functions' in serverless.yml."
  echo "Usage: $0 <function-name>"
  echo "   eg: $0 v1-getTraitVocab-json"
  exit 1
fi
FUNC=`bash -c "echo $FUNC | sed 's/\.js$//'"`
DATA=./test-data/${FUNC}.data.json
if [ ! -f "$DATA" ]; then
  echo "[ERROR] the data file $DATA doesn't exist"
  exit 1
fi
serverless invoke local \
  --function=$FUNC \
  --path=$DATA

#!/bin/bash
# Deletes an AWS stack
cd `dirname $0`
set -e
source ./src/create-stack.sh
stackprefix=$1
if [ -z "$stackprefix" ]; then
  echo "[ERROR] no stack name provided"
  echo "usage: $0 [stack name]"
  echo "   eg: $0 dev"
  exit 1
fi
stackname="$stackprefix-api-site"
echo "[INFO] deleting stack '$stackname'"
aws --region=$REGION \
  cloudformation \
  delete-stack \
  --stack-name $stackname
echo "[WARN] this will fail if you haven't emptied all the S3 buckets that are to be deleted"
sleep 5
echo "[INFO] checking progress (this might say this id does not exist if it deletes too quickly)"
aws --region=$REGION \
  cloudformation \
  describe-stacks \
  --stack-name $stackname
echo "[INFO] check progress with: ./$stackname-describe-stack.sh"
echo "[INFO] find errors with:    ./$stackname-describe-stack-events.sh"

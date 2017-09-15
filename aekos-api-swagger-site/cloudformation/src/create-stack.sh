#!/bin/bash
# creates a stack
REGION=ap-southeast-2
function actionStack {
  local \
    CS_STACK_NAME \
    CS_ACTION \
    CS_ENV_LEVEL
  local "${@}"
  DIR_OF_THIS_SCRIPT="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
  cd $DIR_OF_THIS_SCRIPT
  set -e
  CFTEMPLATE=`pwd`/aekos-api-site-cfn-stack.yml
  fullstackname="$CS_STACK_NAME-api-site"
  echo "[INFO] $CS_ACTION-ing '$fullstackname' stack"
  aws --region=$REGION \
    cloudformation \
    $CS_ACTION-stack \
    --stack-name $fullstackname \
    --template-body file://$CFTEMPLATE \
    --parameters \
      ParameterKey=envlevel,ParameterValue=$CS_ENV_LEVEL \
      ParameterKey=theregion,ParameterValue=$REGION
  sleep 5
  echo "[INFO] describing stack to check progress"
  aws --region=$REGION \
    cloudformation \
    describe-stacks \
    --stack-name $fullstackname
  echo "[INFO] check progress with: ./$fullstackname-describe-stack.sh"
  echo "[INFO] find errors with:    ./$fullstackname-describe-stack-events.sh"
}

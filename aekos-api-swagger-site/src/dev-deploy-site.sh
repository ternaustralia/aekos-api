#!/bin/bash
set -e
cd `dirname $0`
thisdir=`pwd`
STAGE=$1
if [ -z "$STAGE" ]; then
  STAGE=dev
fi
FILE_TYPE=json
BUCKET=www.$STAGE.api.aekos.org.au
SWAGGER_UI_DIR=../swagger-ui-dist
AWS_REGION=us-west-1
SWAGGER_DEF=swagger-aekos-api-$STAGE.$FILE_TYPE
stagelessSwaggerDef=`bash -c "echo $SWAGGER_DEF" | sed "s/-$STAGE//"`
if [ ! -f "$SWAGGER_DEF" ]; then
  echo "ERROR: can't find swagger definition ./$SWAGGER_DEF"
  exit 1
fi
cd $SWAGGER_UI_DIR
aws s3 sync \
  --region=$AWS_REGION \
  --delete \
  --acl public-read \
  . s3://$BUCKET/
cd $thisdir
aws s3 cp \
  --region=$AWS_REGION \
  --acl public-read \
  $SWAGGER_DEF s3://$BUCKET/$stagelessSwaggerDef
aws s3 cp \
  --region=$AWS_REGION \
  --acl public-read \
  feed.rss s3://$BUCKET/
aws s3 cp \
  --region=$AWS_REGION \
  --acl public-read \
  feed.atom s3://$BUCKET/
echo "URL: ${BUCKET}.s3-website-${AWS_REGION}.amazonaws.com"


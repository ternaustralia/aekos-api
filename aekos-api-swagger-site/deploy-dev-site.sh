#!/bin/bash
cd `dirname $0`
BUCKET=www.dev2.api.aekos.org.au
SWAGGER_UI_DIR=swagger-ui-dist
AWS_REGION=us-west-1
STAGE=dev # TODO make param
FILE_TYPE=json # TODO make param
SWAGGER_DEF=swagger-aekos-api-$STAGE.$FILE_TYPE
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
cd ..
aws s3 cp \
  --region=$AWS_REGION \
  --acl public-read \
  $SWAGGER_DEF s3://$BUCKET/
S3_WEBSITE_REGION=ap-southeast-2
echo "URL: ${BUCKET}.s3-website-${S3_WEBSITE_REGION}.amazonaws.com"


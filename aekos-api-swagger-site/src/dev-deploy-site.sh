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
SWAGGER_UI_DIR=swagger-ui-dist
EXAMPLES_DIR=examples
AWS_REGION=us-west-1
SWAGGER_DEF=swagger-aekos-api-$STAGE.$FILE_TYPE
gitcommitfile=git-commit.txt
stagelessSwaggerDef=`bash -c "echo $SWAGGER_DEF" | sed "s/-$STAGE//"`
if [ ! -f "$thisdir/$SWAGGER_DEF" ]; then
  echo "ERROR: can't find swagger definition $thisdir/$SWAGGER_DEF"
  exit 1
fi
cd ..
git log -1 > $gitcommitfile
printf '\n\n' >> $gitcommitfile
git st >> $gitcommitfile
aws s3 sync \
  --region=$AWS_REGION \
  --delete \
  --acl public-read \
  --exclude="*" \
  --include="$SWAGGER_UI_DIR/*" \
  --include="$EXAMPLES_DIR/*" \
  --exclude="$EXAMPLES_DIR/node_modules/*" \
  --include="index*" \
  --include="sampling-protocol-summary.html" \
  --include="favicon.ico" \
  --include="header-bg.jpg" \
  --include="$gitcommitfile" \
  . s3://$BUCKET/
aws s3 cp \
  --region=$AWS_REGION \
  --acl public-read \
  $thisdir/$SWAGGER_DEF s3://$BUCKET/$SWAGGER_UI_DIR/$stagelessSwaggerDef
aws s3 cp \
  --region=$AWS_REGION \
  --acl public-read \
  $thisdir/feed.rss s3://$BUCKET/
aws s3 cp \
  --region=$AWS_REGION \
  --acl public-read \
  $thisdir/feed.atom s3://$BUCKET/
echo "URL: ${BUCKET}.s3-website-${AWS_REGION}.amazonaws.com"


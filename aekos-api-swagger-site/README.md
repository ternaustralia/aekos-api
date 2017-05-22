# AEKOS API Swagger UI
## What is it?
We use the Swagger UI (http://swagger.io/swagger-ui/) as is provided from Swagger and add our Swagger definition in, then deploy it all to a webserver like an AWS S3 static site.

# How to update our Swagger definition
## Prerequisites
 1. have the aws cli installed
 1. configure the aws cli to point to the correct region (us-west-1 at time of writing)
 1. have the correct access key configured for aws cli
## Steps
 1. run the `get-swagger.sh` script in this directory
 1. deploy the updated site to S3
 1. mark the new swagger def as public in S3
 1. invalidate the CloudFront cache so the new version gets rolled out quickly

# How to update the Swagger UI version
 1. the instructions can be found at http://swagger.io/docs/swagger-tools/#download-33
 1. once you have the latest Swagger UI `dist` dir, overwrite all the files in this dir with it
 1. update `index.html` to reference `/swagger-aekos-api-{stage}.json`
 1. deploy the updated files
 1. mark all the new files as public in S3
 1. invalidate the CloudFront cache so the new version gets rolled out quickly

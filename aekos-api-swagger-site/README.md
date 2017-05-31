# AEKOS API Swagger UI
## What is it?
We use the Swagger UI (http://swagger.io/swagger-ui/) as is provided from Swagger and add our Swagger definition in, then deploy it all to a webserver like an AWS S3 static site.

# How to update our Swagger definition
## Prerequisites
 1. have NodeJS 7.x installed
 1. have the aws cli installed
 1. have the correct access key configured for aws cli
## Steps
 1. pull the Swagger definition from API Gateway using the `get-swagger.sh` script
 1. deploy the updated site to S3 using the `deploy-dev-site.sh` script
 1. invalidate the CloudFront cache so the new version gets rolled out quickly

# How to update the Swagger UI version
 1. the instructions can be found at http://swagger.io/docs/swagger-tools/#download-33
 1. once you have the latest Swagger UI `dist` dir, overwrite all the files in this dir with it
 1. update `index.html` to reference `/swagger-aekos-api-{stage}.json`
 1. deploy the updated site to S3 using the `deploy-dev-site.sh` script
 1. invalidate the CloudFront cache so the new version gets rolled out quickly

## Note about Swagger UI styles
At the time of writing Swagger UI doesn't deal well with headings or lists in the description (which we use both of) so we can fix this by patching the `index.html` file like this:

    diff --git a/aekos-api-swagger-site/swagger-ui-dist/index.html b/aekos-api-swagger-site/swagger-ui-dist/index.html
    --- a/aekos-api-swagger-site/swagger-ui-dist/index.html
    +++ b/aekos-api-swagger-site/swagger-ui-dist/index.html
    @@ -25,6 +25,10 @@
        body {
          margin:0;
          background: #fafafa;
    +      line-height: 1.42857143;
    +      font-size: 14px;
    +      font-family: Open Sans,sans-serif;
    +      color: #3b4151;
        }
      </style>
    </head>

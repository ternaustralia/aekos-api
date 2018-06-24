# AEKOS API Swagger UI
## What is it?
We use the Swagger UI (http://swagger.io/swagger-ui/) as is provided from Swagger and add our Swagger definition in, then deploy it all to a webserver like an AWS S3 static site.

# How to update our Swagger definition
## Prerequisites
 - NodeJS 7.x
 - [Yarn](https://yarnpkg.com)
 - AWS CLI (configured with the correct access key)

## Steps to deploy

Run these steps the first time

 1. create an S3 bucket to hold the static site files, e.g: www.`<stage>`.api.aekos.org.au
 1. create a CloudFront distribution for the S3 bucket to serve the site
 1. create a Route53 A-alias record named the same as the S3 bucket and point to the CloudFront distribution

Run these steps every time
```bash
cd /path/to/aekos-api/aekos-api-swagger-site # cd into this repo
yarn # install dependencies
./dev-upload-site.sh # download the swagger definition, ammend it, deploy the swagger UI site
# optionally, invalidate the CloudFront cache to get the new site rolled out faster
```

# How to update the Swagger UI version

        cd ~/git/ # or wherever your git stuff lives
        git clone https://github.com/swagger-api/swagger-ui.git # or `git pull` if you already have it
        cd ~/git/aekos-api/aekos-api-swagger-site/
        ./update-swagger-ui.sh ~/git/swagger-ui/ # copies a new version of swagger-ui into our project and makes required changes
        git commit -m "updated swagger-ui to latest version"
        ./get-swagger.sh && ./deploy-dev-site.sh # pull the latest swagger def and deploy the site
        # optionally, invalidate the CloudFront cache to get the new site rolled out faster

## Note about patching Swagger UI styles
At the time of writing Swagger UI doesn't deal well with headings or lists in the description (which we use both of). We fix this using the `update-swagger-ui.sh` script. If we no longer need to make these changes, edit the script and delete the patch file.

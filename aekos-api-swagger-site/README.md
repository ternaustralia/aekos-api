# AEKOS API Swagger UI
## What is it?
We use the Swagger UI (http://swagger.io/swagger-ui/) as is provided from Swagger and add our Swagger definition in, then deploy it all to a webserver like an AWS S3 static site.

# How to update our Swagger definition
## Prerequisites
 - NodeJS 7.x
 - AWS cli
 - AWS cli is configured with the correct access key
## Steps

        cd /path/to/aekos-api/aekos-api-swagger-site # cd into this repo
        get-swagger.sh # pull the latest swagger definition
        deploy-dev-site.sh # deploy the whole documentation site
        # optionally, invalidate the CloudFront cache to get the new site rolled out faster

# How to update the Swagger UI version

        cd ~/git/ # or wherever your git stuff lives
        git clone https://github.com/swagger-api/swagger-ui.git # or `git pull` if you already have it
        cd ~/git/aekos-api/aekos-api-swagger-site/
        ./update-swagger-ui.sh ~/git/swagger-ui/ # copies a new version of swagger-ui into our project and makes required changes
        git commit -m "updated swagger-ui to latest version"
        ./get-swagger.sh && ./deploy-dev-site.sh # pull the latest swagger def and deploy the site
        # optionally, invalidate the CloudFront cache to get the new site rolled out faster

## Note about patching Swagger UI styles
At the time of writing Swagger UI doesn't deal well with headings or lists in the description (which we use both of). We fix as part of the `update-swagger-ui.sh` script. If we no longer need to make these changes, edit the script and delete the patch file.

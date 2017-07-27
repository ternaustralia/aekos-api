# aekos-api-serverless
This is the server itself.

## What's it built with?
This is a NodeJS project that uses the [Serverless framework](https://serverless.com/). We deploy to Amazon's AWS (Lambda and API Gateway) and use AWS RDS as a data store. We use Yarn to make the package management less brittle. See the `architecture.md` document for more information.

## Prerequisites for developing
 - NodeJS 6.10+
 - Serverless 1.x
 - [Yarn](https://yarnpkg.com/)
 - AWS CLI [https://github.com/aws/aws-cli]()
 - API keys for an AWS account (so you can deploy)

## Getting started

    # clone this repo
    cd aekos-api/aekos-api-serverless/
    yarn # install all dependencies
    npm install --global serverless # install serverless globally

## Installing NodeJS
We develop against a specific version of NodeJS (v6.10.3 at the time of writing) because that's what AWS supports. The best way to get this specific version of NodeJS is to use [https://github.com/creationix/nvm#install-script](NVM). Once you've installed `nvm`, install the specific version of NodeJS with:

    nvm install 6.10

## Deploying

    # TODO add step about creating AWS RDS instance
    ./dev-deploy.sh # deploy the code to AWS
    # look at the ../aekos-api-swagger-site/ project for how to deploy the doco site

# 200 Resource limit on CloudFormation
As serverless uses CloudFormation internally, we're subject to the CF limits. One limit is that you cannot have more than 200 resources in a single template. We're pretty close to this already (actually we've gone over but had to do some tricky stuff to bring it back down). See [https://github.com/serverless/serverless/issues/3411]() for a discussion on possible fixes.

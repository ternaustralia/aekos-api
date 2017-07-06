# aekos-api-serverless
This is the server itself.

## What's it built with?
This is a NodeJS project that uses the [Serverless framework](https://serverless.com/). We deploy to Amazon's AWS (Lambda and API Gateway) and use AWS RDS as a data store. We use Yarn to make the package management less brittle.

## Prerequisites for developing
 - NodeJS 6.10+
 - Serverless 1.x
 - [Yarn](https://yarnpkg.com/)
 - API keys for an AWS account (so you can deploy)

## Getting started

    # clone this repo
    cd aekos-api/aekos-api-serverless/
    yarn # install all dependencies
    npm install --global serverless # install serverless globally

## Deploying

    # TODO add step about creating AWS RDS instance
    ./dev-deploy.sh # deploy the code to AWS

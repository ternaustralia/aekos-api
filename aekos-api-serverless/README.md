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

    # create a MySQL DB somewhere
    # use the ../aekos-api-producer/ project to create some data
    # load the data to the DB
    cp secrets.example.yml secrets.yml
    # edit secrets.yml to have the required values
    ./dev-create-domain.sh
    ./dev-deploy.sh
    # look at the ../aekos-api-swagger-site/ project for how to deploy the doco site

For higher environments, just run the corresponding scripts.

## Notes to developers

### 200 Resource limit on CloudFormation
As serverless uses CloudFormation internally, we're subject to the CF limits. One limit is that you cannot have more than 200 resources in a single template. We were pretty close to this but now we use a single "router" lambda and that's solved it. See [https://github.com/serverless/serverless/issues/3411]() for a discussion on possible fixes.

### event.path vs event.requestContext.path
We use both throughout this app. When we want to know which path was called because we're branching based on the version, we use the `event.path` because it doesn't contain the stage prefix (e.g: `dev`, `test`) so we know it'll start with `/v1/...`. Then, for the link header, we use `event.requestContext.path` becuase it **will** have the stage prefix. This is important because if we aren't running behind CloudFront, we need that prefix for calls to work so we want it included.

#!/bin/bash
set -ev
cd `dirname $0`
pushd aekos-api-producer
./mvnw clean test
popd
cd aekos-api-serverless
nvm install 7.10
nvm use 7.10
npm install -g yarn
yarn
npm test

#!/bin/bash
set -ev
cd `dirname $0`
pushd aekos-api-producer
./mvnw clean test
popd
cd aekos-api-serverless
yarn
npm test

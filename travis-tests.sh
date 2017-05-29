#!/bin/bash
set -ev
cd `dirname $0`
pushd aekos-api-producer
mvn clean test
popd
cd aekos-api-serverless
yarn
npm test

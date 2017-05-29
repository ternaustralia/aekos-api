#!/bin/bash
set -ev
cd `dirname $0`
pushd aekos-api-producer
mvn clean test
popd
cd aekos-api-serverless
npm install # Java VM in Travis doesn't have yarn
npm --harmony test

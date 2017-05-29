#!/bin/bash
set -ev
cd `dirname $0`
pushd aekos-api-producer
./mvnw clean test
popd
cd aekos-api-serverless
curl -o- https://raw.githubusercontent.com/creationix/nvm/v0.33.2/install.sh | bash
export NVM_DIR="$HOME/.nvm"
[ -s "$NVM_DIR/nvm.sh" ] && \. "$NVM_DIR/nvm.sh"  # This loads nvm
nvm install 7.10
nvm use 7.10
npm install -g yarn
yarn
npm test

[![Build Status](https://travis-ci.org/adelaideecoinformatics/aekos-api.svg?branch=master)](https://travis-ci.org/adelaideecoinformatics/aekos-api)

# &AElig;KOS API
This repository is the endpoint that provides a publicly accessible machine interface for data from the AEKOS system (http://www.aekos.org.au/).

## Getting started
This repository requires the following technologies:
 1. Java 8 JDK
 2. Apache Maven 3.x

You can use the repository like this:

    git clone <this url>
    cd aekos-api/
    ./run-server.sh

Or, an alternate way if you like to run the steps yourself:

    git clone <this url>
    cd aekos-api/
    mvn clean package
    java -jar target/aekos-api-X.Y.jar # replace X.Y with the version
Then you'll be able to see the endpoint at https://localhost:8099/. Note it runs on HTTPS so if you don't add the https:// prefix, it won't load.

You can build a docker container that's configured to run this webapp with:

    cd aekos-api/
    ./build-docker-image.sh
    # instructions on how to use it will be printed to the console

## Interacting with the endpoint
Documentation is automatically generated using Swagger. You will be redirected to the documentation when you do a GET on the root of the webapp. You can also look at the production documentation at https://api.aekos.org.au:8099/

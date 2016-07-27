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
Then you'll be able to see the endpoint at https://localhost:8443/.

You can build a docker container that's configured to run this webapp with:

    cd aekos-api/
    ./build-docker-image.sh
    # instructions on how to use it will be printed to the console

## Interacting with the endpoint
Documentation is automatically generated using Swagger. You will see a link to the documentation when you do a GET on the root of the webapp or you can go straight to https://api.aekos.org.au/swagger-ui.html.

## Building a production instance
Note: We're using https://letsencrypt.org/ to generate SSL certificates.
 1. generate SSL certificates (if you haven't already or they've expired) #TODO add more detail
 1. build a JKS keystore from the certificates with `generate-keystore-from-cert.sh`
 1. build a Docker container using `build-docker-image.sh` (Maven will include the keystore and password)
 1. if you didn't do this on the prod machine, then push the image to the remote machine
 1. (re)start the Docker instance

[![Build Status](https://travis-ci.org/adelaideecoinformatics/aekos-api.svg?branch=master)](https://travis-ci.org/adelaideecoinformatics/aekos-api)

# &AElig;KOS API
This repository is the endpoint that provides a publicly accessible machine interface for data from the AEKOS system (http://www.aekos.org.au/).

## Getting started
This repository requires the following technologies:
 1. Java 8 JDK
 2. Apache Maven 3.x

You can use the repository like this:

    git clone <this url>
    cd aekos-api/aekos-api-server/
    ./run-server.sh

You can change the port(s) that the server starts on with:

    ./run-server.sh -Drun.arguments="--server.http.port=8081,--server.port=8444"

Or, an alternate way if you like to run the steps yourself:

    git clone <this url>
    cd aekos-api/aekos-api-server/
    mvn clean package
    java -jar target/aekos-api-server-X.Y.jar # replace X.Y with the version
Then you'll be able to see the endpoint at https://localhost:8443/.

You can build a docker container that's configured to run this webapp with:

    cd aekos-api/
    ./build-docker-image.sh
    # instructions on how to use it will be printed to the console

## Interacting with the endpoint
Documentation is automatically generated using Swagger. You will see a link to the documentation when you do a GET on the root of the webapp or you can go straight to https://api.aekos.org.au/swagger-ui.html.

## Building a production instance

### Preparing SSL certificates (run every 90 days)
We're using https://letsencrypt.org/ to generate SSL certificates. Here's how to (re)generate them.
 1. check https://certbot.eff.org/#ubuntuxenial-other for updated steps and update doco if required
 1. SSH to the prod machine. You need to run `letsencrypt` from the machine that has the DNS record pointing to it
 1. make sure `letsencrypt` is installed with `$ sudo apt-get install letsencrypt` (on Ubuntu Xenial, might not work on earlier versions)
 1. stop the API server because we're going to use the standalone certbot server that needs to bind to the 443 port
 1. generate a new certificate: `letsencrypt certonly --standalone -d api.aekos.org.au`. You could also just run `letsencrypt certonly` to do the process interactively.
 1. the log output will tell you where the certificates are written to. Copy the `fullchain.pem` and `privkey.pem` files to somewhere the current user can read (like the aekos-api git repo dir, probably the current dir): `sudo cp /etc/letsencrypt/live/api.aekos.org.au/{fullchain,privkey}.pem ~/git/aekos-api/`
 1. build a JKS keystore from the certificates with `./generate-keystore-from-cert.sh ./fullchain.pem ./privkey.pem somePassw0rd` (choose your own password)
 1. the JKS keystore and password will be written to the location expected by the next step so it'll include them in the build of the docker image.

### Steps to run every production build
 1. build a Docker container using `build-docker-image.sh` (Maven will include the keystore and password)
 1. if you didn't do this on the prod machine, then push the image to the remote machine
 1. (re)start the Docker instance

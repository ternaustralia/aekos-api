#!/bin/bash
cd `dirname $0`
TAR_NAME=aekos-api-docker.tar
IMAGE_TAR=target/$TAR_NAME
IMAGE_NAME=aekos/aekos-api:latest
trap "echo Exited!; exit;" SIGINT SIGTERM
mvn clean package docker:build
docker save -o $IMAGE_TAR $IMAGE_NAME 
echo "Docker file written to `pwd`/$IMAGE_TAR, now you can:"
echo "  # scp to another machine"
echo "  docker load -i $TAR_NAME"
echo "  docker run -p 8099:8080 $IMAGE_NAME"

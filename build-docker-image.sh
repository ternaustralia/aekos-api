#!/bin/bash
cd `dirname $0`
TAR_NAME=aekos-api-docker.tar
IMAGE_TAR=target/$TAR_NAME
IMAGE_NAME=aekos/aekos-api:latest
trap "echo Exited!; exit;" SIGINT SIGTERM
mvn clean package docker:build
docker save -o $IMAGE_TAR $IMAGE_NAME 
echo "Docker file written to `pwd`/$IMAGE_TAR so now you can:"
echo "  # scp to another machine"
echo "  docker load -i $TAR_NAME"
echo "  docker run -p 443:8443 \\
    -v /host/path/to/tdb:/data \\
    -v /host/path/to/auth:/auth \\
    -v /host/path/to/metrics:/metrics \\
    -v /home/path/to/lucene-index:/lucene-index \\
    $IMAGE_NAME"

#!/bin/bash
cd `dirname $0`
TAR_NAME=aekos-api-producer-docker.tar
IMAGE_TAR=target/$TAR_NAME
IMAGE_NAME=aekos/aekos-api-producer:latest
trap "echo Exited!; exit;" SIGINT SIGTERM
./mvnw clean package docker:build
MVN_RC=$?
if [ "$MVN_RC" != "0" ]; then
  echo "Maven build failed"
  exit $MVN_RC
fi
docker save -o $IMAGE_TAR $IMAGE_NAME 
echo "Docker file written to `pwd`/$IMAGE_TAR so now you can:"
echo "  # scp to another machine"
echo "  docker load -i $TAR_NAME"
echo "  # change the left side of the following paths and run"
echo "  docker run \\
    -v /host/path/to/tdb:/data \\
    -v /host/path/to/output:/output \\
    $IMAGE_NAME"

#!/bin/bash
cd `dirname $0`
TAR_NAME=aekos-api-docker.tar
IMAGE_TAR=target/$TAR_NAME
IMAGE_NAME=aekos/aekos-api:latest
trap "echo Exited!; exit;" SIGINT SIGTERM
./mvnw -Pprod clean package docker:build
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
    -p 443:443 \\
    -p 80:80 \\
    -v /host/path/to/tdb:/data:ro \\
    -v /host/path/to/auth:/auth \\
    -v /host/path/to/metrics:/metrics \\
    -v /home/path/to/lucene-index:/lucene-index:ro \\
    $IMAGE_NAME"

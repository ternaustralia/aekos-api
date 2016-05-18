#!/bin/bash
cd `dirname $0`
trap "echo Exited!; exit;" SIGINT SIGTERM
mvn clean package
MVN_RC=$?
JAR_FILE=`find target/ -name "aekos-api-*.jar"`
if [ "$MVN_RC" != "0" ]; then
  echo "ERROR: maven didn't succeed. Refusing to start server"
  exit $MVN_RC
fi
java -Dserver.port=8099 -jar "$JAR_FILE"

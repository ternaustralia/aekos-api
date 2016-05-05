#!/bin/bash
cd `dirname $0`
trap "echo Exited!; exit;" SIGINT SIGTERM
MVN_RC=0
if [ ! -d "target" ]; then
  echo "No target dir, running maven."
  mvn clean package
  MVN_RC=$?
fi
JAR_FILE=`find target/ -name "aekos-api-*.jar"`
if [ ! -f "$JAR_FILE" ]; then
  echo "JAR file not found, running maven."
  mvn clean package
  MVN_RC=$?
  JAR_FILE=`find target/ -name "aekos-api-*.jar"`
fi
if [ "$MVN_RC" != "0" ]; then
  echo "ERROR: maven didn't succeed. Refusing to start server"
  exit $MVN_RC
fi
java -Dserver.port=8099 -jar "$JAR_FILE"

#!/bin/bash
cd `dirname $0`
mvn -Dserver.port=8099 spring-boot:run

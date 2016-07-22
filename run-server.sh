#!/bin/bash
cd `dirname $0`
mvn clean spring-boot:run $@

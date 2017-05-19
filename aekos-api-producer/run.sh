#!/bin/bash
cd `dirname $0`
./mvnw clean spring-boot:run $@

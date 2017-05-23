#!/bin/bash
cd `dirname $0`
aws apigateway get-rest-apis --output json | ./extract-rest-api-id.js

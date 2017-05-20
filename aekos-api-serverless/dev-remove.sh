#!/bin/bash
cd `dirname $0`
serverless remove --stage=dev --region=us-west-1 --verbose

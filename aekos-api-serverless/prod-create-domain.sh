#!/usr/bin/env bash
cd `dirname $0`
./dev-create-domain.sh prod
echo 'You need to create the Route53 record manually because serverless-domain-manager'
echo '  bounces between complaining about CNAME records at the apex and no being able to'
echo '  access aekos.org.au. Too hard basket at the moment, just do it manually.

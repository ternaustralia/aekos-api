#!/bin/bash
cd `dirname $0`
DB_NAME='somedb' # add database name
THEUSER='someuser' # add username
THEPASS='somepass' # add password
THEURL='someinstance.aa111aaaaaaa.us-west-1.rds.amazonaws.com' # add instance name, cluster and AZ
mysql \
  --host=$THEURL \
  --port=3306 \
  --password=$THEPASS \
  --user=$THEUSER \
  --protocol=tcp \
  $DB_NAME

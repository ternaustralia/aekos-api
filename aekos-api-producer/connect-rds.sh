#!/bin/bash
CURR_DIR=`dirname $0`
source $CURR_DIR/db_env_vars.sh
DB=$1
if [ -z "$DB" ]; then
  DB=$DB_NAME
fi
mysql \
 --host=$THEURL \
 --port=$THEPORT \
 --password=$THEPASS \
 --user=$THEUSER \
 --protocol=tcp \
 $DB

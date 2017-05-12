#!/bin/bash
CURR_DIR=`dirname $0`
source $CURR_DIR/db_env_vars.sh
mysql \
 --host=$THEURL \
 --port=$THEPORT \
 --password=$THEPASS \
 --user=$THEUSER \
 --protocol=tcp \
 $DB_NAME

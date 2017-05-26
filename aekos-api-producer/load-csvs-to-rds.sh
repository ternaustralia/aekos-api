#!/bin/bash
# (re)creates the schema and loads the tables
# run it from the directory with the CSVs in it
CURR_DIR=`dirname $0`
source $CURR_DIR/db_env_vars.sh
echo "Recreating schema"
cat $CURR_DIR/create-schema.sql | mysql \
 --host=$THEURL \
 --port=$THEPORT \
 --password=$THEPASS \
 --user=$THEUSER \
 --protocol=tcp \
 $DB_NAME
SCHEMA_RC=$?
if [ "$SCHEMA_RC" != "0" ]; then
  echo "ERROR: failed to create schema"
  exit $SCHEMA_RC
fi
echo "Loading data"
for CURR in "citations" "env" "envvars" "species" "traits"; do
  echo "Loading $CURR.csv"
  mysqlimport \
    --local \
    --host=$THEURL \
    --port=$THEPORT \
    --password=$THEPASS \
    --user=$THEUSER \
    --protocol=tcp \
    --verbose \
    --fields-terminated-by=, \
    --fields-optionally-enclosed-by=\" \
    $DB_NAME \
    $CURR.csv
done
echo "Done :D"

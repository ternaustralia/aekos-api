#!/bin/bash
# (Re)creates the schema and loads the tables.
# Run it from the directory with the CSVs in it
CURR_DIR=`dirname $0`
set -e
source $CURR_DIR/db_env_vars.sh
SCHEMA_PATH="$CURR_DIR/create-schema.sql"
TARGET_DB="$DB_NAME_PREFIX`date +%Y%m%d_%H%M`"
LOG_FILE="$TARGET_DB-load.log"
echo "Writing to log file $LOG_FILE"
RAWDB="rawdata"
LOAD_FROM_RAW_PATH="$CURR_DIR/load-from-raw.sql"

echo "(Re)creating raw DB ($RAWDB) and target DB ($TARGET_DB)" | tee $LOG_FILE
RAW_SCHEMA_SCRIPT=`grep -v "NOT_RAW" $SCHEMA_PATH`
TARGET_DB_SCHEMA_SCRIPT=`cat $SCHEMA_PATH`
echo "
  DROP DATABASE IF EXISTS $RAWDB;
  CREATE DATABASE $RAWDB;
  USE $RAWDB;
  $RAW_SCHEMA_SCRIPT
  CREATE DATABASE $TARGET_DB;
  USE $TARGET_DB;
  $TARGET_DB_SCHEMA_SCRIPT
" | mysql \
 --host=$THEURL \
 --port=$THEPORT \
 --password=$THEPASS \
 --user=$THEUSER \
 --protocol=tcp 2>&1 | tee --append $LOG_FILE

echo "Loading data to raw DB" | tee --append $LOG_FILE
for CURR in "citations" "env" "envvars" "species" "traits"; do
  echo "$(tput setaf 2)Loading $CURR.csv$(tput sgr0)" | tee --append $LOG_FILE
  echo "
    USE $RAWDB;
    LOAD DATA LOCAL INFILE '$CURR.csv'
    INTO TABLE $CURR
    FIELDS
      TERMINATED BY ','
      OPTIONALLY ENCLOSED BY '\"';
    SHOW WARNINGS;
  " | mysql \
  --host=$THEURL \
  --port=$THEPORT \
  --password=$THEPASS \
  --user=$THEUSER \
  --protocol=tcp \
  2>&1 | tee --append $LOG_FILE
done

echo "Pushing data from raw into the target DB" | tee --append $LOG_FILE
cat $LOAD_FROM_RAW_PATH | mysql \
  --host=$THEURL \
  --port=$THEPORT \
  --password=$THEPASS \
  --user=$THEUSER \
  --protocol=tcp \
  $TARGET_DB \
  2>&1 | tee --append $LOG_FILE

echo "Done :D Loaded to $TARGET_DB via $RAWDB" | tee --append $LOG_FILE

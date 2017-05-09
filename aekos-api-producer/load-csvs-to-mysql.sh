#!/bin/bash
cd `dirname $0`
DB_NAME=test
THEPASS=my-secret-pw
for CURR in "species" "traits"; do
  echo "Loading $CURR.csv"
  mysqlimport \
    --local \
    --host=localhost \
    --port=3306 \
    --password=$THEPASS \
    --user=root \
    --protocol=tcp \
    --verbose \
    --fields-terminated-by=, \
    --fields-optionally-enclosed-by=\" \
    $DB_NAME \
    $CURR.csv
done
echo "Done :D"

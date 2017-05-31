#!/bin/bash
# Converts the OWL file to a JSON file that contains an object with code = label mappings
# for the API server to use to resolve trait and environmental variable codes to titles.
# Requirements:
#  - Apache JENA 3.x
#  - NodeJS 7.x
cd `dirname $0`
set -e
JENA_BIN=`cd ~/tools/apache-jena-3.1.0/bin && pwd` # you might have to edit this (or turn it into a param)
OWLFILE="aekos_20160713_1647.owl" # update when we get a new OWL file
QUERY_RESULT_FILE="query-result.json"
TEMP_N3="temp-owl-as-triples.n3"
FINAL_FILE="code-to-label.json"
echo "[info] Converting to N3"
$JENA_BIN/turtle $OWLFILE > $TEMP_N3
echo "[info] Running query"
$JENA_BIN/arq \
 --results=JSON \
 --data=$TEMP_N3 \
 --query=owl-to-json.rq > $QUERY_RESULT_FILE
echo "[info] Removing temporary N3 file"
rm -f $TEMP_N3
echo "[info] Parsing query result"
node map-query-response.js $QUERY_RESULT_FILE > $FINAL_FILE
echo "[info] Removing query result"
rm -f $QUERY_RESULT_FILE
echo "[info] Final mapping file written to $FINAL_FILE"

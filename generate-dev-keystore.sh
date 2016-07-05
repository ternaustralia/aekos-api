#!/bin/bash
cd `dirname $0`
CERT_NAME=localhost
KEYSTORE_NAME="./src/main/resources/keystore.jks"
PASS=password
ALIAS=selfsigned
rm $KEYSTORE_NAME
printf "$CERT_NAME\nDev\nTERN Ecoinformatics\nAdelaide\nSouth Australia\nAU\nyes\n\n" | \
  keytool -genkey -keyalg RSA -alias $ALIAS \
  -keystore $KEYSTORE_NAME -storepass $PASS -validity 360 -keysize 2048
printf "\n\n\n"
echo "Printing generated certificate"
echo ""
echo $PASS | keytool -list -v -keystore $KEYSTORE_NAME -alias $ALIAS
printf "\n\nThe keystore password is '$PASS'. Make sure application.properties has this configured correctly\n"

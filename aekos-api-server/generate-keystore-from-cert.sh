#!/bin/bash
cd `dirname $0`
trap "echo Exited!; exit;" SIGINT SIGTERM
USAGE="This script creates a JKS keystore from the Let'sEncrypt certificate and key.
The password parameter is the password that will be set on the keystore so you can make it up.
The password is written to a file and automatically read in by the next step so you don't have to remember it.
usage: $0 <fullchain> <privkey> <keystore password>
   eg: $0 ./fullchain.pem ./privkey.pem roflc0pt3r\n"
FULLCHAIN=$1
PRIVKEY=$2
KS_PW=$3
if [ -z "$FULLCHAIN" ];then
  echo "$(tput setaf 1)ERROR: path to fullchain PEM not supplied$(tput sgr0)"
  printf "$USAGE"
  exit 1
fi
if [ -z "$PRIVKEY" ];then
  echo "$(tput setaf 1)ERROR: path to privkey PEM not supplied$(tput sgr0)"
  printf "$USAGE"
  exit 1
fi
if [ -z "$KS_PW" ];then
  echo "$(tput setaf 1)ERROR: keystore password not supplied$(tput sgr0)"
  printf "$USAGE"
  exit 1
fi
PKCS_OUT=/tmp/aekos-api-pkcs.p12
JKS_OUT=./src/main/docker/prod-keystore.jks
PW_OUT=./src/main/docker/prod-keystore.properties
CERT_NAME=LE
# TODO add call to Let'sEncrypt
if [ -f "$JKS_OUT" ];then
  echo "Java keystore '$JKS_OUT' already exists, deleting it!"
  rm $JKS_OUT
fi
echo $KS_PW | openssl pkcs12 -export -in $FULLCHAIN -inkey $PRIVKEY -out $PKCS_OUT -name $CERT_NAME -password stdin
# -destkeypass $KS_PW \
keytool \
 -importkeystore \
 -deststorepass $KS_PW \
 -destkeystore $JKS_OUT \
 -srckeystore $PKCS_OUT \
 -srcstoretype PKCS12 \
 -srcstorepass $KS_PW \
 -alias $CERT_NAME
rm $PKCS_OUT
echo "keystore-password=$KS_PW" > $PW_OUT
echo "Done, wrote keystore to $JKS_OUT. Wrote keystore password '$KS_PW' to $PW_OUT."
echo "Now run the $(tput setaf 2)build-docker-image.sh$(tput sgr0) script to build a container with the keystore in it (maven reads the password and does the hard work)."

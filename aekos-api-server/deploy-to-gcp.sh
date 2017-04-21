#!/bin/bash
# builds and deploys the app to the currently configured (using gcloud) GCP project.
cd `dirname $0`
mvn appengine:deploy -Dapp.stage.dockerDirectory=/ignore/me $@

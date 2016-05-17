# &AElig;KOS API
This repository is the endpoint that provides a publicly accessible machine interface for data from the AEKOS system (http://www.aekos.org.au/).

## Getting started
This repository requires the following technologies:
 1. Java 8 JDK
 2. Apache Maven 3.x

You can use the repository like this:

    git clone <this url>
    cd aekos-api/
    ./run-server.sh

Or, an alternate way if you like to run the steps yourself:

    git clone <this url>
    cd aekos-api/
    mvn clean package
    java -jar target/aekos-api-X.Y.jar # replace X.Y with the version
Then you'll be able to see the endpoint at http://localhost:8099/

You can build a docker container that's configured to run this webapp with:

    cd aekos-api/
    ./build-docker-image.sh
    # instructions on how to use it will be printed to the console

## Interacting with the endpoint
The API provides the following methods for searching:
 - `GET /v1/getTraitVocab.json`  gets the available trait vocabulary values
 - `GET /v1/speciesAutocomplete.json?q=[partial species name]`  queries for a matching species names based on the supplied partial name
 - `GET /v1/getTraitBySpecies.json?q=[species name]`  queries for matching *trait* names based on the supplied *species* name
 - `GET /v1/getSpeciesByTrait.json?q=[trait name]`  queries for matching *species* names based on the supplied *trait* name

...and the following methods for getting data:
 - `GET /v1/speciesData.json[?limit=n]`  gets data in JSON format with an optional limit on the number of records returned
 - `GET /v1/speciesData.csv[?limit=n]`  gets data in CSV format with an optional limit on the number of records returned
 - `GET /v1/traitData.json?speciesName=<speciesName>`  gets the trait data for all records for the supplied species

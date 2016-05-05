# &AElig;KOS API
This repository is the endpoint that provides a publicly accessible machine interface for data from the AEKOS system (http://www.aekos.org.au/).

## Getting started
This repository requires the following technologies:
 1. Java 8 JDK
 2. Apache Maven 3.x

You can use the repository like this:

    git clone <this url>
    cd aekos-api/
    mvn clean package
    java -jar target/aekos-api-X.Y.jar
Then you'll be able to see the endpoint at http://localhost:8080/

## Interacting with the endpoint
The provides the following methods:
 - `GET /v1/data.json[?limit=n]`  gets data in JSON format with an optional limit on the number of records returned
 - `GET /v1/data.csv[?limit=n]`  gets data in CSV format with an optional limit on the number of records returned

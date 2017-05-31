# Ontology stuff

## The problem we're solving
The AEKOS infrastructure can produce the ontology as an OWL file but we can't use that directly in the NodeJS (AWS Lambda/Serverless) world. So, we solve the problem by converting that OWL file into a JSON object that has `code`s as key and `label`s and values. Then we can read that JSON file in the NodeJS and resolve codes to labels.

## How to run it
The result (`code-to-label.json`) lives in version control so you should only have to run this when you get a new OWL file.
You need the following installed:
 - Apache JENA 3.x
 - NodeJS 7.x

Then you can generate the new mapping by running the `do-owl-to-json.sh` script. Check inside it first though because you'll have to update:
 1. the path to JENA's binaries
 1. the filename of the OWL file

Be sure to delete the old OWL file and commit the new OWL and JSON mapping files.
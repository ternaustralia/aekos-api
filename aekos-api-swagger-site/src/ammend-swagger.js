#!/usr/bin/env node
'use strict'
let fs = require('fs')
let inputFile = process.argv[2]
if (typeof inputFile === 'undefined') {
  console.error('[ERROR] you must supply the input file as the first param')
  console.error(`usage: ${process.argv[1]} <input-file>`)
  console.error(`   eg: ${process.argv[1]} /path/to/unprocessed-swagger.json`)
  process.exit(1)
}
let stdout = process.stdout

const tagMappingKeySeparator = '#'
const dataRetrievalTag = 'Data Retrieval by Species'
const everythingRetrievalTag = 'Data Retrieval (everything)'
const searchTag = 'Search'
const deprecatedDataRetrievalTag = 'Deprecated - Data Retrieval by Species'
const deprecatedEverythingRetrievalTag = 'Deprecated - Data Retrieval (everything)'
const deprecatedSearchTag = 'Deprecated - Search'
const tagMapping = {
  [post('/v1/environmentData')]: deprecatedDataRetrievalTag,
  [post('/v2/environmentData')]: dataRetrievalTag,
  [post('/v1/environmentData.json')]: deprecatedDataRetrievalTag,
  [post('/v2/environmentData.json')]: dataRetrievalTag,
  [post('/v1/environmentData.csv')]: deprecatedDataRetrievalTag,
  [post('/v2/environmentData.csv')]: dataRetrievalTag,
  [post('/v1/speciesData')]: deprecatedDataRetrievalTag,
  [post('/v2/speciesData')]: dataRetrievalTag,
  [post('/v1/speciesData.json')]: deprecatedDataRetrievalTag,
  [post('/v2/speciesData.json')]: dataRetrievalTag,
  [post('/v1/speciesData.csv')]: deprecatedDataRetrievalTag,
  [post('/v2/speciesData.csv')]: dataRetrievalTag,
  [post('/v1/traitData')]: deprecatedDataRetrievalTag,
  [post('/v2/traitData')]: dataRetrievalTag,
  [post('/v1/traitData.json')]: deprecatedDataRetrievalTag,
  [post('/v2/traitData.json')]: dataRetrievalTag,
  [post('/v1/traitData.csv')]: deprecatedDataRetrievalTag,
  [post('/v2/traitData.csv')]: dataRetrievalTag,
  [get('/v1/getEnvironmentalVariableVocab.json')]: searchTag,
  [get('/v1/getTraitVocab.json')]: searchTag,
  [post('/v1/getEnvironmentBySpecies.json')]: searchTag,
  [get('/v1/getSpeciesByTrait.json')]: searchTag,
  [get('/v1/getTraitsBySpecies.json')]: searchTag,
  [get('/v1/speciesAutocomplete.json')]: searchTag,
  [post('/v1/speciesSummary.json')]: searchTag,
  [get('/v1/allSpeciesData')]: deprecatedEverythingRetrievalTag,
  [get('/v2/allSpeciesData')]: everythingRetrievalTag,
  [get('/v1/allSpeciesData.json')]: deprecatedEverythingRetrievalTag,
  [get('/v2/allSpeciesData.json')]: everythingRetrievalTag,
  [get('/v1/allSpeciesData.csv')]: deprecatedEverythingRetrievalTag,
  [get('/v2/allSpeciesData.csv')]: everythingRetrievalTag,
}

function get(path) {
  return tagMappingKey(path, 'get')
}

function post(path) {
  return tagMappingKey(path, 'post')
}

function tagMappingKey(path, method) {
  return path + tagMappingKeySeparator + method
}

// We need to read the file because piping to stdin has a limit of 65536 chars, then it adds a comma and destroys things
fs.readFile(inputFile, 'utf8', (error, data) => {
  if (error) {
    throw error
  }
  let parsedData = JSON.parse(data)
  tagResources(parsedData)
  addTagDescriptions(parsedData)
  addApiDescription(parsedData)
  removeRootRedirectResource(parsedData)
  updateParameterTypes(parsedData)
  stdout.write(JSON.stringify(parsedData, null, 2))
  stdout.write('\n')
})

function tagResources(parsedData) {
  Object.keys(parsedData.paths).forEach(currPathKey => {
    let currPath = parsedData.paths[currPathKey]
    Object.keys(currPath).forEach(currMethodKey => {
      let currMethod = currPath[currMethodKey]
      let isOptionsMethod = currMethodKey === 'options'
      if (isOptionsMethod) {
        delete currPath[currMethodKey]
        return
      }
      let key = tagMappingKey(currPathKey, currMethodKey)
      let tag = tagMapping[key]
      let isNoTagDefined = typeof tag === 'undefined'
      if (isNoTagDefined) {
        return
      }
      currMethod.tags = [tag]
    })
  })
}

function updateParameterTypes (parsedData) {
  const strategies = {
    download: e => {
      e.type = 'boolean'
    },
    rows: e => {
      e.type = 'integer'
    },
    start: e => {
      e.type = 'integer'
    },
    pageNum: e => {
      e.type = 'integer'
    },
    pageSize: e => {
      e.type = 'integer'
    },
    offset: e => {
      e.type = 'integer'
    }
  }
  Object.keys(parsedData.paths).forEach(currPathKey => {
    let currPath = parsedData.paths[currPathKey]
    Object.keys(currPath).forEach(currMethodKey => {
      let currMethod = currPath[currMethodKey]
      let params = currMethod.parameters
      if (typeof params === 'undefined') {
        return
      }
      params.forEach(e => {
        let strat = strategies[e.name]
        if (typeof strat === 'undefined') {
          return
        }
        strat(e)
      })
    })
  })
}

function addTagDescriptions(parsedData) {
  parsedData.tags = [
    {
      name: dataRetrievalTag,
      description: 'Retrieve data using parameters from search'
    },
    {
      name: everythingRetrievalTag,
      description: 'Retrieve all records'
    },
    {
      name: searchTag,
      description: 'Find species, traits and environmental variables'
    }
  ]
}

function addApiDescription(parsedData) {
  let info = parsedData.info
  info.description = theDesc
  info.title = 'AEKOS REST API'
  info.termsOfService = 'http://www.ecoinformatics.org.au/licensing_and_attributions'
  info.contact = {
    name: 'TERN Ecoinformatics',
    url: 'http://www.ecoinformatics.org.au',
    email: 'api@aekos.org.au'
  }
  info.license = {
    name: 'Licensing and attributions',
    url: 'http://www.ecoinformatics.org.au/licensing_and_attributions'
  }
}

function removeRootRedirectResource (parsedData) {
  delete (parsedData.paths['/'])
}

// Whitespace is important in this block
const theDesc = `
The AEKOS API is used for Machine2Machine (M2M) REST access to AEKOS ecological data.

# High level workflow
 1. Start with a trait or species
 1. Find what species have that trait or what *traits*/*environmental variables* are available for that species
 1. Use your traits/species/environmental variables to retrieve the data with the retrieval services

# Detailed workflow
Firstly, start with the search services. You should either find a species with \`speciesAutocomplete\`
or find a trait with \`getTraitVocab\`. Then, for traits, find the species with that trait with
\`getSpeciesByTrait\`. For species, you can find what traits or environmental variables are available with
\`getEnvironmentBySpecies\` or \`getTraitsBySpecies\`.
Now you can retrieve the data. You can get Darwin Core species records (using species names), Darwin Core +
traits (using species names and optionally filtering by trait names) or environmental variable records
(using species names and optionally filtering by environmental variable names).

# A note about species names
We have used Darwin Core terms (version 2015-06-02) as field names for the result of the data retrieval resources.
There are two fields that are relevant to species names: \`scientificName\` and \`taxonRemarks\`. The former is, as the name
suggests, for scientific species names and the latter is for records that have a common species name or a
commentary about the organism e.g.: "Grass", "Absent" or "Clover". See http://rs.tdwg.org/dwc/terms/index.htm#scientificName
and http://rs.tdwg.org/dwc/terms/index.htm#taxonRemarks for more information.

# A note about performance/response times
This API is running on AWS Lambda and API Gateway. Each resource operates individually and they will go to sleep
when they haven't been called for some time (something around 5-10 minutes seems to be the consensus). When the
resource is warm (has recently been called) it should response within 1-2 seconds. However, when it's cold (gone
to sleep) it will have to start up before it can respond so the total response time could be up to 10 seconds.

To build a UI (typically calls are time sensitive) on top of these functions, you can "pre-heat" the resources your users are likely to use
as the page loads. A workflow like:
 1. user loads page
 1. as page loads, call the function without any params and disregard the result
 1. user spends a few seconds filling out form on page
 1. user submits request
 1. AEKOS API responds quickly because the resource is already warmed up
 
# CORS support
All resources are CORS-enabled for any origin. In the interest of keeping this documentation clean, the \`OPTIONS\` methods have been omitted.`

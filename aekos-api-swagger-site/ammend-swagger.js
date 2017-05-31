#!/usr/bin/env node
'use strict'
let stdin = process.stdin
let stdout = process.stdout
let inputChunks = []

const tagMappingKeySeparator = '#'
const dataRetrievalTag = 'Data Retrieval by Species'
const searchTag = 'Search'
const corsTag = 'CORS Related'
const tagMapping = {
  [get('/v1/environmentData')]: dataRetrievalTag,
  [get('/v1/environmentData.json')]: dataRetrievalTag,
  [get('/v1/environmentData.csv')]: dataRetrievalTag,
  [get('/v1/speciesData')]: dataRetrievalTag,
  [get('/v1/speciesData.json')]: dataRetrievalTag,
  [get('/v1/speciesData.csv')]: dataRetrievalTag,
  [get('/v1/traitData')]: dataRetrievalTag,
  [get('/v1/traitData.json')]: dataRetrievalTag,
  [get('/v1/traitData.csv')]: dataRetrievalTag,
  [get('/v1/getEnvironmentalVariableVocab.json')]: searchTag,
  [get('/v1/getTraitVocab.json')]: searchTag,
  [get('/v1/getEnvironmentBySpecies.json')]: searchTag,
  [get('/v1/getSpeciesByTrait.json')]: searchTag,
  [get('/v1/getTraitsBySpecies.json')]: searchTag,
  [get('/v1/speciesAutocomplete.json')]: searchTag,
  [get('/v1/speciesSummary.json')]: searchTag,
}

function get (path) {
  return tagMappingKey(path, 'get')
}

function tagMappingKey (path, method) {
  return path + tagMappingKeySeparator + method
}

stdin.resume()
stdin.setEncoding('utf8')

stdin.on('data', function (chunk) {
  inputChunks.push(chunk);
})

stdin.on('end', function () {
  let inputJSON = inputChunks.join()
  let parsedData = JSON.parse(inputJSON)
  Object.keys(parsedData.paths).forEach(currPathKey => {
    let currPath = parsedData.paths[currPathKey]
    Object.keys(currPath).forEach(currMethodKey => {
      let currMethod = currPath[currMethodKey]
      let isOptionsMethod = currMethodKey === 'options'
      if (isOptionsMethod) {
        currMethod.tags = [corsTag]
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
  stdout.write(JSON.stringify(parsedData, null, 2))
  stdout.write('\n')
})

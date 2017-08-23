'use strict'
let r = require('./response-helper')
const mapping = {
  '/v1/speciesAutocomplete.json': require('./speciesAutocomplete-json'),
  '/v2/speciesAutocomplete.json': require('./speciesAutocomplete-json'),
  '/v1/getTraitVocab.json': require('./traitVocab-json'),
  '/v2/getTraitVocab.json': require('./traitVocab-json'),
  '/v1/getEnvironmentBySpecies.json': require('./environmentBySpecies-json'),
  '/v2/getEnvironmentBySpecies.json': require('./environmentBySpecies-json'),
  '/v1/getTraitsBySpecies.json': require('./traitsBySpecies-json'),
  '/v2/getTraitsBySpecies.json': require('./traitsBySpecies-json'),
  '/v1/getSpeciesByTrait.json': require('./speciesByTrait-json'),
  '/v2/getSpeciesByTrait.json': require('./speciesByTrait-json'),
  '/v1/getEnvironmentalVariableVocab.json': require('./environmentalVariableVocab-json'),
  '/v2/getEnvironmentalVariableVocab.json': require('./environmentalVariableVocab-json'),
  '/v1/speciesSummary.json': require('./speciesSummary-json'),
  '/v2/speciesSummary.json': require('./speciesSummary-json'),
  '/v1/speciesData.json': require('./speciesData'),
  '/v2/speciesData.json': require('./speciesData'),
  '/v1/speciesData.csv': require('./speciesData'),
  '/v2/speciesData.csv': require('./speciesData'),
  '/v1/speciesData': require('./speciesData'),
  '/v2/speciesData': require('./speciesData'),
  '/v1/traitData.json': require('./traitData'),
  '/v2/traitData.json': require('./traitData'),
  '/v1/traitData.csv': require('./traitData'),
  '/v2/traitData.csv': require('./traitData'),
  '/v1/traitData': require('./traitData'),
  '/v2/traitData': require('./traitData'),
  '/v1/environmentData.json': require('./environmentData'),
  '/v2/environmentData.json': require('./environmentData'),
  '/v1/environmentData.csv': require('./environmentData'),
  '/v2/environmentData.csv': require('./environmentData'),
  '/v1/environmentData': require('./environmentData'),
  '/v2/environmentData': require('./environmentData'),
  '/v1/allSpeciesData.json': require('./allSpeciesData'),
  '/v2/allSpeciesData.json': require('./allSpeciesData'),
  '/v1/allSpeciesData.csv': require('./allSpeciesData'),
  '/v2/allSpeciesData.csv': require('./allSpeciesData'),
  '/v1/allSpeciesData': require('./allSpeciesData'),
  '/v2/allSpeciesData': require('./allSpeciesData')
}
module.exports.handler = (event, context, callback) => {
  let db = require('./db-helper')
  doHandle(event, callback, db, r.calculateElapsedTime)
}

module.exports._testonly = {
  doHandle: doHandle
}

function doHandle (event, callback, db, elapsedTimeCalculator) {
  let path = event.path // we use event.path because it doesn't have a stage prefix
  let mappedHandler = mapping[path]
  if (!mappedHandler) {
    let msg = `The resource '${path}' does not exist.`
    console.error(`Programmer error: ${msg} But it should which means something isn't configured right.`)
    r.json.notFound(callback, msg)
    return
  }
  mappedHandler.doHandle(event, callback, db, elapsedTimeCalculator)
}

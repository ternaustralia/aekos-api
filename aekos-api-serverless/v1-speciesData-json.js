'use strict'
let r = require('./response-helper')
let v1AllSpeciesDataJson = require('./v1-allSpeciesData-json')
let v2speciesDataJson = require('./v2-speciesData-json')

module.exports.handler = (event, context, callback) => {
  let db = require('./db-helper')
  doHandle(event, callback, db, r.calculateElapsedTime)
}

function doHandle (event, callback, db, elapsedTimeCalculator) {
  let isValid = v2speciesDataJson.validator(event, callback)
  if (!isValid) {
    return
  }
  v2speciesDataJson.prepareResult(event, db, elapsedTimeCalculator).then(successResult => {
    v1AllSpeciesDataJson.removeV2FieldsFrom(successResult)
    r.json.ok(callback, successResult, event)
  }).catch(error => {
    console.error('Failed to get speciesData', error)
    r.json.internalServerError(callback)
  })
}

module.exports.doQuery = v2speciesDataJson.doQuery
module.exports.extractParams = v2speciesDataJson.extractParams
module.exports._testonly = {
  doHandle: doHandle
}

'use strict'
let r = require('./response-helper')
let v2AllSpeciesDataJson = require('./v2-allSpeciesData-json')

module.exports.handler = (event, context, callback) => {
  let db = require('./db-helper')
  doHandle(event, callback, db, r.calculateElapsedTime)
}

function doHandle (event, callback, db, elapsedTimeCalculator) {
  let processStart = r.now()
  let params = v2AllSpeciesDataJson.extractParams(event) // FIXME handle thrown Errors for invalid request
  v2AllSpeciesDataJson.doAllSpeciesQuery(params, processStart, db, elapsedTimeCalculator).then(successResult => {
    removeV2FieldsFrom(successResult)
    r.json.ok(callback, successResult, event)
  }).catch(error => {
    console.error('Failed to get v1-allSpeciesData', error)
    r.json.internalServerError(callback)
  })
}

function removeV2FieldsFrom (successResult) {
  successResult.response.forEach(curr => {
    delete curr.locationName
    delete curr.datasetName
  })
}

module.exports.doAllSpeciesQuery = v2AllSpeciesDataJson.doAllSpeciesQuery
module.exports.doQuery = v2AllSpeciesDataJson.doQuery
module.exports.getRecordsSql = v2AllSpeciesDataJson.getRecordsSql
module.exports.getCountSql = v2AllSpeciesDataJson.getCountSql
module.exports.extractParams = v2AllSpeciesDataJson.extractParams
module.exports._testonly = {
  doHandle: doHandle
}

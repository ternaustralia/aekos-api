'use strict'
let r = require('./response-helper')
let v2AllSpeciesDataJson = require('./v2-allSpeciesData-json')

module.exports.handler = (event, context, callback) => {
  let db = require('./db-helper')
  doHandle(event, callback, db, r.calculateElapsedTime)
}

function doHandle (event, callback, db, elapsedTimeCalculator) {
  v2AllSpeciesDataJson.prepareResult(event, db, elapsedTimeCalculator).then(successResult => {
    removeV2FieldsFrom(successResult)
    r.json.ok(callback, successResult, event)
  }).catch(error => {
    console.error('Failed to get v1-allSpeciesData', error)
    r.json.internalServerError(callback)
  })
}

module.exports.removeV2FieldsFrom = removeV2FieldsFrom
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

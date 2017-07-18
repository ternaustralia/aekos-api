'use strict'
let r = require('./response-helper')
let yaml = require('yamljs')
const speciesNameParam = yaml.load('./constants.yml').paramNames.SINGLE_SPECIES_NAME
let v2EnvironmentDataJson = require('./v2-environmentData-json.js')

module.exports.handler = (event, context, callback) => {
  let db = require('./db-helper')
  doHandle(event, callback, db, r.calculateElapsedTime)
}

function doHandle (event, callback, db, elapsedTimeCalculator) {
  let processStart = r.now()
  if (!r.isQueryStringParamPresent(event, speciesNameParam)) {
    r.json.badRequest(callback, `the '${speciesNameParam}' query string parameter must be supplied`)
    return
  }
  let params = v2EnvironmentDataJson.extractParams(event, db)
  doQuery(params, processStart, db, elapsedTimeCalculator).then(successResult => {
    r.json.ok(callback, successResult, event)
  }).catch(error => {
    console.error('Failed to get v1-environmentData', error)
    r.json.internalServerError(callback)
  })
}

module.exports._testonly = {
  doHandle: doHandle
}

module.exports.doQuery = doQuery
function doQuery (params, processStart, db, elapsedTimeCalculator) {
  let removeV2Fields = curr => {
    delete curr.locationName
    delete curr.datasetName
  }
  return v2EnvironmentDataJson.doQuery(params, processStart, db, elapsedTimeCalculator).then(v2Result => {
    v2Result.response.forEach(removeV2Fields)
    return v2Result
  })
}
module.exports.extractParams = v2EnvironmentDataJson.extractParams

'use strict'
let r = require('./response-helper')
let speciesDataJson = require('./speciesData-json')
let allSpeciesDataCsv = require('./allSpeciesData-csv')

module.exports.handler = (event, context, callback) => {
  let db = require('./db-helper')
  doHandle(event, callback, db, r.calculateElapsedTime)
}

const validator = r.compositeValidator([
  speciesDataJson.validator
  // TODO validate download query string parameter
])

function doHandle (event, callback, db, elapsedTimeCalculator) {
  r.handleCsvPost(event, callback, db, validator, responder, {
    event: event,
    elapsedTimeCalculator: elapsedTimeCalculator
  })
}

function responder (requestBody, db, queryStringParameters, extrasProvider) {
  return new Promise((resolve, reject) => {
    let csvHeaders = allSpeciesDataCsv.getCsvHeadersForRequestedVersion(extrasProvider.event)
    speciesDataJson.responder(requestBody, db, queryStringParameters, extrasProvider).then(successResult => {
      let result = allSpeciesDataCsv.mapJsonToCsv(successResult.response, csvHeaders)
      let downloadFileName = allSpeciesDataCsv.getCsvDownloadFileName(extrasProvider.event, 'Species')
      resolve({
        body: result,
        downloadFileName: downloadFileName,
        jsonResponse: successResult
      })
    }).catch(reject)
  })
}

module.exports._testonly = {
  doHandle: doHandle
}

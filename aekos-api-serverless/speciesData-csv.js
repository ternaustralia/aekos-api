'use strict'
let r = require('./response-helper')
let speciesDataJson = require('./speciesData-json')
let allSpeciesDataCsv = require('./allSpeciesData-csv')

const validator = r.compositeValidator([
  speciesDataJson.validator,
  r.downloadParamValidator
])

module.exports.doHandle = doHandle
function doHandle (event, callback, db, elapsedTimeCalculator) {
  r.handleCsvPost(event, callback, db, validator, responder, {
    event: event,
    elapsedTimeCalculator: elapsedTimeCalculator
  })
}

function responder (requestBody, db, queryStringParameters, extrasProvider) {
  return new Promise((resolve, reject) => {
    let csvHeaders = allSpeciesDataCsv.getCsvHeadersForRequestedVersion(extrasProvider.event)
    speciesDataJson.responder(requestBody, db, queryStringParameters, extrasProvider).then(wrapper => {
      let successResult = wrapper.body
      let result = allSpeciesDataCsv.mapJsonToCsv(successResult.response, csvHeaders)
      let downloadFileName = allSpeciesDataCsv.getCsvDownloadFileName(extrasProvider.event, 'Species')
      resolve({
        body: result,
        downloadFileName: downloadFileName,
        linkHeaderData: wrapper.linkHeaderData
      })
    }).catch(reject)
  })
}

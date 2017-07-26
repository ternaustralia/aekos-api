'use strict'
let r = require('./response-helper')
let speciesDataJson = require('./speciesData-json')
let allSpeciesDataCsv = require('./allSpeciesData-csv')

module.exports.handler = (event, context, callback) => {
  let db = require('./db-helper')
  doHandle(event, callback, db, r.calculateElapsedTime)
}

function doHandle (event, callback, db, elapsedTimeCalculator) {
  let csvHeaders = allSpeciesDataCsv.getCsvHeadersForRequestedVersion(event)
  let processStart = r.now()
  let params = speciesDataJson.extractParams(event, db)
  speciesDataJson.doQuery(event, params, processStart, false, db, elapsedTimeCalculator).then(successResult => {
    let result = allSpeciesDataCsv.mapJsonToCsv(successResult.response, csvHeaders)
    let downloadFileName = allSpeciesDataCsv.getCsvDownloadFileName(event, 'Species')
    r.csv.ok(callback, result, downloadFileName)
  }).catch(error => {
    console.error('Failed while building result', error)
    r.json.internalServerError(callback)
  })
}

module.exports._testonly = {
  doHandle: doHandle
}

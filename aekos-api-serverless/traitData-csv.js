'use strict'
let quoted = require('./FieldConfig').quoted
let r = require('./response-helper')
let traitDataJson = require('./traitData-json')
let allSpeciesDataCsv = require('./allSpeciesData-csv')
let v1CsvHeaders = allSpeciesDataCsv.v1CsvHeaders
let v2CsvHeaders = allSpeciesDataCsv.v2CsvHeaders
module.exports.v1CsvHeaders = v1CsvHeaders
module.exports.v2CsvHeaders = v2CsvHeaders

module.exports.handler = (event, context, callback) => {
  let db = require('./db-helper')
  doHandle(event, callback, db, r.calculateElapsedTime)
}

const validator = r.compositeValidator([
  traitDataJson.validator,
  r.downloadParamValidator
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
    traitDataJson.responder(requestBody, db, queryStringParameters, extrasProvider).then(successResult => {
      let result = mapJsonToCsv(successResult.response, csvHeaders)
      let downloadFileName = allSpeciesDataCsv.getCsvDownloadFileName(extrasProvider.event, 'Trait')
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

module.exports.mapJsonToCsv = mapJsonToCsv
function mapJsonToCsv (records, csvHeaders) {
  let maxTraitCount = 0
  let dataRows = records.reduce((prev, curr) => {
    maxTraitCount = Math.max(maxTraitCount, curr.traits.length)
    if (prev === '') {
      return createCsvRow(curr, csvHeaders)
    }
    return prev + '\n' + createCsvRow(curr, csvHeaders)
  }, '')
  let headerRow = getCsvHeaderRow(maxTraitCount, csvHeaders)
  return headerRow + '\n' + dataRows
}

module.exports.createCsvRow = createCsvRow
function createCsvRow (record, csvHeaders) {
  let result = allSpeciesDataCsv.createCsvRow(csvHeaders, record)
  for (let i = 0; i < record.traits.length; i++) {
    let curr = record.traits[i]
    result += `,"${curr.traitName}","${curr.traitValue}",`
    if (curr.traitUnit === null) {
      continue
    }
    result += `"${curr.traitUnit}"`
  }
  return result
}

module.exports.getCsvHeaderRow = getCsvHeaderRow
function getCsvHeaderRow (traitCount, csvHeaders) {
  let traitHeaders = []
  for (let i = 1; i <= traitCount; i++) {
    traitHeaders.push(quoted(`trait${i}Name`))
    traitHeaders.push(quoted(`trait${i}Value`))
    traitHeaders.push(quoted(`trait${i}Units`))
  }
  let allHeaders = csvHeaders.concat(traitHeaders)
  return allHeaders.reduce((prev, curr) => {
    if (prev === '') {
      return `"${curr.name}"`
    }
    return `${prev},"${curr.name}"`
  }, '')
}

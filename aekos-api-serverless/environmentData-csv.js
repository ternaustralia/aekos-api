'use strict'
let quoted = require('./FieldConfig').quoted
let notQuoted = require('./FieldConfig').notQuoted
let quotedListConcat = require('./FieldConfig').quotedListConcat
let r = require('./response-helper')
let envDataJson = require('./environmentData-json')
let allSpeciesDataCsv = require('./allSpeciesData-csv')
let v1CsvHeaders = [
  notQuoted('decimalLatitude'),
  notQuoted('decimalLongitude'),
  quoted('geodeticDatum'),
  quoted('locationID'),
  quotedListConcat('scientificNames'),
  quotedListConcat('taxonRemarks'),
  quoted('eventDate'),
  notQuoted('year'),
  notQuoted('month'),
  quoted('bibliographicCitation'),
  quoted('samplingProtocol')
]
module.exports.v1CsvHeaders = v1CsvHeaders
let v2CsvHeaders = [
  notQuoted('decimalLatitude'),
  notQuoted('decimalLongitude'),
  quoted('geodeticDatum'),
  quoted('locationID'),
  quoted('locationName'),
  quoted('datasetName'),
  quotedListConcat('scientificNames'),
  quotedListConcat('taxonRemarks'),
  quoted('eventDate'),
  notQuoted('year'),
  notQuoted('month'),
  quoted('bibliographicCitation'),
  quoted('samplingProtocol')
]
module.exports.v2CsvHeaders = v2CsvHeaders

module.exports.handler = (event, context, callback) => {
  let db = require('./db-helper')
  doHandle(event, callback, db, r.calculateElapsedTime)
}

const validator = r.compositeValidator([
  envDataJson.validator,
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
    let csvHeaders = getCsvHeadersForRequestedVersion(extrasProvider.event)
    envDataJson.responder(requestBody, db, queryStringParameters, extrasProvider).then(wrapper => {
      let successResult = wrapper.body
      let result = mapJsonToCsv(successResult.response, csvHeaders)
      let downloadFileName = allSpeciesDataCsv.getCsvDownloadFileName(extrasProvider.event, 'Environment')
      resolve({
        body: result,
        downloadFileName: downloadFileName,
        linkHeaderData: wrapper.linkHeaderData
      })
    }).catch(reject)
  })
}

module.exports._testonly = {
  doHandle: doHandle
}

function getCsvHeadersForRequestedVersion (event) {
  let versionHandler = r.newVersionHandler({
    '/v1/': v1CsvHeaders,
    '/v2/': v2CsvHeaders
  })
  return versionHandler.handle(event)
}

module.exports.mapJsonToCsv = mapJsonToCsv
function mapJsonToCsv (records, csvHeaders) {
  let maxVariableCount = 0
  let dataRows = records.reduce((prev, curr) => {
    maxVariableCount = Math.max(maxVariableCount, curr.variables.length)
    if (prev === '') {
      return createCsvRow(curr, csvHeaders)
    }
    return prev + '\n' + createCsvRow(curr, csvHeaders)
  }, '')
  let headerRow = getCsvHeaderRow(maxVariableCount, csvHeaders)
  return headerRow + '\n' + dataRows
}

module.exports.createCsvRow = createCsvRow
function createCsvRow (record, csvHeaders) {
  let result = ''
  for (let i = 0; i < csvHeaders.length; i++) {
    let currHeaderDef = csvHeaders[i]
    if (result.length > 0) {
      result += ','
    }
    result += currHeaderDef.getValue(record)
  }
  for (let i = 0; i < record.variables.length; i++) {
    let curr = record.variables[i]
    result += `,"${curr.varName}","${curr.varValue}",`
    if (curr.varUnit === null || typeof curr.varUnit === 'undefined') {
      continue
    }
    result += `"${curr.varUnit}"`
  }
  return result
}

module.exports.getCsvHeaderRow = getCsvHeaderRow
function getCsvHeaderRow (varCount, csvHeaders) {
  let varHeaders = []
  for (let i = 1; i <= varCount; i++) {
    varHeaders.push(quoted(`variable${i}Name`))
    varHeaders.push(quoted(`variable${i}Value`))
    varHeaders.push(quoted(`variable${i}Units`))
  }
  let allHeaders = csvHeaders.concat(varHeaders)
  return allHeaders.reduce((prev, curr) => {
    if (prev === '') {
      return `"${curr.name}"`
    }
    return `${prev},"${curr.name}"`
  }, '')
}

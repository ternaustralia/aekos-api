'use strict'
let quoted = require('./FieldConfig').quoted
let notQuoted = require('./FieldConfig').notQuoted
let r = require('./response-helper')
let allSpeciesDataJson = require('./allSpeciesData-json')
let v1CsvHeaders = [
  notQuoted('decimalLatitude'),
  notQuoted('decimalLongitude'),
  quoted('geodeticDatum'),
  quoted('locationID'),
  quoted('scientificName'),
  quoted('taxonRemarks'),
  notQuoted('individualCount'),
  quoted('eventDate'),
  notQuoted('year'),
  notQuoted('month'),
  quoted('bibliographicCitation'),
  quoted('samplingProtocol')
]
let v2CsvHeaders = [
  notQuoted('decimalLatitude'),
  notQuoted('decimalLongitude'),
  quoted('geodeticDatum'),
  quoted('locationID'),
  quoted('locationName'),
  quoted('datasetName'),
  quoted('scientificName'),
  quoted('taxonRemarks'),
  notQuoted('individualCount'),
  quoted('eventDate'),
  notQuoted('year'),
  notQuoted('month'),
  quoted('bibliographicCitation'),
  quoted('samplingProtocol')
]
let yaml = require('yamljs')
const downloadParam = yaml.load('./constants.yml').paramNames.DOWNLOAD
module.exports.v1CsvHeaders = v1CsvHeaders
module.exports.v2CsvHeaders = v2CsvHeaders

module.exports.handler = (event, context, callback) => {
  let db = require('./db-helper')
  doHandle(event, callback, db, r.calculateElapsedTime)
}

const validator = r.compositeValidator([
  allSpeciesDataJson.validator,
  r.downloadParamValidator
])

function doHandle (event, callback, db, elapsedTimeCalculator) {
  r.handleCsvGet(event, callback, db, validator, responder, {
    event: event,
    elapsedTimeCalculator: elapsedTimeCalculator
  })
}

function responder (_, db, queryStringParameters, extrasProvider) {
  return new Promise((resolve, reject) => {
    let csvHeaders = getCsvHeadersForRequestedVersion(extrasProvider.event)
    allSpeciesDataJson.responder(db, queryStringParameters, extrasProvider).then(successResult => {
      let result = mapJsonToCsv(successResult.response, csvHeaders)
      let downloadFileName = getCsvDownloadFileName(extrasProvider.event, 'Species')
      resolve({
        body: result,
        downloadFileName: downloadFileName,
        jsonResponse: successResult
      })
    }).catch(reject)
  })
}

module.exports.getCsvHeadersForRequestedVersion = getCsvHeadersForRequestedVersion
function getCsvHeadersForRequestedVersion (event) {
  let versionHandler = r.newVersionHandler({
    '/v1/': v1CsvHeaders,
    '/v2/': v2CsvHeaders
  })
  return versionHandler.handle(event)
}

module.exports._testonly = {
  doHandle: doHandle,
  getCsvHeaderRow: getCsvHeaderRow,
  validator: validator
}

module.exports.getCsvDownloadFileName = getCsvDownloadFileName
function getCsvDownloadFileName (event, nameFragment) {
  let params = event.queryStringParameters
  if (params === null) {
    return null
  }
  let downloadParamValue = params[downloadParam]
  if (typeof downloadParamValue === 'undefined' || typeof downloadParamValue !== 'string') {
    return null
  }
  if (downloadParamValue.toLowerCase() === 'true') {
    return 'aekos' + nameFragment + 'Data.csv'
  }
  return null
}

module.exports.mapJsonToCsv = mapJsonToCsv
function mapJsonToCsv (records, csvHeaders) {
  let headerRow = getCsvHeaderRow(csvHeaders)
  let dataRows = records.reduce((prev, curr) => {
    if (prev === '') {
      return createCsvRow(csvHeaders, curr)
    }
    return prev + '\n' + createCsvRow(csvHeaders, curr)
  }, '')
  return headerRow + '\n' + dataRows
}

module.exports.createCsvRow = createCsvRow
function createCsvRow (csvHeadersParam, record) {
  let result = ''
  for (let i = 0; i < csvHeadersParam.length; i++) {
    let currHeaderDef = csvHeadersParam[i]
    if (result.length > 0) {
      result += ','
    }
    result += currHeaderDef.getValue(record)
  }
  return result
}

function getCsvHeaderRow (csvHeaders) {
  return csvHeaders.reduce((prev, curr) => {
    if (prev === '') {
      return `"${curr.name}"`
    }
    return `${prev},"${curr.name}"`
  }, '')
}

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

function doHandle (event, callback, db, elapsedTimeCalculator) {
  let csvHeaders = getCsvHeadersForRequestedVersion(event)
  let processStart = r.now()
  let params = allSpeciesDataJson.extractParams(event, db)
  allSpeciesDataJson.doAllSpeciesQuery(event, params, processStart, db, elapsedTimeCalculator).then(successResult => {
    let result = mapJsonToCsv(successResult.response, csvHeaders)
    let downloadFileName = getCsvDownloadFileName(event)
    r.csv.ok(callback, result, downloadFileName)
  }).catch(error => {
    console.error('Failed while building result', error)
    r.json.internalServerError(callback)
  })
}

function getCsvHeadersForRequestedVersion (event) {
  let path = event.requestContext.path
  const mapping = {
    '/v1/allSpeciesData.csv': v1CsvHeaders,
    '/v2/allSpeciesData.csv': v2CsvHeaders
  }
  let result = mapping[path]
  if (typeof result === 'undefined') {
    throw new Error(`Programmer problem: unhandled path '${path}'`)
  }
  return result
}

module.exports._testonly = {
  doHandle: doHandle,
  getCsvHeaderRow: getCsvHeaderRow
}

module.exports.getCsvDownloadFileName = getCsvDownloadFileName
function getCsvDownloadFileName (event) {
  let params = event.queryStringParameters
  if (params === null) {
    return null
  }
  let downloadParamValue = params[downloadParam]
  if (typeof downloadParamValue === 'undefined') {
    return null
  }
  if (downloadParamValue === 'true') {
    return 'aekosSpeciesData.csv'
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

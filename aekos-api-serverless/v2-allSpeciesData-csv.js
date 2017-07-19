'use strict'
let quoted = require('./FieldConfig').quoted
let notQuoted = require('./FieldConfig').notQuoted
let r = require('./response-helper')
let allSpeciesDataJson = require('./v2-allSpeciesData-json')
let csvHeaders = [
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
module.exports.csvHeaders = csvHeaders

module.exports.handler = (event, context, callback) => {
  let db = require('./db-helper')
  doHandle(event, callback, db, r.calculateElapsedTime)
}

function doHandle (event, callback, db, elapsedTimeCalculator) {
  let processStart = r.now()
  let params = allSpeciesDataJson.extractParams(event, db)
  allSpeciesDataJson.doAllSpeciesQuery(params, processStart, db, elapsedTimeCalculator).then(successResult => {
    let result = mapJsonToCsv(successResult.response)
    let downloadFileName = getCsvDownloadFileName(event)
    r.csv.ok(callback, result, downloadFileName)
  }).catch(error => {
    console.error('Failed while building result', error)
    r.json.internalServerError(callback)
  })
}

module.exports._testonly = {
  doHandle: doHandle
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
function mapJsonToCsv (records) {
  let headerRow = getCsvHeaderRow()
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

module.exports.getCsvHeaderRow = getCsvHeaderRow
function getCsvHeaderRow () {
  return csvHeaders.reduce((prev, curr) => {
    if (prev === '') {
      return `"${curr.name}"`
    }
    return `${prev},"${curr.name}"`
  }, '')
}

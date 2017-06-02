'use strict'
let quoted = require('./FieldConfig').quoted
let notQuoted = require('./FieldConfig').notQuoted
let r = require('./response-helper')
let speciesDataJson = require('./v1-speciesData-json')
let csvHeaders = [
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
module.exports.csvHeaders = csvHeaders

module.exports.handler = (event, context, callback) => {
  let processStart = r.now()
  let params = speciesDataJson.extractParams(event)
  speciesDataJson.getSpeciesData(params, processStart).then(successResult => {
    let result = mapJsonToCsv(successResult.response)
    r.csv.ok(callback, result)
  }).catch(error => {
    console.error('Failed while building result', error)
    r.json.internalServerError(callback, 'Sorry, something went wrong')
  })
}

function mapJsonToCsv (records) {
  let headerRow = getCsvHeaderRow()
  let dataRows = records.reduce((prev, curr) => {
    if (prev === '') {
      return createCsvRow(curr)
    }
    return prev + '\n' + createCsvRow(curr)
  }, '')
  return headerRow + '\n' + dataRows
}

module.exports.createCsvRow = createCsvRow
function createCsvRow (csvHeadersParam, record) {
  let result = ''
  for (let i = 0; i < csvHeadersParam.length; i++) {
    let curr = csvHeadersParam[i]
    if (result.length > 0) {
      result += ','
    }
    if (curr.isQuoted) {
      let value = record[curr.name]
      if (value === null) {
        continue
      }
      result += `"${value}"`
      continue
    }
    result += record[curr.name]
  }
  return result
}

function getCsvHeaderRow () {
  return csvHeaders.reduce((prev, curr) => {
    if (prev === '') {
      return `"${curr.name}"`
    }
    return `${prev},"${curr.name}"`
  }, '')
}

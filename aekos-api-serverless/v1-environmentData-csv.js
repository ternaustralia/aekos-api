'use strict'
let quoted = require('./FieldConfig').quoted
let notQuoted = require('./FieldConfig').notQuoted
let quotedListConcat = require('./FieldConfig').quotedListConcat
let r = require('./response-helper')
let envDataJson = require('./v1-environmentData-json')
let csvHeaders = [
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

module.exports.handler = (event, context, callback) => {
  let processStart = r.now()
  let params = envDataJson.extractParams(event)
  const dontIncludeSpeciesRecordId = false
  envDataJson.doQuery(params, processStart, dontIncludeSpeciesRecordId).then(successResult => {
    let result = mapJsonToCsv(successResult.response)
    r.csv.ok(callback, result)
  }).catch(error => {
    console.error('Failed while building result', error)
    r.json.internalServerError(callback)
  })
}

module.exports.mapJsonToCsv = mapJsonToCsv
function mapJsonToCsv (records) {
  let maxVariableCount = 0
  let dataRows = records.reduce((prev, curr) => {
    maxVariableCount = Math.max(maxVariableCount, curr.variables.length)
    if (prev === '') {
      return createCsvRow(curr)
    }
    return prev + '\n' + createCsvRow(curr)
  }, '')
  let headerRow = getCsvHeaderRow(maxVariableCount)
  return headerRow + '\n' + dataRows
}

module.exports.createCsvRow = createCsvRow
function createCsvRow (record) {
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
    if (curr.varUnit === null) { // FIXME are they null or undefined?
      continue
    }
    result += `"${curr.varUnit}"`
  }
  return result
}

module.exports.getCsvHeaderRow = getCsvHeaderRow
function getCsvHeaderRow (varCount) {
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

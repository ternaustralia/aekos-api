'use strict'
let quoted = require('./FieldConfig').quoted
let notQuoted = require('./FieldConfig').notQuoted
let r = require('./response-helper')
let traitDataJson = require('./v10-traitData-json')
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
  // ,"traitNName", "traitNValue", "traitNUnits"...
]

module.exports.handler = (event, context, callback) => {
  let processStart = r.now()
  let params = traitDataJson.extractParams(event)
  traitDataJson.getTratiData(params, processStart).then(successResult => {
    let result = mapJsonToCsv(successResult.response)
    r.csv.ok(callback, result)
  }).catch(error => {
    console.error('Failed while building result', error)
    r.json.internalServerError(callback, 'Sorry, something went wrong')
  })
}

module.exports.mapJsonToCsv = mapJsonToCsv
function mapJsonToCsv (records) {
  let maxTraitCount = 0
  let dataRows = records.reduce((prev, curr) => {
    maxTraitCount = Math.max(maxTraitCount, curr.traits.length)
    if (prev === '') {
      return createCsvRow(curr)
    }
    return prev + '\n' + createCsvRow(curr)
  }, '')
  let headerRow = getCsvHeaderRow(maxTraitCount)
  return headerRow + '\n' + dataRows
}

module.exports.createCsvRow = createCsvRow
function createCsvRow (record) {
  let result = ''
  for (let i = 0; i < csvHeaders.length; i++) {
    let curr = csvHeaders[i]
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
  for (let i = 0; i < record.traits.length; i++) {
    let curr = record.traits[i]
    result += `,"${curr.traitName}","${curr.traitValue}",`
    if (curr.traitUnit === null) { // FIXME are they null or undefined?
      continue
    }
    result += `"${curr.traitUnit}"`
  }
  return result
}

module.exports.getCsvHeaderRow = getCsvHeaderRow
function getCsvHeaderRow (traitCount) {
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

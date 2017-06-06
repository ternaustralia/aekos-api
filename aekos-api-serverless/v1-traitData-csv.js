'use strict'
let quoted = require('./FieldConfig').quoted
let r = require('./response-helper')
let traitDataJson = require('./v1-traitData-json')
let speciesDataCsv = require('./v1-speciesData-csv')
let csvHeaders = speciesDataCsv.csvHeaders

module.exports.handler = (event, context, callback) => {
  let db = require('./db-helper')
  doHandle(event, callback, db, r.calculateElapsedTime)
}

function doHandle (event, callback, db, elapsedTimeCalculator) {
  let processStart = r.now()
  let params = traitDataJson.extractParams(event, db)
  traitDataJson.getTraitData(params, processStart, db, elapsedTimeCalculator).then(successResult => {
    let result = mapJsonToCsv(successResult.response)
    r.csv.ok(callback, result)
  }).catch(error => {
    console.error('Failed while building result', error)
    r.json.internalServerError(callback, 'Sorry, something went wrong')
  })
}

module.exports._testonly = {
  doHandle: doHandle
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
  let result = speciesDataCsv.createCsvRow(csvHeaders, record)
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

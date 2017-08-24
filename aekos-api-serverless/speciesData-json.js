'use strict'
let r = require('./response-helper')
let yaml = require('yamljs')
let allSpeciesDataJson = require('./allSpeciesData-json')
const speciesNamesParam = yaml.load('./constants.yml').paramNames.speciesName.multiple

const validator = r.compositeValidator([
  allSpeciesDataJson.validator,
  r.speciesNamesValidator
])
module.exports.validator = validator

module.exports.doHandle = doHandle
function doHandle (event, callback, db, elapsedTimeCalculator) {
  r.handleJsonPost(event, callback, db, validator, responder, {
    event: event,
    elapsedTimeCalculator: elapsedTimeCalculator
  })
}

module.exports.responder = responder
function responder (requestBody, db, queryStringParameters, extrasProvider) {
  let processStart = r.now()
  let params = extractParams(requestBody, queryStringParameters, db)
  return doQuery(extrasProvider.event, params, processStart, db, extrasProvider.elapsedTimeCalculator).then(result => {
    return Promise.resolve(r.toLinkHeaderDataAndResponseObj(result))
  })
}

module.exports.doQuery = doQuery
function doQuery (event, params, processStart, db, elapsedTimeCalculator) {
  const recordsSql = getRecordsSql(params.speciesNames, params.start, params.rows)
  const countSql = getCountSql(params.speciesNames)
  return allSpeciesDataJson.doQuery(event, params, processStart, db, elapsedTimeCalculator, recordsSql, countSql).then(successResult => {
    successResult.responseHeader.elapsedTime = elapsedTimeCalculator(processStart)
    successResult.responseHeader.params[speciesNamesParam] = params.unescapedSpeciesNames
    return successResult
  })
}

module.exports._testonly = {
  getRecordsSql: getRecordsSql,
  getCountSql: getCountSql
}

function getRecordsSql (escapedSpeciesName, start, rows) {
  r.assertIsSupplied(escapedSpeciesName)
  let whereFragment = buildWhereFragment(escapedSpeciesName)
  return allSpeciesDataJson.getRecordsSql(start, rows, whereFragment)
}

function getCountSql (escapedSpeciesName) {
  let whereFragment = buildWhereFragment(escapedSpeciesName)
  return allSpeciesDataJson.getCountSql(whereFragment)
}

function buildWhereFragment (escapedSpeciesName) {
  return `
      WHERE (
        scientificName IN (${escapedSpeciesName})
        OR taxonRemarks IN (${escapedSpeciesName})
      )`
}

module.exports.extractParams = extractParams
function extractParams (requestBody, queryStringParameters, db) {
  let allSpeciesParams = allSpeciesDataJson.extractParams(queryStringParameters)
  let speciesNames = r.getOptionalArray(requestBody, speciesNamesParam, db)
  return new Params(speciesNames.escaped, speciesNames.unescaped, allSpeciesParams.start, allSpeciesParams.rows)
}

class Params {
  constructor (
    speciesNames,
    unescapedSpeciesNames,
    start,
    rows
  ) {
    this.speciesNames = speciesNames
    this.unescapedSpeciesNames = unescapedSpeciesNames
    this.start = start
    this.rows = rows
  }
}

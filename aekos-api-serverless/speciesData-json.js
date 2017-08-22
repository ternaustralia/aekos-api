'use strict'
let r = require('./response-helper')
let yaml = require('yamljs')
let allSpeciesDataJson = require('./allSpeciesData-json')
const speciesNamesParam = yaml.load('./constants.yml').paramNames.speciesName.multiple

module.exports.handler = (event, context, callback) => {
  let db = require('./db-helper')
  doHandle(event, callback, db, r.calculateElapsedTime)
}

const validator = r.compositeValidator([
  allSpeciesDataJson.validator,
  r.speciesNamesValidator
])
module.exports.validator = validator

function doHandle (event, callback, db, elapsedTimeCalculator) {
  r.handleJsonPost(event, callback, db, validator, responder, {
    event: event,
    elapsedTimeCalculator: elapsedTimeCalculator
  })
}

module.exports.responder = responder
function responder (requestBody, db, queryStringParameters, extrasProvider) {
  return new Promise((resolve, reject) => {
    try {
      let processStart = r.now()
      let params = extractParams(requestBody, queryStringParameters, db)
      doQuery(extrasProvider.event, params, processStart, db, extrasProvider.elapsedTimeCalculator).then(result => {
        resolve(r.toLinkHeaderDataAndResponseObj(result))
      })
    } catch (error) {
      reject(error)
    }
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
  getCountSql: getCountSql,
  doHandle: doHandle
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

'use strict'
let r = require('./response-helper')
let yaml = require('yamljs')
let allSpeciesDataJson = require('./v1-allSpeciesData-json')
const speciesNameParam = yaml.load('./constants.yml').paramNames.SINGLE_SPECIES_NAME

module.exports.handler = (event, context, callback) => {
  let db = require('./db-helper')
  doHandle(event, callback, db, r.calculateElapsedTime)
}

function doHandle (event, callback, db, elapsedTimeCalculator) {
  let processStart = r.now()
  if (!r.isQueryStringParamPresent(event, speciesNameParam)) {
    r.json.badRequest(callback, `the '${speciesNameParam}' query string parameter must be supplied`)
    return
  }
  let params = extractParams(event, db) // FIXME handle thrown Errors for invalid request
  doQuery(params, processStart, false, db, elapsedTimeCalculator).then(successResult => {
    r.json.ok(callback, successResult)
  }).catch(error => {
    console.error('Failed to get speciesData', error)
    r.json.internalServerError(callback, 'Sorry, something went wrong')
  })
}

module.exports.doQuery = doQuery
function doQuery (params, processStart, includeSpeciesRecordId, db, elapsedTimeCalculator) {
  const recordsSql = getRecordsSql(params.speciesName, params.start, params.rows, includeSpeciesRecordId)
  const countSql = getCountSql(params.speciesName)
  return allSpeciesDataJson.doQuery(params, processStart, db, elapsedTimeCalculator, recordsSql, countSql).then(successResult => {
    successResult.responseHeader.elapsedTime = elapsedTimeCalculator(processStart)
    successResult.responseHeader.params[speciesNameParam] = params.unescapedSpeciesName // FIXME change to support array
    return successResult
  })
}

module.exports._testonly = {
  getRecordsSql: getRecordsSql,
  doHandle: doHandle
}

function getRecordsSql (escapedSpeciesName, start, rows, includeSpeciesRecordId) {
  r.assertIsSupplied(escapedSpeciesName)
  let whereFragment = `
    WHERE (
      s.scientificName IN (${escapedSpeciesName})
      OR s.taxonRemarks IN (${escapedSpeciesName})
    )`
  return allSpeciesDataJson.getRecordsSql(start, rows, includeSpeciesRecordId, whereFragment)
}

function getCountSql (escapedSpeciesName) {
  let whereFragment = `
    WHERE (
      s.scientificName IN (${escapedSpeciesName})
      OR s.taxonRemarks IN (${escapedSpeciesName})
    )`
  return allSpeciesDataJson.getCountSql(whereFragment)
}

module.exports.extractParams = extractParams
function extractParams (event, db) {
  let allSpeciesParams = allSpeciesDataJson.extractParams(event)
  let unescapedSpeciesName = event.queryStringParameters[speciesNameParam] // FIXME handle escaping a list when we can get multiple names
  let escapedSpeciesName = db.escape(unescapedSpeciesName)
  return new Params(escapedSpeciesName, unescapedSpeciesName, allSpeciesParams.start, allSpeciesParams.rows)
}

class Params {
  constructor (
    speciesName,
    unescapedSpeciesName,
    start,
    rows
  ) {
    this.speciesName = speciesName
    this.unescapedSpeciesName = unescapedSpeciesName
    this.start = start
    this.rows = rows
  }
}

'use strict'
let r = require('./response-helper')
let speciesDataJson = require('./speciesData-json')
let latches = require('latches')
let yaml = require('yamljs')
const traitNamesParam = yaml.load('./constants.yml').paramNames.traitName.multiple

module.exports.handler = (event, context, callback) => {
  let db = require('./db-helper')
  doHandle(event, callback, db, r.calculateElapsedTime)
}

function doHandle (event, callback, db, elapsedTimeCalculator) {
  r.handleJsonPost(event, callback, db, validator, responder, {
    event: event,
    elapsedTimeCalculator: elapsedTimeCalculator
  })
}

const validator = r.compositeValidator([
  speciesDataJson.validator
  // TODO add trait validator
])
module.exports.validator = validator

module.exports.responder = responder
function responder (requestBody, db, queryStringParameters, extrasProvider) {
  let processStart = r.now()
  let params = extractParams(requestBody, queryStringParameters, db)
  return getTraitData(extrasProvider.event, params, processStart, db, extrasProvider.elapsedTimeCalculator)
}

module.exports.getTraitData = getTraitData
function getTraitData (event, params, processStart, db, elapsedTimeCalculator) {
  return speciesDataJson.doQuery(event, params, processStart, true, db, elapsedTimeCalculator).then(successResult => {
    return enrichWithTraitData(successResult, params.traitNames, db)
  }).then(successResultWithTraits => {
    successResultWithTraits.responseHeader.elapsedTime = elapsedTimeCalculator(processStart)
    successResultWithTraits.responseHeader.params[traitNamesParam] = params.unescapedTraitNames
    return successResultWithTraits
  })
}

function enrichWithTraitData (successResult, traitNames, db) {
  let speciesRecords = successResult.response
  return new Promise((resolve, reject) => {
    let cdl = new latches.CountDownLatch(speciesRecords.length)
    cdl.wait(function () {
      resolve(successResult)
    })
    speciesRecords.forEach(curr => {
      const traitSql = getTraitSql(curr.id, traitNames)
      db.execSelectPromise(traitSql).then(traitRecords => {
        curr.traits = traitRecords
        delete (curr.id)
        cdl.hit()
      }).catch(error => {
        reject(new Error(`DB problem: failed while adding traits to species.id='${curr.id}' with error=${JSON.stringify(error)}`))
      })
    })
  })
}

module.exports._testonly = {
  getTraitSql: getTraitSql,
  doHandle: doHandle
}

function getTraitSql (parentId, traitNames) {
  r.assertIsSupplied(parentId)
  let traitFilterFragment = ''
  if (traitNames) {
    traitFilterFragment = `AND traitName in (${traitNames})`
  }
  return `
    SELECT
    traitName,
    traitValue,
    traitUnit
    FROM traits
    WHERE parentId = '${parentId}'
    ${traitFilterFragment};`
}

module.exports.extractParams = extractParams
function extractParams (requestBody, queryStringParameters, db) {
  let speciesParams = speciesDataJson.extractParams(requestBody, queryStringParameters, db)
  let traitNames = r.getOptionalArray(requestBody, traitNamesParam, db)
  return new Params(speciesParams.speciesNames, speciesParams.unescapedSpeciesNames,
    speciesParams.start, speciesParams.rows, traitNames.escaped, traitNames.unescaped)
}

class Params {
  constructor (
    speciesNames,
    unescapedSpeciesNames,
    start,
    rows,
    traitNames,
    unescapedTraitNames
  ) {
    this.speciesNames = speciesNames
    this.unescapedSpeciesNames = unescapedSpeciesNames
    this.start = start
    this.rows = rows
    this.traitNames = traitNames
    this.unescapedTraitNames = unescapedTraitNames
  }
}

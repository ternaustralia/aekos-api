'use strict'
let r = require('./response-helper')
let speciesDataJson = require('./speciesData-json')
let latches = require('latches')
let yaml = require('yamljs')
const speciesNameParam = yaml.load('./constants.yml').paramNames.SINGLE_SPECIES_NAME
const traitNameParam = yaml.load('./constants.yml').paramNames.SINGLE_TRAIT_NAME

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
  getTraitData(event, params, processStart, db, elapsedTimeCalculator).then(successResult => {
    r.json.ok(callback, successResult, event)
  }).catch(error => {
    console.error('Failed while building result', error)
    r.json.internalServerError(callback)
  })
}

module.exports.getTraitData = getTraitData
function getTraitData (event, params, processStart, db, elapsedTimeCalculator) {
  return speciesDataJson.doQuery(event, params, processStart, true, db, elapsedTimeCalculator).then(successResult => {
    return enrichWithTraitData(successResult, params.traitName, db)
  }).then(successResultWithTraits => {
    successResultWithTraits.responseHeader.elapsedTime = elapsedTimeCalculator(processStart)
    successResultWithTraits.responseHeader.params[traitNameParam] = params.unescapedTraitName // FIXME change to support array
    return successResultWithTraits
  })
}

function enrichWithTraitData (successResult, traitName, db) {
  let speciesRecords = successResult.response
  return new Promise((resolve, reject) => {
    let cdl = new latches.CountDownLatch(speciesRecords.length)
    cdl.wait(function () {
      resolve(successResult)
    })
    speciesRecords.forEach(curr => {
      const traitSql = getTraitSql(curr.id, traitName)
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

function getTraitSql (parentId, traitName) {
  r.assertIsSupplied(parentId)
  let traitFilterFragment = ''
  if (traitName) {
    traitFilterFragment = `AND traitName in (${traitName})`
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
function extractParams (event, db) {
  let speciesParams = speciesDataJson.extractParams(event, db)
  let unescapedTraitName = r.getOptionalStringParam(event, traitNameParam, null)
  let escapedTraitName = null
  if (unescapedTraitName) {
    escapedTraitName = db.escape(unescapedTraitName)
  }
  return new Params(speciesParams.speciesName, speciesParams.unescapedSpeciesName, speciesParams.start,
    speciesParams.rows, escapedTraitName, unescapedTraitName)
}

class Params {
  constructor (
    speciesName,
    unescapedSpeciesName,
    start,
    rows,
    traitName,
    unescapedTraitName
  ) {
    this.speciesName = speciesName
    this.unescapedSpeciesName = unescapedSpeciesName
    this.start = start
    this.rows = rows
    this.traitName = traitName
    this.unescapedTraitName = unescapedTraitName
  }
}

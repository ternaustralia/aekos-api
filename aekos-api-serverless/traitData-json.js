'use strict'
let r = require('./response-helper')
let allSpeciesDataJson = require('./allSpeciesData-json')
let speciesDataJson = require('./speciesData-json')
let yaml = require('yamljs')
const speciesNamesParam = yaml.load('./constants.yml').paramNames.speciesName.multiple
const traitNamesParam = yaml.load('./constants.yml').paramNames.traitName.multiple

module.exports.doHandle = doHandle
function doHandle (event, callback, db, elapsedTimeCalculator) {
  r.handleJsonPost(event, callback, db, validator, responder, {
    event: event,
    elapsedTimeCalculator: elapsedTimeCalculator
  })
}

const validator = r.compositeValidator([
  speciesDataJson.validator,
  r.traitNamesOptionalValidator
])
module.exports.validator = validator

module.exports.responder = responder
function responder (requestBody, db, queryStringParameters, extrasProvider) {
  return new Promise((resolve, reject) => {
    try {
      let processStart = r.now()
      let params = extractParams(requestBody, queryStringParameters, db)
      getTraitData(extrasProvider.event, params, processStart, db, extrasProvider.elapsedTimeCalculator).then(result => {
        resolve(r.toLinkHeaderDataAndResponseObj(result))
      })
    } catch (error) {
      reject(error)
    }
  })
}

module.exports.getTraitData = getTraitData
function getTraitData (event, params, processStart, db, elapsedTimeCalculator) {
  const recordsSql = getRecordsSql(params.speciesNames, params.start, params.rows, params.traitNames)
  const countSql = getCountSql(params.speciesNames, params.traitNames)
  return allSpeciesDataJson.doQuery(event, params, processStart, db, elapsedTimeCalculator, recordsSql, countSql).then(successResult => {
    successResult.responseHeader.elapsedTime = elapsedTimeCalculator(processStart)
    successResult.responseHeader.params[speciesNamesParam] = params.unescapedSpeciesNames
    successResult.responseHeader.params[traitNamesParam] = params.unescapedTraitNames
    let successResultWithTraits = rollupRecords(successResult)
    successResultWithTraits.responseHeader.elapsedTime = elapsedTimeCalculator(processStart)
    return successResultWithTraits
  })
}

module.exports._testonly = {
  getRecordsSql: getRecordsSql,
  getCountSql: getCountSql,
  rollupRecords: rollupRecords,
  doHandle: doHandle
}

/*
 * Transforms the response so we have one record with all of its traits.
 * It uses the 'key' field to determine record identity, grabs all the trait
 * fields and expects all other fields to be the same for each repeat of the record.
 */
function rollupRecords (responseObj) {
  const fieldNameKey = 'id'
  const fieldNamesTraits = ['traitName', 'traitValue', 'traitUnit']
  let keyManager = {}
  responseObj.response.forEach(currRawRecord => {
    let keyField = currRawRecord[fieldNameKey]
    if (typeof keyField === 'undefined') {
      throw new Error(`Data problem: record did not have the '${fieldNameKey}' field`)
    }
    if (typeof keyManager[keyField] === 'undefined') {
      let record = {
        traits: []
      }
      Object.keys(currRawRecord).forEach(currFieldName => {
        if (currFieldName === fieldNameKey) {
          return
        }
        if (fieldNamesTraits.indexOf(currFieldName) >= 0) {
          return
        }
        record[currFieldName] = currRawRecord[currFieldName]
      })
      keyManager[keyField] = record
    }
    let record = keyManager[keyField]
    let newTrait = {}
    fieldNamesTraits.forEach(currFieldName => {
      newTrait[currFieldName] = currRawRecord[currFieldName]
    })
    record.traits.push(newTrait)
  })
  let result = Object.keys(keyManager).reduce((previous, current) => {
    let value = keyManager[current]
    previous.push(value)
    return previous
  }, [])
  return {
    response: result,
    responseHeader: responseObj.responseHeader
  }
}

function getRecordsSql (escapedSpeciesName, start, rows, traitNames) {
  r.assertIsSupplied(escapedSpeciesName)
  let traitNamesFilterFragment = getTraitNamesFilterFragment(traitNames, 'AND')
  let whereFragment = buildWhereFragment(escapedSpeciesName, traitNames)
  let extraSelectFields = `,
    t.traitName,
    t.traitValue,
    t.traitUnit`
  let extraJoinFragment = `
    INNER JOIN traits AS t
    ON t.parentId = s.id
    ${traitNamesFilterFragment}`
  const yesIncludeSpeciesRecordId = true
  return allSpeciesDataJson.getRecordsSql(start, rows, whereFragment,
    extraSelectFields, extraJoinFragment, yesIncludeSpeciesRecordId)
}

function getCountSql (escapedSpeciesName, traitNames) {
  let whereFragment = buildWhereFragment(escapedSpeciesName, traitNames)
  return allSpeciesDataJson.getCountSql(whereFragment)
}

function buildWhereFragment (escapedSpeciesName, traitNames) {
  let traitNamesFilterFragment = getTraitNamesFilterFragment(traitNames, 'WHERE')
  return `
      WHERE id IN (
        SELECT parentId
        FROM traits AS t
        ${traitNamesFilterFragment}
      )
      AND (
        scientificName IN (${escapedSpeciesName})
        OR taxonRemarks IN (${escapedSpeciesName})
      )`
}

function getTraitNamesFilterFragment (escapedTraitNames, prefix) {
  if (!escapedTraitNames) {
    return '--'
  }
  return `${prefix} t.traitName IN (${escapedTraitNames})`
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

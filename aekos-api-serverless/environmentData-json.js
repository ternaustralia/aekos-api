'use strict'
let r = require('./response-helper')
let Set = require('collections/set')
let speciesDataJson = require('./speciesData-json')
let allSpeciesDataJson = require('./allSpeciesData-json')
let yaml = require('yamljs')
const speciesNamesParam = yaml.load('./constants.yml').paramNames.speciesName.multiple
const varNamesParam = yaml.load('./constants.yml').paramNames.varName.multiple
const recordsHeldField = 'recordsHeld'
const visitKeySeparator = '#'

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
  speciesDataJson.validator,
  r.envVarNamesOptionalValidator
])
module.exports.validator = validator

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
  const recordsSql = getRecordsSql(params.speciesNames, params.varNames, params.start, params.rows)
  const countSql = getCountSql(params.speciesNames, params.varNames)
  let recordsPromise = db.execSelectPromise(recordsSql)
  let countPromise = db.execSelectPromise(countSql)
  return Promise.all([recordsPromise, countPromise]).then(values => {
    let records = values[0]
    let count = values[1]
    if (count.length !== 1) {
      throw new Error('SQL result problem: result from count query did not have exactly one row. Result=' + JSON.stringify(count))
    }
    let numFound = count[0][recordsHeldField]
    let totalPages = r.calculateTotalPages(params.rows, numFound)
    let pageNumber = r.calculatePageNumber(params.start, numFound, totalPages)
    let rolledupRecords = rollupRecords(records)
    let partialResponseObj = {
      responseHeader: {
        elapsedTime: elapsedTimeCalculator(processStart),
        numFound: numFound,
        pageNumber: pageNumber,
        params: {
          rows: params.rows,
          start: params.start,
          [speciesNamesParam]: params.unescapedSpeciesNames,
          [varNamesParam]: params.unescapedVarNames
        },
        totalPages: totalPages
      },
      response: rolledupRecords
    }
    let strategyForVersion = getStrategyForVersion(event)
    strategyForVersion(partialResponseObj)
    return partialResponseObj
  }).then(partialResponseObj => {
    let visitKeys = partialResponseObj.response.map(e => {
      return e.visitKey
    })
    if (visitKeys.length === 0) {
      let noRecordsSoShortCircuit = true
      return [partialResponseObj, noRecordsSoShortCircuit]
    }
    let visitKeyClauses = getVisitKeyClauses(visitKeys)
    let passPartialResponseObjDownChainPromise = new Promise(resolve => {
      resolve(partialResponseObj)
    })
    let speciesNamesClauses = params.speciesNames
    let speciesNamesSql = getSpeciesNamesSql(visitKeyClauses, speciesNamesClauses)
    let speciesNamesPromise = db.execSelectPromise(speciesNamesSql)
    let continuePromiseChainProcessingPromise = new Promise(resolve => {
      resolve(false)
    })
    return Promise.all([passPartialResponseObjDownChainPromise, continuePromiseChainProcessingPromise,
      speciesNamesPromise])
  }).then(dataWrapper => {
    let partialResponseObj = dataWrapper[0]
    let isShortCircuitPromiseChain = dataWrapper[1]
    if (isShortCircuitPromiseChain) {
      return partialResponseObj
    }
    let speciesNamesRecords = dataWrapper[2]
    let speciesNamesLookup = speciesNamesRecords.reduce((prev, curr) => {
      let visitKey = curr.visitKey
      delete (curr.visitKey)
      if (typeof prev[visitKey] === 'undefined') {
        prev[visitKey] = {
          scientificNames: new Set(),
          taxonRemarks: new Set()
        }
      }
      if (curr.scientificName) {
        prev[visitKey].scientificNames.add(curr.scientificName)
      }
      if (curr.taxonRemarks) {
        prev[visitKey].taxonRemarks.add(curr.taxonRemarks)
      }
      return prev
    }, {})
    appendSpeciesNames(partialResponseObj.response, speciesNamesLookup)
    stripVisitKeys(partialResponseObj.response)
    return partialResponseObj
  })
}

function getStrategyForVersion (event) {
  const doNothing = () => {}
  let versionHandler = r.newVersionHandler({
    '/v1/': allSpeciesDataJson.removeV2FieldsFrom,
    '/v2/': doNothing
  })
  return versionHandler.handle(event)
}

module.exports._testonly = {
  doHandle: doHandle,
  appendSpeciesNames: appendSpeciesNames,
  stripVisitKeys: stripVisitKeys,
  getRecordsSql: getRecordsSql,
  getCountSql: getCountSql,
  getSpeciesNamesSql: getSpeciesNamesSql,
  getVisitKeyClauses: getVisitKeyClauses
}

/*
 * Transforms the response so we have one record with all of its vars.
 * It uses the 'key' field to determine record identity, grabs all the var
 * fields and expects all other fields to be the same for each repeat of the record.
 */
function rollupRecords (records) {
  const fieldNameKey = 'visitKey'
  const fieldNamesVars = [
    { name: 'varName', isMandatory: true },
    { name: 'varValue', isMandatory: true },
    { name: 'varUnit', isMandatory: false }]
  let keyManager = {}
  records.forEach(currRawRecord => {
    let keyField = currRawRecord[fieldNameKey]
    if (typeof keyField === 'undefined') {
      throw new Error(`Data problem: record did not have the '${fieldNameKey}' field`)
    }
    let isFirstAppearanceOfRecord = typeof keyManager[keyField] === 'undefined'
    if (isFirstAppearanceOfRecord) {
      let record = {
        variables: []
      }
      Object.keys(currRawRecord).forEach(currFieldName => {
        let isVariableDataField = fieldNamesVars.some(element => {
          return element.name === currFieldName
        })
        if (isVariableDataField) {
          return
        }
        record[currFieldName] = currRawRecord[currFieldName]
      })
      keyManager[keyField] = record
    }
    let record = keyManager[keyField]
    let newVar = {}
    for (let currField of fieldNamesVars) {
      let currFieldName = currField.name
      let newFieldValue = currRawRecord[currFieldName]
      let isNoValueDefined = typeof newFieldValue === 'undefined' || newFieldValue === null
      if (currField.isMandatory && isNoValueDefined) {
        break
      }
      newVar[currFieldName] = newFieldValue
    }
    let isNoVariables = Object.values(newVar).length === 0
    if (isNoVariables) {
      return
    }
    record.variables.push(newVar)
  })
  let result = Object.values(keyManager)
  return result
}

function appendSpeciesNames (records, speciesNameLookup) {
  records.forEach(e => {
    let visitKey = e.visitKey
    if (!speciesNameLookup[visitKey]) {
      e.scientificNames = []
      e.taxonRemarks = []
      return
    }
    e.scientificNames = speciesNameLookup[visitKey].scientificNames.toArray()
    e.taxonRemarks = speciesNameLookup[visitKey].taxonRemarks.toArray()
  })
}

function stripVisitKeys (records) {
  records.forEach(e => {
    delete (e.visitKey)
  })
}

function getRecordsSql (escapedSpeciesNames, escapedVarNames, start, rows) {
  r.assertIsSupplied(escapedSpeciesNames)
  let varFilterClause = buildVarFilterClause(escapedVarNames)
  let extraVarFilterClause = ''
  let isFilteringByVar = varFilterClause.length > 0
  if (isFilteringByVar) {
    extraVarFilterClause = `AND v.varName IN (${escapedVarNames})`
  }
  return `
    SELECT
    CONCAT(e.locationID, '${visitKeySeparator}', e.eventDate) AS visitKey,
    e.eventDate,
    e.\`month\`,
    e.\`year\`,
    e.decimalLatitude,
    e.decimalLongitude,
    e.geodeticDatum,
    e.locationID,
    e.locationName,
    e.samplingProtocol,
    c.bibliographicCitation,
    c.datasetName,
    v.varName,
    v.varValue,
    v.varUnit
    FROM (
      SELECT DISTINCT
      e.eventDate,
      e.locationID
      FROM species AS s
      INNER JOIN env AS e
      ON s.locationID = e.locationID
      AND s.eventDate = e.eventDate
      AND (
        s.scientificName IN (${escapedSpeciesNames})
        OR s.taxonRemarks IN (${escapedSpeciesNames})
      )${varFilterClause}
      ORDER BY 2,1
      LIMIT ${rows} OFFSET ${start}
    ) AS coreData
    INNER JOIN env AS e
    ON coreData.locationID = e.locationID
    AND coreData.eventDate = e.eventDate
    INNER JOIN citations AS c
    ON e.samplingProtocol = c.samplingProtocol
    LEFT JOIN envvars AS v
    ON coreData.locationID = v.locationID
    AND coreData.eventDate = v.eventDate
    ${extraVarFilterClause};`
}

function getCountSql (escapedSpeciesName, escapedVarNames) {
  r.assertIsSupplied(escapedSpeciesName)
  let varFilterClause = buildVarFilterClause(escapedVarNames)
  return `
    SELECT count(DISTINCT e.locationID, e.eventDate) AS ${recordsHeldField}
    FROM species AS s
    INNER JOIN env AS e
    ON s.locationID = e.locationID
    AND s.eventDate = e.eventDate
    AND (
      s.scientificName IN (${escapedSpeciesName})
      OR s.taxonRemarks IN (${escapedSpeciesName})
    )${varFilterClause}
    ;`
}

function buildVarFilterClause (escapedVarNames) {
  let result = ''
  if (escapedVarNames) {
    result = `
      INNER JOIN envvars AS v
      ON v.locationID = e.locationID
      AND v.eventDate = e.eventDate
      AND v.varName IN (${escapedVarNames})`
  }
  return result
}

function getSpeciesNamesSql (visitKeyClauses, speciesNamesClauses) {
  r.assertIsSupplied(visitKeyClauses)
  r.assertIsSupplied(speciesNamesClauses)
  // TODO do we need to filter by species that match the trait filter?
  return `
    SELECT DISTINCT
    CONCAT(locationID, '${visitKeySeparator}', eventDate) AS visitKey,
    scientificName,
    taxonRemarks
    FROM species
    WHERE (
      scientificName IN (${speciesNamesClauses})
      OR taxonRemarks IN (${speciesNamesClauses})
    )
    AND (locationID, eventDate) in (${visitKeyClauses})
    ORDER BY 1;`
}

function getVisitKeyClauses (visitKeys) {
  return visitKeys.reduce((prev, curr) => {
    let parts = curr.split(visitKeySeparator)
    let locationID = parts[0]
    let eventDate = parts[1]
    let fragment = `('${locationID}','${eventDate}')`
    if (prev === '') {
      return fragment
    }
    let formattingForEasierReading = '\n    '
    return prev + ',' + formattingForEasierReading + fragment
  }, '')
}

module.exports.extractParams = extractParams
function extractParams (requestBody, queryStringParameters, db) {
  let speciesParams = speciesDataJson.extractParams(requestBody, queryStringParameters, db)
  let varNames = r.getOptionalArray(requestBody, varNamesParam, db)
  return new Params(speciesParams.speciesNames, speciesParams.unescapedSpeciesNames, speciesParams.start,
    speciesParams.rows, varNames.escaped, varNames.unescaped)
}

class Params {
  constructor (
    speciesNames,
    unescapedSpeciesNames,
    start,
    rows,
    varNames,
    unescapedVarNames
  ) {
    this.speciesNames = speciesNames
    this.unescapedSpeciesNames = unescapedSpeciesNames
    this.start = start
    this.rows = rows
    this.varNames = varNames
    this.unescapedVarNames = unescapedVarNames
  }
}

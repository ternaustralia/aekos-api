'use strict'
let r = require('./response-helper')
let Set = require('collections/set')
let speciesDataJson = require('./v1-speciesData-json')
let yaml = require('yamljs')
const speciesNameParam = yaml.load('./constants.yml').paramNames.SINGLE_SPECIES_NAME
const varNameParam = yaml.load('./constants.yml').paramNames.SINGLE_VAR_NAME
const recordsHeldField = 'recordsHeld'
const visitKeySeparator = '#'

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
  let params = extractParams(event, db)
  doQuery(params, processStart, db, elapsedTimeCalculator).then(successResult => {
    r.json.ok(callback, successResult, event)
  }).catch(error => {
    console.error('Failed to get v2-environmentData', error)
    r.json.internalServerError(callback)
  })
}

module.exports.doQuery = doQuery
function doQuery (params, processStart, db, elapsedTimeCalculator) {
  const recordsSql = getRecordsSql(params.speciesName, params.start, params.rows)
  const countSql = getCountSql(params.speciesName)
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
    let partialResponseObj = {
      responseHeader: {
        elapsedTime: elapsedTimeCalculator(processStart),
        numFound: numFound,
        pageNumber: pageNumber,
        params: {
          rows: params.rows,
          start: params.start,
          [speciesNameParam]: params.unescapedSpeciesName,
          [varNameParam]: params.unescapedVarName
        },
        totalPages: totalPages
      },
      response: records
    }
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
    let varsSql = getVarsSql(visitKeyClauses)
    let varsPromise = db.execSelectPromise(varsSql)
    let speciesNamesClauses = params.speciesName // FIXME change to list of escaped names
    let speciesNamesSql = getSpeciesNamesSql(visitKeyClauses, speciesNamesClauses)
    let speciesNamesPromise = db.execSelectPromise(speciesNamesSql)
    let continuePromiseChainProcessingPromise = new Promise(resolve => {
      resolve(false)
    })
    return Promise.all([passPartialResponseObjDownChainPromise, continuePromiseChainProcessingPromise,
      varsPromise, speciesNamesPromise])
  }).then(dataWrapper => {
    let partialResponseObj = dataWrapper[0]
    let isShortCircuitPromiseChain = dataWrapper[1]
    if (isShortCircuitPromiseChain) {
      return partialResponseObj
    }
    let varsRecords = dataWrapper[2]
    let varsLookup = varsRecords.reduce((prev, curr) => {
      let visitKey = curr.visitKey
      delete (curr.visitKey)
      if (typeof prev[visitKey] === 'undefined') {
        prev[visitKey] = []
      }
      prev[visitKey].push(curr)
      return prev
    }, {})
    appendVars(partialResponseObj.response, varsLookup)
    let speciesNamesRecords = dataWrapper[3]
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

module.exports._testonly = {
  doHandle: doHandle,
  appendVars: appendVars,
  appendSpeciesNames: appendSpeciesNames,
  stripVisitKeys: stripVisitKeys,
  getRecordsSql: getRecordsSql,
  getCountSql: getCountSql,
  getVarsSql: getVarsSql,
  getSpeciesNamesSql: getSpeciesNamesSql,
  getVisitKeyClauses: getVisitKeyClauses
}

function appendVars (records, varsLookup) {
  records.forEach(e => {
    let visitKey = e.visitKey
    if (!varsLookup[visitKey]) {
      e.variables = []
      return
    }
    e.variables = varsLookup[visitKey]
  })
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

function getRecordsSql (escapedSpeciesName, start, rows) {
  r.assertIsSupplied(escapedSpeciesName)
  return `
    SELECT DISTINCT
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
    c.datasetName
    FROM species AS s
    INNER JOIN env AS e
    ON s.locationID = e.locationID
    AND s.eventDate = e.eventDate
    AND (
      s.scientificName IN (${escapedSpeciesName})
      OR s.taxonRemarks IN (${escapedSpeciesName})
    )
    INNER JOIN citations AS c
    ON e.samplingProtocol = c.samplingProtocol
    ORDER BY 1
    LIMIT ${rows} OFFSET ${start};`
}

function getCountSql (escapedSpeciesName) {
  r.assertIsSupplied(escapedSpeciesName)
  return `
    SELECT count(DISTINCT locationID, eventDate) as ${recordsHeldField}
    FROM species
    WHERE (
      scientificName IN (${escapedSpeciesName})
      OR taxonRemarks IN (${escapedSpeciesName})
    );`
}

function getVarsSql (visitKeyClauses) {
  r.assertIsSupplied(visitKeyClauses)
  return `
    SELECT
    CONCAT(locationID, '${visitKeySeparator}', eventDate) AS visitKey,
    varName,
    varValue,
    varUnit
    FROM envvars
    WHERE (locationID, eventDate) in (${visitKeyClauses})
    ORDER BY 1;`
}

function getSpeciesNamesSql (visitKeyClauses, speciesNamesClauses) {
  r.assertIsSupplied(visitKeyClauses)
  r.assertIsSupplied(speciesNamesClauses)
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
    return prev + '\n,' + fragment
  }, '')
}

module.exports.extractParams = extractParams
function extractParams (event, db) {
  let speciesParams = speciesDataJson.extractParams(event, db)
  let varName = null // FIXME get optional varName(s)
  let unescapedVarName = null // FIXME do escaping
  return new Params(speciesParams.speciesName, speciesParams.unescapedSpeciesName, speciesParams.start,
    speciesParams.rows, varName, unescapedVarName)
}

class Params {
  constructor (
    speciesName,
    unescapedSpeciesName,
    start,
    rows,
    varName,
    unescapedVarName
  ) {
    this.speciesName = speciesName
    this.unescapedSpeciesName = unescapedSpeciesName
    this.start = start
    this.rows = rows
    this.varName = varName
    this.unescapedVarName = unescapedVarName
  }
}

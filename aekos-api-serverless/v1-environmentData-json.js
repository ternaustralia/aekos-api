'use strict'
let r = require('./response-helper')
let db = require('./db-helper')
let Set = require('collections/set')
const speciesNameParam = 'speciesName'
const recordsHeldField = 'recordsHeld'

module.exports.handler = (event, context, callback) => {
  let processStart = r.now()
  // FIXME get repeated query string params mapping correctly rather than just the last one
  if (!r.isQueryStringParamPresent(event, speciesNameParam)) {
    r.json.badRequest(callback, `the '${speciesNameParam}' query string parameter must be supplied`)
    return
  }
  // FIXME handle escaping a list when we can get multiple names
  let speciesName = event.queryStringParameters[speciesNameParam]
  let escapedSpeciesName = db.escape(speciesName)
  let start = r.getOptionalParam(event, 'start', 0)
  let rows = r.getOptionalParam(event, 'rows', 20)
  doQuery(escapedSpeciesName, start, rows, processStart, false).then(successResult => {
    r.json.ok(callback, successResult)
  }).catch(error => {
    console.error('Failed to get environmentData', error)
    r.json.internalServerError(callback, 'Sorry, something went wrong')
  })
}

module.exports.doQuery = doQuery
function doQuery (escapedSpeciesName, start, rows, processStart, includeSpeciesRecordId) {
  const recordsSql = getRecordsSql(escapedSpeciesName, start, rows, includeSpeciesRecordId)
  const countSql = getCountSql(escapedSpeciesName)
  let pageNumber = -1 // TODO calc page number
  let totalPages = -1 // TODO calc total pages
  let recordsPromise = db.execSelectPromise(recordsSql)
  let countPromise = db.execSelectPromise(countSql)
  return Promise.all([recordsPromise, countPromise]).then(values => {
    let records = values[0]
    let count = values[1]
    if (count.length !== 1) {
      throw new Error('SQL result problem: result from count query did not have exactly one row. Result=' + JSON.stringify(count))
    }
    let partialResponseObj = {
      responseHeader: {
        elapsedTime: r.now() - processStart,
        numFound: count[0][recordsHeldField],
        pageNumber: pageNumber,
        params: {
          rows: rows,
          start: start
          // TODO add params
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
    let speciesNamesClauses = escapedSpeciesName // FIXME change to list of escaped names
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
  appendVars: appendVars,
  appendSpeciesNames: appendSpeciesNames,
  stripVisitKeys: stripVisitKeys
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

function getRecordsSql (escapedSpeciesName, start, rows, includeSpeciesRecordId) {
  return `
    SELECT DISTINCT
    CONCAT(e.locationID, '#', e.eventDate) AS visitKey,
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
  return `
    SELECT count(DISTINCT locationID, eventDate) as ${recordsHeldField}
    FROM species
    WHERE (
      scientificName IN (${escapedSpeciesName})
      OR taxonRemarks IN (${escapedSpeciesName})
    );`
}

function getVarsSql (visitKeyClauses) {
  return `
    SELECT
    CONCAT(locationID, '#', eventDate) AS visitKey,
    varName,
    varValue,
    varUnit
    FROM envvars
    WHERE (locationID, eventDate) in (${visitKeyClauses})
    ORDER BY 1;`
}

function getSpeciesNamesSql (visitKeyClauses, speciesNamesClauses) {
  return `
    SELECT DISTINCT
    CONCAT(locationID, '#', eventDate) AS visitKey,
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
    let parts = curr.split('#')
    let locationID = parts[0]
    let eventDate = parts[1]
    let fragment = `('${locationID}','${eventDate}')`
    if (prev === '') {
      return fragment
    }
    return prev + '\n,' + fragment
  }, '')
}

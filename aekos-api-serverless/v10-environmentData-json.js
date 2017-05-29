'use strict'
let r = require('./response-helper')
let db = require('./db-helper')
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
    let result = {
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
    return result
  }).then(result => {
    // TODO get visitKeys
    let visitKeys = result.response.map(e => {
      return e.visitKey
    })
    let visitKeyClauses = getVisitKeyClauses(visitKeys)
    let passResultDownChainPromise = new Promise((resolve) => {
      resolve(result)
    })
    let varsSql = getVarsSql(visitKeyClauses)
    let varsPromise = db.execSelectPromise(varsSql)
    let speciesNamesClauses = escapedSpeciesName // FIXME change to list of escaped names
    let speciesNamesSql = getSpeciesNamesSql(visitKeyClauses, speciesNamesClauses)
    let speciesNamesPromise = db.execSelectPromise(speciesNamesSql)
    return Promise.all([passResultDownChainPromise, varsPromise, speciesNamesPromise])
  }).then(data => {
    let result = data[0]
    // FIXME do the mapping for var and species into the result
    let varsRecords = data[1]
    let varsDict = varsRecords.reduce((prev, curr) => {
      let visitKey = curr.visitKey
      delete (curr.visitKey)
      if (typeof prev[visitKey] === 'undefined') {
        prev[visitKey] = []
      }
      prev[visitKey].push(curr)
      return prev
    }, {})
    let speciesNamesRecords = data[2]
    return {
      theResult: result,
      varsRecords: varsDict,
      speciesNamesRecords: speciesNamesRecords
    }
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

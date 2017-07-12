'use strict'
let r = require('./response-helper')
const recordsHeldField = 'recordsHeld'

module.exports.handler = (event, context, callback) => {
  let db = require('./db-helper')
  doHandle(event, callback, db, r.calculateElapsedTime)
}

function doHandle (event, callback, db, elapsedTimeCalculator) {
  let processStart = r.now()
  let params = extractParams(event) // FIXME handle thrown Errors for invalid request
  doAllSpeciesQuery(params, processStart, db, elapsedTimeCalculator).then(successResult => {
    r.json.ok(callback, successResult, event)
  }).catch(error => {
    console.error('Failed to get allSpeciesData', error)
    r.json.internalServerError(callback, 'Sorry, something went wrong')
  })
}

module.exports.doAllSpeciesQuery = doAllSpeciesQuery
function doAllSpeciesQuery (params, processStart, db, elapsedTimeCalculator) {
  const noWhereFragmentForAllSpecies = ''
  const recordsSql = getRecordsSql(params.start, params.rows, false, noWhereFragmentForAllSpecies)
  const countSql = getCountSql(noWhereFragmentForAllSpecies)
  return doQuery(params, processStart, db, elapsedTimeCalculator, recordsSql, countSql)
}

module.exports.doQuery = doQuery
function doQuery (params, processStart, db, elapsedTimeCalculator, recordsSql, countSql) {
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
    let result = {
      responseHeader: {
        elapsedTime: elapsedTimeCalculator(processStart),
        numFound: numFound,
        pageNumber: pageNumber,
        params: {
          rows: params.rows,
          start: params.start
        },
        totalPages: totalPages
      },
      response: records
    }
    return result
  })
}

module.exports._testonly = {
  doHandle: doHandle
}

module.exports.getRecordsSql = getRecordsSql
function getRecordsSql (start, rows, includeSpeciesRecordId, whereFragment) {
  let speciesIdFragment = ''
  if (includeSpeciesRecordId) {
    speciesIdFragment = 's.id,'
  }
  return `
    SELECT
    ${speciesIdFragment}
    s.scientificName,
    s.taxonRemarks,
    s.individualCount,
    s.eventDate,
    e.\`month\`,
    e.\`year\`,
    e.decimalLatitude,
    e.decimalLongitude,
    e.geodeticDatum,
    s.locationID,
    e.locationName,
    e.samplingProtocol,
    c.bibliographicCitation,
    c.datasetName
    FROM species AS s
    LEFT JOIN env AS e
    ON s.locationID = e.locationID
    AND s.eventDate = e.eventDate
    LEFT JOIN citations AS c
    ON e.samplingProtocol = c.samplingProtocol
    ${whereFragment}
    ORDER BY 1
    LIMIT ${rows} OFFSET ${start};`
}

module.exports.getCountSql = getCountSql
function getCountSql (whereFragment) {
  return `
    SELECT count(*) AS ${recordsHeldField}
    FROM species AS s
    INNER JOIN env AS e
    ON s.locationID = e.locationID
    AND s.eventDate = e.eventDate
    INNER JOIN citations AS c
    ON e.samplingProtocol = c.samplingProtocol
    ${whereFragment};`
}

module.exports.extractParams = extractParams
function extractParams (event) {
  return {
    start: r.getOptionalNumberParam(event, 'start', 0),
    rows: r.getOptionalNumberParam(event, 'rows', 20) // TODO validate > 0
  }
}

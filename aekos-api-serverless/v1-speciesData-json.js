'use strict'
let r = require('./response-helper')
let yaml = require('yamljs')
const speciesNameParam = yaml.load('./constants.yml').paramNames.SINGLE_SPECIES_NAME
const recordsHeldField = 'recordsHeld'

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
          start: params.start,
          [speciesNameParam]: params.unescapedSpeciesName
        },
        totalPages: totalPages
      },
      response: records
    }
    return result
  })
}

module.exports._testonly = {
  getRecordsSql: getRecordsSql,
  doHandle: doHandle
}

function getRecordsSql (escapedSpeciesName, start, rows, includeSpeciesRecordId) {
  r.assertIsSupplied(escapedSpeciesName)
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
    INNER JOIN env AS e
    ON s.locationID = e.locationID
    AND s.eventDate = e.eventDate
    INNER JOIN citations AS c
    ON e.samplingProtocol = c.samplingProtocol
    WHERE (
      s.scientificName IN (${escapedSpeciesName})
      OR s.taxonRemarks IN (${escapedSpeciesName})
    )
    ORDER BY 1
    LIMIT ${rows} OFFSET ${start};`
}

function getCountSql (escapedSpeciesName) {
  return `
    SELECT count(*) AS ${recordsHeldField}
    FROM species AS s
    INNER JOIN env AS e
    ON s.locationID = e.locationID
    AND s.eventDate = e.eventDate
    INNER JOIN citations AS c
    ON e.samplingProtocol = c.samplingProtocol
    WHERE (
      s.scientificName IN (${escapedSpeciesName})
      OR s.taxonRemarks IN (${escapedSpeciesName})
    );`
}

module.exports.extractParams = extractParams
function extractParams (event, db) {
  // FIXME handle escaping a list when we can get multiple names
  let unescapedSpeciesName = event.queryStringParameters[speciesNameParam]
  let escapedSpeciesName = db.escape(unescapedSpeciesName)
  return {
    speciesName: escapedSpeciesName, // FIXME change to multiple names
    unescapedSpeciesName: unescapedSpeciesName,
    start: r.getOptionalNumberParam(event, 'start', 0),
    rows: r.getOptionalNumberParam(event, 'rows', 20) // TODO validate > 0
  }
}

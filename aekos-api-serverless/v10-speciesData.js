'use strict'
let r = require('./response-helper')
let db = require('./db-helper')
const speciesNameParam = 'speciesName'
const recordsHeldField = 'recordsHeld'

module.exports.handler = (event, context, callback) => {
  let processStart = r.now()
  // FIXME need to do content negotiation and delegate to the appropriate handler
  // FIXME get repeated query string params mapping correctly rather than just the last one
  if (!r.isQueryStringParamPresent(event, speciesNameParam)) {
    r.badRequest(callback, `the '${speciesNameParam}' query string parameter must be supplied`)
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
    console.error('Failed to get speciesData', error)
    r.internalServerError(callback, 'Sorry, something went wrong')
  })
}

function doQuery (escapedSpeciesName, start, rows, processStart, includeSpeciesRecordId) {
  return new Promise((resolve, reject) => {
    const recordsSql = getRecordsSql(escapedSpeciesName, start, rows, includeSpeciesRecordId)
    const countSql = getCountSql(escapedSpeciesName)
    let pageNumber = -1 // TODO calc page number
    let totalPages = -1 // TODO calc total pages
    let recordsPromise = db.execSelectPromise(recordsSql)
    let countPromise = db.execSelectPromise(countSql)
    Promise.all([recordsPromise, countPromise]).then(values => {
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
          },
          totalPages: totalPages
        },
        response: records
      }
      resolve(result)
    })
    .catch(error => {
      let msg = 'Problem executing SQL: ' + error.message
      console.error(msg)
      reject(msg)
    })
  })
}
module.exports.doQuery = doQuery

function getRecordsSql (escapedSpeciesName, start, rows, includeSpeciesRecordId) {
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

'use strict'
let r = require('./response-helper')
let db = require('./db-helper')
const speciesNameParam = 'speciesName'
const recordsHeldField = 'recordsHeld'

function now () {
  return new Date().getTime()
}

module.exports.handler = (event, context, callback) => {
  let processStart = now()
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
  const recordsSql = `
    SELECT
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
  const countSql = `
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
  let pageNumber = -1 // TODO calc page number
  let totalPages = -1 // TODO calc total pages
  let recordsPromise = db.execSelectPromise(recordsSql)
  let countPromise = db.execSelectPromise(countSql)
  Promise.all([recordsPromise, countPromise]).then(values => {
    let records = values[0]
    let count = values[1]
    let result = {
      responseHeader: {
        elapsedTime: now() - processStart,
        numFound: JSON.stringify(count), // FIXME get count int
        pageNumber: pageNumber,
        params: {
          rows: rows,
          start: start
        },
        totalPages: totalPages
      },
      response: records // FIXME are records in correct format?
    }
    r.ok(callback, result) // FIXME why can't we see r to resolve?
  })
}

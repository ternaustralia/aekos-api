'use strict'
let r = require('./response-helper')
let db = require('./db-helper')
const speciesNameParam = 'speciesName'

function mapResults (queryResult) {
  queryResult.forEach(function (e) {
    e.label = 'FIXME' // FIXME need to look this up
  })
  return queryResult
}
module.exports.mapResults = mapResults

module.exports.handler = (event, context, callback) => {
  // FIXME get repeated query string params mapping correctly rather than just the last one
  if (!r.isQueryStringParamPresent(event, speciesNameParam)) {
    r.badRequest(callback, `the '${speciesNameParam}' query string parameter must be supplied`)
    return
  }
  // FIXME handle escaping a list when we can get multiple names
  let speciesName = event.queryStringParameters[speciesNameParam]
  let escapedSpeciesName = db.escape(speciesName)
  let pageSize = r.getOptionalParam(event, 'pageSize', 50)
  let pageNum = r.getOptionalParam(event, 'pageNum', 1)
  let offset = r.calculateOffset(pageNum, pageSize)
  const sql = `
    SELECT v.varName AS code, count(*) AS recordsHeld
    FROM species AS s
    INNER JOIN env AS e
    ON s.locationID = e.locationID
    AND s.eventDate = e.eventDate
    AND (
	  s.scientificName IN (${escapedSpeciesName})
      OR s.taxonRemarks IN (${escapedSpeciesName})
	  )
    INNER JOIN envvars AS v
    ON v.locationID = e.locationID
    AND v.eventDate = e.eventDate
    GROUP BY 1
    ORDER BY 1
    LIMIT ${pageSize} OFFSET ${offset};`
  db.execSelect(sql, (queryResult) => {
    let mappedResults = mapResults(queryResult)
    r.json.ok(callback, mappedResults)
  })
}

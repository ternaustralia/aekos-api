'use strict'
let r = require('./response-helper')
let db = require('./db-helper')
const startsWithChar = '%'
const qParam = 'q'

module.exports.handler = (event, context, callback) => {
  // TODO add taxonRemarks to the search
  if (!r.isQueryStringParamPresent(event, qParam)) {
    r.badRequest(callback, `the '${qParam}' query string parameter must be supplied`)
    return
  }
  let partialName = event.queryStringParameters[qParam]
  let escapedPartialName = db.escape(partialName + startsWithChar)
  let offset = 0 // FIXME add param for this
  let pageSize = 20 // FIXME add param for this
  const sql = `
    SELECT speciesName, sum(recordsHeld) AS recordsHeld, 'notusedanymore' AS id
    FROM (
      SELECT scientificName AS speciesName, count(*) AS recordsHeld
      FROM species
      WHERE scientificName LIKE ${escapedPartialName}
      GROUP BY 1
      UNION
      SELECT taxonRemarks AS speciesName, count(*) AS recordsHeld
      FROM species
      WHERE taxonRemarks LIKE ${escapedPartialName}
      GROUP BY 1 
    ) AS a
    WHERE speciesName IS NOT NULL
    GROUP BY 1
    ORDER BY 1
    LIMIT ${pageSize} OFFSET ${offset};`
  db.execSelect(sql, (queryResult) => {
    r.json.ok(callback, queryResult)
  })
}

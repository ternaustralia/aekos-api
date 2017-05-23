'use strict'
let r = require('./response-helper')
let db = require('./db-helper')

module.exports.handler = (event, context, callback) => {
  // FIXME get repeated query string params mapping correctly rather than just the last one
  // FIXME handle when the query string param isn't present and return 400
  let speciesName = event.queryStringParameters.speciesName
  // FIXME get paging working
  // FIXME validate and escape params
  let start = 0
  let pageSize = 20
  let escapedSpeciesName = db.escape(speciesName)
  // FIXME get the id working or remove it
  const sql = `
    SELECT speciesName, sum(recordsHeld) AS recordsHeld, 'FIXME' AS id
    FROM (
      SELECT scientificName AS speciesName, count(*) AS recordsHeld
      FROM species
      WHERE scientificName in (${escapedSpeciesName})
      UNION
      SELECT taxonRemarks AS speciesName, count(*) AS recordsHeld
      FROM species
      WHERE taxonRemarks in (${escapedSpeciesName})
    ) AS a
    GROUP BY 1
    ORDER BY 1
    LIMIT ${pageSize} OFFSET ${start};`
  db.execSelect(sql, (queryResult) => {
    r.ok(callback, queryResult)
  })
}

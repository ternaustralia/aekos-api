'use strict'
let r = require('./response-helper')
let db = require('./db-helper')
const traitNameParam = 'traitName'

module.exports.handler = (event, context, callback) => {
  // FIXME get repeated query string params mapping correctly rather than just the last one
  if (!r.isQueryStringParamPresent(event, traitNameParam)) {
    r.json.badRequest(callback, `the '${traitNameParam}' query string parameter must be supplied`)
    return
  }
  // FIXME handle escaping a list when we can get multiple names
  let traitName = event.queryStringParameters[traitNameParam]
  let escapedTraitName = db.escape(traitName)
  let pageSize = r.getOptionalNumberParam(event, 'pageSize', 50)
  let pageNum = r.getOptionalNumberParam(event, 'pageNum', 1)
  let offset = r.calculateOffset(pageNum, pageSize)
  const sql = `
    SELECT COALESCE(s.scientificName, s.taxonRemarks) AS name, count(*) AS recordsHeld, 'notusedanymore' AS id
    FROM species AS s
    INNER JOIN traits AS t
    ON t.parentId = s.id
    AND t.traitName in (${escapedTraitName})
    GROUP BY 1
    ORDER BY 1
    LIMIT ${pageSize} OFFSET ${offset};`
  db.execSelect(sql, (queryResult) => {
    r.json.ok(callback, queryResult)
  })
}

'use strict'
let r = require('./response-helper')
let db = require('./db-helper')
const codeField = 'traitName'
const countField = 'recordsHeld'
const startsWithChar = '%'
const qParam = 'q'

let mapQueryResult = (queryResult) => {
  queryResult.forEach(function(element) {
    element.code = element[codeField]
    delete(element[codeField])
    element.label = 'FIXME' // FIXME need to get this
  })
  return queryResult
}
module.exports.mapQueryResult = mapQueryResult

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
  const sql = `SELECT scientificName, count(*) AS ${countField} 
    FROM species 
    WHERE scientificName LIKE ${escapedPartialName} 
    GROUP BY 1 
    ORDER BY 1
    LIMIT ${pageSize} OFFSET ${offset};`
  db.execSelect(sql, (queryResult) => {
    let mappedResult = mapQueryResult(queryResult)
    r.ok(callback, mappedResult)
  })
}

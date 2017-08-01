'use strict'
let r = require('./response-helper')
let db = require('./db-helper')

let mapQueryResult = queryResult => {
  queryResult.forEach(element => {
    element.label = r.resolveVocabCode(element.code)
  })
  return queryResult
}
module.exports.mapQueryResult = mapQueryResult

module.exports.handler = (event, context, callback) => {
  const sql = `
    SELECT traitName AS code, count(*) AS recordsHeld
    FROM traits
    GROUP BY 1
    ORDER BY 1;`
  db.execSelect(sql, (queryResult) => {
    let mappedResult = mapQueryResult(queryResult)
    r.json.ok(callback, mappedResult)
  })
}

module.exports.responseSchema = require('./commonSchemas.js').vocabSchema

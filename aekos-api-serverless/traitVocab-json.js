'use strict'
let r = require('./response-helper')

module.exports.doHandle = doHandle
function doHandle (event, callback, db) {
  const sql = `
    SELECT traitName AS code, count(*) AS recordsHeld
    FROM traits
    GROUP BY 1
    ORDER BY 1;`
  db.execSelectPromise(sql).then(queryResult => {
    let mappedResult = mapQueryResult(queryResult)
    r.json.ok(callback, mappedResult)
  }).catch(reason => {
    console.error('Failed to get trait vocab', reason)
    r.json.internalServerError(callback)
  })
}

module.exports.mapQueryResult = mapQueryResult
function mapQueryResult (queryResult) {
  queryResult.forEach(element => {
    element.label = r.resolveVocabCode(element.code)
  })
  return queryResult
}

module.exports.responseSchema = require('./commonSchemas.js').vocabSchema

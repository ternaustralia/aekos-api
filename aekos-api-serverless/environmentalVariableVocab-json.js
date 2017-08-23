'use strict'
let r = require('./response-helper')
const codeField = 'varName'
const countField = 'recordsHeld'

module.exports.doHandle = doHandle
function doHandle (event, callback, db) {
  const sql = `
    SELECT ${codeField} as code, count(*) AS ${countField}
    FROM envvars
    GROUP BY 1
    ORDER BY 1;`
  db.execSelectPromise(sql).then(queryResult => {
    let mappedResult = mapQueryResult(queryResult)
    r.json.ok(callback, mappedResult)
  }).catch(error => {
    console.error('Failed when querying or mapping response', error)
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

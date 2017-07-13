'use strict'
let r = require('./response-helper')
const codeField = 'varName'
const countField = 'count'

module.exports.handler = (event, context, callback) => {
  let db = require('./db-helper')
  doHandle(db, callback)
}

module.exports._testonly = {
  mapQueryResult: mapQueryResult,
  doHandle: doHandle
}

function doHandle (db, callback) {
  const sql = `
    SELECT ${codeField}, count(*) AS ${countField}
    FROM envvars
    GROUP BY 1
    ORDER BY 1;`
  db.execSelectPromise(sql).then(queryResult => {
    let mappedResult = mapQueryResult(queryResult)
    r.json.ok(callback, mappedResult)
  }).catch(error => {
    console.error('Failed when querying or mapping response', error)
    r.json.internalServerError(callback, 'Sorry about that, something has gone wrong')
  })
}

function mapQueryResult (queryResult) {
  queryResult.forEach(element => {
    element.code = element[codeField]
    delete (element[codeField])
    element.label = r.resolveVocabCode(element.code)
  })
  return queryResult
}

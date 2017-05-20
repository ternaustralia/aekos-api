'use strict'
let r = require('./response-helper')
let db = require('./db-helper')
const codeField = 'traitName'
const countField = 'count'

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
  const sql = `SELECT ${codeField}, count(*) AS ${countField} FROM envvars GROUP BY 1 ORDER BY 1;`
  db.execSelect(sql, (queryResult) => {
    let mappedResult = mapQueryResult(queryResult)
    r.ok(callback, mappedResult)
  })
}

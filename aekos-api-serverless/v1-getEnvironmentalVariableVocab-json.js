'use strict'
let r = require('./response-helper')
let db = require('./db-helper')
const codeField = 'varName'
const countField = 'count'

let mapQueryResult = (queryResult) => {
  queryResult.forEach(function (element) {
    element.code = element[codeField]
    delete (element[codeField])
    element.label = r.resolveVocabCode(element.code)
  })
  return queryResult
}
module.exports.mapQueryResult = mapQueryResult

module.exports.handler = (event, context, callback) => {
  const sql = `
    SELECT ${codeField}, count(*) AS ${countField}
    FROM envvars
    GROUP BY 1
    ORDER BY 1;`
  db.execSelect(sql, (queryResult) => {
    let mappedResult = mapQueryResult(queryResult)
    r.json.ok(callback, mappedResult)
  })
}

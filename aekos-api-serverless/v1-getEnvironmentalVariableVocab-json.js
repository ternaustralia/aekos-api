'use strict'
let r = require('./response-helper')
const codeField = 'varName'
const countField = 'recordsHeld'

module.exports.handler = (event, context, callback) => {
  let db = require('./db-helper')
  doHandle(db, callback)
}

module.exports._testonly = {
  doHandle: doHandle
}

function doHandle (db, callback) {
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

module.exports.responseSchema = () => {
  return {
    '$schema': 'http://json-schema.org/draft-04/schema#',
    'additionalItems': false,
    'items': {
      'additionalProperties': false,
      'properties': {
        'code': {
          'type': 'string'
        },
        'label': {
          'type': 'string'
        },
        'recordsHeld': {
          'type': 'integer'
        }
      },
      'required': [
        'recordsHeld',
        'code',
        'label'
      ],
      'type': 'object'
    },
    'type': 'array'
  }
}

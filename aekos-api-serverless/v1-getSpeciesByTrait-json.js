'use strict'
let r = require('./response-helper')
let envByS = require('./v1-getEnvironmentBySpecies-json') // remove when we don't need wrapAsEvent hack anymore
let yaml = require('yamljs')
const traitNamesParam = yaml.load('./constants.yml').paramNames.traitName.multiple
const defaultPageSize = yaml.load('./constants.yml').defaults.PAGE_SIZE
const defaultPageNum = yaml.load('./constants.yml').defaults.PAGE_NUM

module.exports.handler = (event, context, callback) => {
  let db = require('./db-helper')
  r.handleJsonPost(event, callback, db, validator, responder)
}

const validator = r.traitNamesMandatoryValidator

function responder (requestBody, db, queryStringObj) {
  let traitNames = requestBody[traitNamesParam]
  let escapedTraitNames = db.toSqlList(traitNames)
  let pageSize = r.getOptionalNumberParam(envByS._testonly.wrapAsEvent(queryStringObj), 'pageSize', defaultPageSize)
  let pageNum = r.getOptionalNumberParam(envByS._testonly.wrapAsEvent(queryStringObj), 'pageNum', defaultPageNum)
  let sql = getSql(escapedTraitNames, pageNum, pageSize)
  return db.execSelectPromise(sql)
}

module.exports._testonly = {
  getSql: getSql,
  responder: responder,
  validator: validator
}

function getSql (escapedTraitNames, pageNum, pageSize) {
  let offset = r.calculateOffset(pageNum, pageSize)
  return `
    SELECT speciesName AS name, recordsHeld, 'notusedanymore' AS id
    FROM traitcounts
    WHERE traitName IN (${escapedTraitNames})
    ORDER BY 1
    LIMIT ${pageSize} OFFSET ${offset};`
}

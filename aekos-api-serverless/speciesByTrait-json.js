'use strict'
let r = require('./response-helper')
let envByS = require('./environmentBySpecies-json') // remove when we don't need wrapAsEvent hack anymore
let speciesSummary = require('./speciesSummary-json')
let yaml = require('yamljs')
const traitNamesParam = yaml.load('./constants.yml').paramNames.traitName.multiple
const pageSizeParam = yaml.load('./constants.yml').paramNames.PAGE_SIZE
const pageNumParam = yaml.load('./constants.yml').paramNames.PAGE_NUM
const defaultPageSize = yaml.load('./constants.yml').defaults.PAGE_SIZE
const defaultPageNum = yaml.load('./constants.yml').defaults.PAGE_NUM

module.exports.handler = (event, context, callback) => {
  let db = require('./db-helper')
  doHandle(event, callback, db)
}

function doHandle (event, callback, db) {
  r.handleJsonPost(event, callback, db, validator, responder, {
    event: event
  })
}

const validator = r.traitNamesMandatoryValidator

function responder (requestBody, db, queryStringObj, extrasProvider) {
  let traitNames = requestBody[traitNamesParam]
  let escapedTraitNames = db.toSqlList(traitNames)
  let pageSize = r.getOptionalNumberParam(envByS._testonly.wrapAsEvent(queryStringObj), pageSizeParam, defaultPageSize)
  let pageNum = r.getOptionalNumberParam(envByS._testonly.wrapAsEvent(queryStringObj), pageNumParam, defaultPageNum)
  let sql = getSql(escapedTraitNames, pageNum, pageSize)
  return db.execSelectPromise(sql).then(queryResult => {
    return new Promise((resolve, reject) => {
      try {
        speciesSummary.processWithVersionStrategy(queryResult, extrasProvider.event)
        resolve({
          body: queryResult
          // TODO add linkHeaderData
        })
      } catch (error) {
        reject(error)
      }
    })
  })
}

module.exports._testonly = {
  getSql: getSql,
  doHandle: doHandle,
  validator: validator
}

function getSql (escapedTraitNames, pageNum, pageSize) {
  let offset = r.calculateOffset(pageNum, pageSize)
  return `
    SELECT speciesName AS name, recordsHeld
    FROM traitcounts
    WHERE traitName IN (${escapedTraitNames})
    ORDER BY 1
    LIMIT ${pageSize} OFFSET ${offset};`
}

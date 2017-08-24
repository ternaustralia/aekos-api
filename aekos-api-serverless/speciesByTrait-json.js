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

module.exports.doHandle = doHandle
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
  let recordsSql = getRecordsSql(escapedTraitNames, pageNum, pageSize)
  let countSql = getCountSql(escapedTraitNames)
  let recordsPromise = db.execSelectPromise(recordsSql)
  let countPromise = db.execSelectPromise(countSql)
  return Promise.all([recordsPromise, countPromise]).then(values => {
    let queryResult = values[0]
    let count = values[1]
    if (count.length !== 1) {
      throw new Error(`SQL result problem: result from count query did not have exactly one row. Result='${JSON.stringify(count)}'`)
    }
    let totalRecordsCount = count[0].totalRecords
    speciesSummary.processWithVersionStrategy(queryResult, extrasProvider.event)
    return Promise.resolve({
      body: queryResult,
      linkHeaderData: {
        pageNumber: pageNum,
        totalPages: Math.ceil(totalRecordsCount / pageSize)
      }
    })
  })
}

module.exports._testonly = {
  getRecordsSql: getRecordsSql,
  getCountSql: getCountSql,
  validator: validator
}

function getRecordsSql (escapedTraitNames, pageNum, pageSize) {
  let offset = r.calculateOffset(pageNum, pageSize)
  return `
    SELECT speciesName AS name, recordsHeld
    FROM traitcounts
    WHERE traitName IN (${escapedTraitNames})
    ORDER BY 1
    LIMIT ${pageSize} OFFSET ${offset};`
}

function getCountSql (escapedTraitNames) {
  return `
    SELECT count(*) AS totalRecords
    FROM traitcounts
    WHERE traitName IN (${escapedTraitNames});`
}

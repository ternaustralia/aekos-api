'use strict'
let r = require('./response-helper')
let getEnvVarVocab = require('./environmentalVariableVocab-json.js')
let yaml = require('yamljs')
const speciesNamesParam = yaml.load('./constants.yml').paramNames.speciesName.multiple
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

const validator = r.compositeValidator([
  r.speciesNamesValidator,
  r.pageSizeValidator,
  r.pageNumValidator
])

module.exports._testonly = {
  doHandle: doHandle,
  validator: validator,
  wrapAsEvent: wrapAsEvent
}

function responder (requestBody, db, queryStringObj) {
  let speciesNames = requestBody[speciesNamesParam]
  let escapedSpeciesNames = db.toSqlList(speciesNames)
  let pageSize = r.getOptionalNumberParam(wrapAsEvent(queryStringObj), pageSizeParam, defaultPageSize)
  let pageNum = r.getOptionalNumberParam(wrapAsEvent(queryStringObj), pageNumParam, defaultPageNum)
  const recordsSql = getRecordsSql(escapedSpeciesNames, pageSize, pageNum)
  const countSql = getCountSql(escapedSpeciesNames)
  let recordsPromise = db.execSelectPromise(recordsSql)
  let countPromise = db.execSelectPromise(countSql)
  return Promise.all([recordsPromise, countPromise]).then(values => {
    let queryResult = values[0]
    let count = values[1]
    if (count.length !== 1) {
      throw new Error(`SQL result problem: result from count query did not have exactly one row. Result='${JSON.stringify(count)}'`)
    }
    let totalRecordsCount = count[0].totalRecords
    return new Promise((resolve, reject) => {
      try {
        let mappedResults = getEnvVarVocab.mapQueryResult(queryResult)
        resolve({
          body: mappedResults,
          linkHeaderData: {
            pageNumber: pageNum,
            totalPages: Math.ceil(totalRecordsCount / pageSize)
          }
        })
      } catch (error) {
        reject(error)
      }
    })
  })
}

function getRecordsSql (escapedSpeciesNames, pageSize, pageNum) {
  let offset = r.calculateOffset(pageNum, pageSize)
  return `
    SELECT v.varName AS code, count(*) AS recordsHeld
    FROM species AS s
    INNER JOIN env AS e
    ON s.locationID = e.locationID
    AND s.eventDate = e.eventDate
    AND (
      s.scientificName IN (${escapedSpeciesNames})
      OR s.taxonRemarks IN (${escapedSpeciesNames})
    )
    INNER JOIN envvars AS v
    ON v.locationID = e.locationID
    AND v.eventDate = e.eventDate
    GROUP BY 1
    ORDER BY 1
    LIMIT ${pageSize} OFFSET ${offset};`
}

function getCountSql (escapedSpeciesNames) {
  return `
    SELECT count(DISTINCT v.varName) AS totalRecords
    FROM species AS s
    INNER JOIN env AS e
    ON s.locationID = e.locationID
    AND s.eventDate = e.eventDate
    AND (
      s.scientificName IN (${escapedSpeciesNames})
      OR s.taxonRemarks IN (${escapedSpeciesNames})
    )
    INNER JOIN envvars AS v
    ON v.locationID = e.locationID
    AND v.eventDate = e.eventDate;`
}

function wrapAsEvent (queryStringObj) {
  return {
    queryStringParameters: queryStringObj
  }
}

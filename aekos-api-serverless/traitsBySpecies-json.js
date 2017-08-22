'use strict'
let r = require('./response-helper')
let v1TraitVocab = require('./traitVocab-json')
let yaml = require('yamljs')
const speciesNamesParam = yaml.load('./constants.yml').paramNames.speciesName.multiple
let envByS = require('./environmentBySpecies-json') // remove when we don't need wrapAsEvent hack anymore
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

const validator = r.speciesNamesValidator

function responder (requestBody, db, queryStringObj) {
  let speciesNames = requestBody[speciesNamesParam]
  let escapedSpeciesNames = db.toSqlList(speciesNames)
  let pageSize = r.getOptionalNumberParam(envByS._testonly.wrapAsEvent(queryStringObj), 'pageSize', defaultPageSize)
  let pageNum = r.getOptionalNumberParam(envByS._testonly.wrapAsEvent(queryStringObj), 'pageNum', defaultPageNum)
  let recordsSql = getRecordsSql(escapedSpeciesNames, pageNum, pageSize)
  let countSql = getCountSql(escapedSpeciesNames)
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
        let mappedResults = v1TraitVocab.mapQueryResult(queryResult)
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

module.exports._testonly = {
  getRecordsSql: getRecordsSql,
  getCountSql: getCountSql,
  doHandle: doHandle,
  validator: validator
}

function getRecordsSql (escapedSpeciesNames, pageNum, pageSize) {
  let offset = r.calculateOffset(pageNum, pageSize)
  return `
    SELECT t.traitName AS code, count(*) AS recordsHeld
    FROM species AS s
    INNER JOIN traits AS t
    ON t.parentId = s.id
    AND (
      s.scientificName IN (${escapedSpeciesNames})
      OR s.taxonRemarks IN (${escapedSpeciesNames})
    )
    GROUP BY 1
    ORDER BY 1
    LIMIT ${pageSize} OFFSET ${offset};`
}

function getCountSql (escapedSpeciesNames) {
  return `
    SELECT count(DISTINCT t.traitName) AS totalRecords
    FROM species AS s
    INNER JOIN traits AS t
    ON t.parentId = s.id
    AND (
      s.scientificName IN (${escapedSpeciesNames})
      OR s.taxonRemarks IN (${escapedSpeciesNames})
    );`
}

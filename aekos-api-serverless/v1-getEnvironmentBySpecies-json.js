'use strict'
let r = require('./response-helper')
let getEnvVarVocab = require('./v1-getEnvironmentalVariableVocab-json.js')
let yaml = require('yamljs')
const speciesNamesParam = yaml.load('./constants.yml').paramNames.speciesName.multiple
const defaultPageSize = yaml.load('./constants.yml').defaults.PAGE_SIZE
const defaultPageNum = yaml.load('./constants.yml').defaults.PAGE_NUM

module.exports.handler = (event, context, callback) => {
  let db = require('./db-helper')
  r.handlePost(event, callback, db, validator, responder)
}

const validator = r.compositeValidator([
  r.speciesNamesValidator,
  r.pageSizeValidator,
  r.pageNumValidator
])

module.exports._testonly = {
  responder: responder,
  validator: validator
}

function responder (requestBody, db, queryStringObj) {
  let speciesNames = requestBody[speciesNamesParam]
  let escapedSpeciesNames = db.toSqlList(speciesNames)
  let pageSize = r.getOptionalNumberParam(wrapAsEvent(queryStringObj), 'pageSize', defaultPageSize)
  let pageNum = r.getOptionalNumberParam(wrapAsEvent(queryStringObj), 'pageNum', defaultPageNum)
  let offset = r.calculateOffset(pageNum, pageSize)
  const sql = `
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
  return db.execSelectPromise(sql).then(queryResult => {
    return new Promise((resolve, reject) => {
      let mappedResults = getEnvVarVocab.mapQueryResult(queryResult)
      resolve(mappedResults)
    })
  })
}

function wrapAsEvent (queryStringObj) {
  return {
    queryStringParameters: queryStringObj
  }
}

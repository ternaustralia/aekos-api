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
  r.handleJsonPost(event, callback, db, validator, responder)
}

const validator = r.speciesNamesValidator

function responder (requestBody, db, queryStringObj) {
  let speciesNames = requestBody[speciesNamesParam]
  let escapedSpeciesName = db.toSqlList(speciesNames)
  let pageSize = r.getOptionalNumberParam(envByS._testonly.wrapAsEvent(queryStringObj), 'pageSize', defaultPageSize)
  let pageNum = r.getOptionalNumberParam(envByS._testonly.wrapAsEvent(queryStringObj), 'pageNum', defaultPageNum)
  let sql = getSql(escapedSpeciesName, pageNum, pageSize)
  return db.execSelectPromise(sql).then(queryResult => {
    return new Promise((resolve, reject) => {
      try {
        let mappedResults = v1TraitVocab.mapQueryResult(queryResult)
        resolve(mappedResults)
      } catch (error) {
        reject(error)
      }
    })
  })
}

module.exports._testonly = {
  getSql: getSql,
  responder: responder,
  validator: validator
}

function getSql (escapedSpeciesNames, pageNum, pageSize) {
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

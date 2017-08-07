'use strict'
let r = require('./response-helper')
let speciesSummary = require('./speciesSummary-json')
const startsWithChar = '%'
let yaml = require('yamljs')
const qParam = yaml.load('./constants.yml').paramNames.PARTIAL_NAME
const startParam = yaml.load('./constants.yml').paramNames.START
const startDefault = yaml.load('./constants.yml').defaults.START
const rowsParam = yaml.load('./constants.yml').paramNames.ROWS
const rowsDefault = yaml.load('./constants.yml').defaults.ROWS

module.exports.handler = (event, context, callback) => {
  let db = require('./db-helper')
  doHandle(event, callback, db)
}

function doHandle (event, callback, db) {
  if (!r.isQueryStringParamPresent(event, qParam)) {
    r.json.badRequest(callback, `the '${qParam}' query string parameter must be supplied`)
    return
  }
  let partialName = event.queryStringParameters[qParam]
  let start = r.getOptionalNumberParam(event, startParam, startDefault)
  let rows = r.getOptionalNumberParam(event, rowsParam, rowsDefault)
  const sql = getSql(partialName, rows, start, db)
  db.execSelectPromise(sql).then(queryResult => {
    speciesSummary.processWithVersionStrategy(queryResult, event)
    r.json.ok(callback, queryResult)
  }).catch(error => {
    console.log('Failed to execute species autocomplete SQL', error)
    r.json.internalServerError(callback)
  })
}

module.exports._testonly = {
  getSql: getSql,
  doHandle: doHandle
}

function getSql (partialName, rows, start, db) {
  let escapedPartialName = db.escape(partialName + startsWithChar)
  return `
    SELECT speciesName, sum(recordsHeld) AS recordsHeld
    FROM (
      SELECT scientificName AS speciesName, count(*) AS recordsHeld
      FROM species
      WHERE scientificName LIKE ${escapedPartialName}
      GROUP BY 1
      UNION
      SELECT taxonRemarks AS speciesName, count(*) AS recordsHeld
      FROM species
      WHERE taxonRemarks LIKE ${escapedPartialName}
      GROUP BY 1 
    ) AS a
    WHERE speciesName IS NOT NULL
    GROUP BY 1
    ORDER BY 1
    LIMIT ${rows} OFFSET ${start};`
}

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

module.exports.doHandle = doHandle
function doHandle (event, callback, db) {
  if (!r.isQueryStringParamPresent(event, qParam)) {
    r.json.badRequest(callback, `the '${qParam}' query string parameter must be supplied`)
    return
  }
  let partialName = event.queryStringParameters[qParam]
  let start = r.getOptionalNumberParam(event, startParam, startDefault)
  let rows = r.getOptionalNumberParam(event, rowsParam, rowsDefault)
  const recordsSql = getRecordsSql(partialName, rows, start, db)
  const countSql = getCountSql(partialName, db)
  let recordsPromise = db.execSelectPromise(recordsSql)
  let countPromise = db.execSelectPromise(countSql)
  Promise.all([recordsPromise, countPromise]).then(values => {
    let queryResult = values[0]
    let count = values[1]
    if (count.length !== 1) {
      throw new Error(`SQL result problem: result from count query did not have exactly one row. Result='${JSON.stringify(count)}'`)
    }
    let totalRecordsCount = count[0].totalRecords
    speciesSummary.processWithVersionStrategy(queryResult, event)
    let totalPages = r.calculateTotalPages(rows, totalRecordsCount)
    let pageNumber = r.calculatePageNumber(start, totalRecordsCount, totalPages)
    r.json.ok(callback, queryResult, event, r.buildLinkHeaderData(start, rows, pageNumber, totalPages))
  }).catch(error => {
    console.error('Failed to execute species autocomplete SQL', error)
    r.json.internalServerError(callback)
  })
}

module.exports._testonly = {
  getRecordsSql: getRecordsSql,
  getCountSql: getCountSql
}

function getRecordsSql (partialName, rows, start, db) {
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

function getCountSql (partialName, db) {
  let escapedPartialName = db.escape(partialName + startsWithChar)
  return `
    SELECT count(*) AS totalRecords
    FROM (
      SELECT DISTINCT scientificName AS speciesName
      FROM species
      WHERE scientificName LIKE ${escapedPartialName}
      UNION
      SELECT DISTINCT taxonRemarks AS speciesName
      FROM species
      WHERE taxonRemarks LIKE ${escapedPartialName}
    ) AS a
    WHERE speciesName IS NOT NULL;`
}

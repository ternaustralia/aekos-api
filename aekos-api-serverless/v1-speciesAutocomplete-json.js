'use strict'
let r = require('./response-helper')
const startsWithChar = '%'
let yaml = require('yamljs')
const qParam = yaml.load('./constants.yml').paramNames.PARTIAL_NAME
const offsetParam = yaml.load('./constants.yml').paramNames.OFFSET
const offsetDefault = yaml.load('./constants.yml').defaults.OFFSET
const pageSizeParam = yaml.load('./constants.yml').paramNames.PAGE_SIZE
const pageSizeDefault = yaml.load('./constants.yml').defaults.PAGE_SIZE

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
  let offset = r.getOptionalNumberParam(event, offsetParam, offsetDefault)
  let pageSize = r.getOptionalNumberParam(event, pageSizeParam, pageSizeDefault)
  const sql = getSql(partialName, pageSize, offset, db)
  db.execSelectPromise(sql).then(queryResult => {
    r.json.ok(callback, queryResult)
  }).catch(error => {
    const msg = 'Failed to execute species autocomplete SQL'
    console.log(msg, error)
    r.json.internalServerError(callback)
  })
}

module.exports._testonly = {
  getSql: getSql,
  doHandle: doHandle
}

function getSql (partialName, pageSize, offset, db) {
  let escapedPartialName = db.escape(partialName + startsWithChar)
  return `
    SELECT speciesName, sum(recordsHeld) AS recordsHeld, 'notusedanymore' AS id
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
    LIMIT ${pageSize} OFFSET ${offset};`
}

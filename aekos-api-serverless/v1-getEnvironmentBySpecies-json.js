'use strict'
let r = require('./response-helper')
let getEnvVarVocab = require('./v1-getEnvironmentalVariableVocab-json.js')
let yaml = require('yamljs')
const speciesNameParam = yaml.load('./constants.yml').paramNames.SINGLE_SPECIES_NAME
const msg500 = yaml.load('./constants.yml').messages.public.internalServerError

module.exports.handler = (event, context, callback) => {
  let db = require('./db-helper')
  doHandle(db, event, callback)
}

module.exports._testonly = {
  doHandle: doHandle
}

function doHandle (db, event, callback) {
  if (!r.isQueryStringParamPresent(event, speciesNameParam)) {
    r.json.badRequest(callback, `the '${speciesNameParam}' query string parameter must be supplied`)
    return
  }
  // FIXME handle escaping a list when we can get multiple names
  let speciesName = event.queryStringParameters[speciesNameParam]
  let escapedSpeciesName = db.escape(speciesName)
  let pageSize = r.getOptionalNumberParam(event, 'pageSize', 50) // move to defaults, add to doco
  let pageNum = r.getOptionalNumberParam(event, 'pageNum', 1) // move to defaults, add to doco
  let offset = r.calculateOffset(pageNum, pageSize)
  const sql = `
    SELECT v.varName AS code, count(*) AS recordsHeld
    FROM species AS s
    INNER JOIN env AS e
    ON s.locationID = e.locationID
    AND s.eventDate = e.eventDate
    AND (
      s.scientificName IN (${escapedSpeciesName})
      OR s.taxonRemarks IN (${escapedSpeciesName})
    )
    INNER JOIN envvars AS v
    ON v.locationID = e.locationID
    AND v.eventDate = e.eventDate
    GROUP BY 1
    ORDER BY 1
    LIMIT ${pageSize} OFFSET ${offset};`
  db.execSelectPromise(sql).then(queryResult => {
    let mappedResults = getEnvVarVocab.mapQueryResult(queryResult)
    r.json.ok(callback, mappedResults)
  }).catch(error => {
    console.error('Failed when querying or mapping response', error)
    r.json.internalServerError(callback, msg500)
  })
}

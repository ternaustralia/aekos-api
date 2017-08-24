'use strict'
let r = require('./response-helper')
let yaml = require('yamljs')
const speciesNamesParam = yaml.load('./constants.yml').paramNames.speciesName.multiple

module.exports.doHandle = doHandle
function doHandle (event, callback, db) {
  r.handleJsonPost(event, callback, db, r.speciesNamesValidator, responder, {
    event: event
  })
}

module.exports._testonly = {
  responder: responder,
  getSql: getSql
}

function responder (requestBody, db, _, extrasProvider) {
  let speciesNames = requestBody[speciesNamesParam]
  let sql = getSql(speciesNames, db)
  return db.execSelectPromise(sql).then(sqlResult => {
    processWithVersionStrategy(sqlResult, extrasProvider.event)
    return Promise.resolve({
      body: sqlResult
      // no linkHeaderData because we don't support paging
    })
  })
}

module.exports.processWithVersionStrategy = processWithVersionStrategy
function processWithVersionStrategy (sqlResult, event) {
  let strategy = getVersionStrategy(event)
  strategy(sqlResult)
}

function addIdField (records) {
  records.forEach(curr => {
    curr.id = 'notusedanymore'
  })
}

function getVersionStrategy (event) {
  let versionHandler = r.newVersionHandler({
    '/v1/': addIdField,
    '/v2/': () => {}
  })
  return versionHandler.handle(event)
}

function getSql (speciesNames, db) {
  let escapedSpeciesNames = db.toSqlList(speciesNames)
  return `
    SELECT speciesName, sum(recordsHeld) AS recordsHeld
    FROM (
      SELECT scientificName AS speciesName, count(*) AS recordsHeld
      FROM species
      WHERE scientificName in (${escapedSpeciesNames})
      GROUP BY 1
      UNION
      SELECT taxonRemarks AS speciesName, count(*) AS recordsHeld
      FROM species
      WHERE taxonRemarks in (${escapedSpeciesNames})
      GROUP BY 1
    ) AS a
    WHERE a.speciesName IS NOT NULL
    GROUP BY 1
    ORDER BY 1;`
}

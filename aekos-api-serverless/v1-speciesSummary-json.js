'use strict'
let r = require('./response-helper')
let yaml = require('yamljs')
const speciesNameParam = yaml.load('./constants.yml').paramNames.speciesName.multiple

module.exports.handler = (event, context, callback) => {
  let db = require('./db-helper')
  doHandle(event, callback, db)
}

module.exports._testonly = {
  doHandle: doHandle,
  getSql: getSql
}
function doHandle (event, callback, db) {
  let requestBody = JSON.parse(event.body) // TODO extract to helper
  // TODO validate property is present
  let speciesNames = requestBody[speciesNameParam]
  let sql = getSql(speciesNames, db)
  db.execSelectPromise(sql).then(queryResult => {
    r.json.ok(callback, queryResult)
  }).catch(error => {
    console.error('Failed to get species summaries', error)
    r.json.internalServerError(callback, 'Sorry, something went wrong')
  })
}

function getSql (speciesNames, db) {
  let escapedSpeciesName = db.toSqlList(speciesNames)
  return `
    SELECT speciesName, sum(recordsHeld) AS recordsHeld, 'notusedanymore' AS id
    FROM (
      SELECT scientificName AS speciesName, count(*) AS recordsHeld
      FROM species
      WHERE scientificName in (${escapedSpeciesName})
      GROUP BY 1
      UNION
      SELECT taxonRemarks AS speciesName, count(*) AS recordsHeld
      FROM species
      WHERE taxonRemarks in (${escapedSpeciesName})
      GROUP BY 1
    ) AS a
    WHERE a.speciesName IS NOT NULL
    GROUP BY 1
    ORDER BY 1;`
}

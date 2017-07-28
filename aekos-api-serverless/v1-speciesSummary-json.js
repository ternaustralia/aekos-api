'use strict'
let r = require('./response-helper')
let yaml = require('yamljs')
const speciesNamesParam = yaml.load('./constants.yml').paramNames.speciesName.multiple

module.exports.handler = (event, context, callback) => {
  let db = require('./db-helper')
  r.handleJsonPost(event, callback, db, r.speciesNamesValidator, responder)
}

module.exports._testonly = {
  responder: responder,
  getSql: getSql
}

function responder (requestBody, db) {
  let speciesNames = requestBody[speciesNamesParam]
  let sql = getSql(speciesNames, db)
  return db.execSelectPromise(sql)
}

function getSql (speciesNames, db) {
  let escapedSpeciesNames = db.toSqlList(speciesNames)
  return `
    SELECT speciesName, sum(recordsHeld) AS recordsHeld, 'notusedanymore' AS id
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

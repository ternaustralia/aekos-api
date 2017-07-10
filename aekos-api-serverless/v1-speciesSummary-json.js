'use strict'
let r = require('./response-helper')
let yaml = require('yamljs')
const speciesNameParam = yaml.load('./constants.yml').paramNames.speciesName.multiple

module.exports.handler = (event, context, callback) => {
  let db = require('./db-helper')
  r.handlePost(event, callback, db, validator, responder)
}

module.exports._testonly = {
  responder: responder,
  validator: validator,
  getSql: getSql
}
function validator (requestBody) {
  let speciesNames = requestBody[speciesNameParam]
  let isFieldNotSupplied = typeof speciesNames === 'undefined'
  if (isFieldNotSupplied) {
    return { isValid: false, message: 'No species names were supplied' }
  }
  let isFieldNotArray = speciesNames.constructor !== Array
  if (isFieldNotArray) {
    return { isValid: false, message: `The '${speciesNameParam}' field must be an array (of strings)` }
  }
  let isArrayEmpty = speciesNames.length < 1
  if (isArrayEmpty) {
    return { isValid: false, message: `The '${speciesNameParam}' field is mandatory and was not supplied` }
  }
  let isAnyElementNotStrings = speciesNames.some(element => { return typeof element !== 'string' })
  if (isAnyElementNotStrings) {
    return { isValid: false, message: `The '${speciesNameParam}' field must be an array of strings. You supplied a non-string element.` }
  }
  return { isValid: true }
}

function responder (requestBody, db) {
  let speciesNames = requestBody[speciesNameParam]
  let sql = getSql(speciesNames, db)
  return db.execSelectPromise(sql)
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

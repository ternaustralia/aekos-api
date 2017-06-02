'use strict'
let r = require('./response-helper')
let db = require('./db-helper')
let speciesDataJson = require('./v1-speciesData-json')
let latches = require('latches')
let yaml = require('yamljs')
const speciesNameParam = yaml.load('./constants.yml').paramNames.SINGLE_SPECIES_NAME

module.exports.handler = (event, context, callback) => {
  let processStart = r.now()
  if (!r.isQueryStringParamPresent(event, speciesNameParam)) {
    r.json.badRequest(callback, `the '${speciesNameParam}' query string parameter must be supplied`)
    return
  }
  let params = extractParams(event)
  getTraitData(params, processStart).then(successResult => {
    r.json.ok(callback, successResult)
  }).catch(error => {
    console.error('Failed while building result', error)
    r.json.internalServerError(callback, 'Sorry, something went wrong')
  })
}

module.exports.getTraitData = getTraitData
function getTraitData (params, processStart) {
  return speciesDataJson.doQuery(params.speciesName, params.start, params.rows, processStart, true).then(successResult => {
    return enrichWithTraitData(successResult, params.traitNames)
  }).then((successResultWithTraits) => {
    successResultWithTraits.responseHeader.elapsedTime = r.now() - processStart
    return successResultWithTraits
  })
}

function enrichWithTraitData (successResult, traitNames) {
  let speciesRecords = successResult.response
  return new Promise((resolve, reject) => {
    let cdl = new latches.CountDownLatch(speciesRecords.length)
    cdl.wait(function () {
      resolve(successResult)
    })
    speciesRecords.forEach(curr => {
      const traitSql = getTraitSql(curr.id, traitNames)
      db.execSelectPromise(traitSql).then(traitRecords => {
        curr.traits = traitRecords
        delete (curr.id)
        cdl.hit()
      }).catch(error => {
        reject(new Error(`DB problem: failed while adding traits to species.id='${curr.id}' with error=${JSON.stringify(error)}`))
      })
    })
  })
}

function getTraitSql (parentId, traitNames) {
  // FIXME check if we need to filter by traitName
  return `
    SELECT
    traitName,
    traitValue,
    traitUnit
    FROM traits
    WHERE parentId = '${parentId}';`
}

module.exports.extractParams = extractParams
function extractParams (event) {
  let result = speciesDataJson.extractParams(event)
  result.traitNames = [] // FIXME get optional traitName(s)
  return result
}

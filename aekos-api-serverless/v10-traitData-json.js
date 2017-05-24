'use strict'
let r = require('./response-helper')
let db = require('./db-helper')
let speciesData = require('./v10-speciesData')
let latches = require('latches')
const speciesNameParam = 'speciesName'
const recordsHeldField = 'recordsHeld'

module.exports.handler = (event, context, callback) => {
  let processStart = now()
  // FIXME need to do content negotiation and delegate to the appropriate handler
  // FIXME get repeated query string params mapping correctly rather than just the last one
  if (!r.isQueryStringParamPresent(event, speciesNameParam)) {
    r.badRequest(callback, `the '${speciesNameParam}' query string parameter must be supplied`)
    return
  }
  // FIXME handle escaping a list when we can get multiple names
  let speciesName = event.queryStringParameters[speciesNameParam]
  let escapedSpeciesName = db.escape(speciesName)
  // FIXME get optional traitName(s)
  let traitNames = []
  let start = r.getOptionalParam(event, 'start', 0)
  let rows = r.getOptionalParam(event, 'rows', 20)
  let speciesDataResult = speciesData.doQuery(escapedSpeciesName, start, rows, processStart, true).then(successResult => {
    enrichWithTraitData(successResult.response, traitNames).then(() => {
      successResult.responseHeader.elapsedTime = now() - processStart
      r.ok(callback, successResult)
    }).catch(error => {
      r.internalServerError(callback, 'Sorry, something went wrong')
    })
  }).catch(error => {
    r.internalServerError(callback, 'Sorry, something went wrong')
  })
}

function enrichWithTraitData (speciesRecords, traitNames) {
  return new Promise((resolve, reject) => {
    let cdl = new latches.CountDownLatch(speciesRecords.length)
    cdl.wait(function () {
      resolve()
    })
    speciesRecords.forEach(curr => {
      const traitSql = getTraitSql(curr.id, traitNames)
      db.execSelectPromise(traitSql).then(traitRecords => {
        curr.traits = traitRecords
        delete(curr.id)
        cdl.hit()
      }).catch(error => {
        reject(`DB problem: failed while adding traits to species.id='${curr.id}' with error=${JSON.stringify(error)}`)
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

function now () {
  return new Date().getTime()
}

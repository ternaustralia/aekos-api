'use strict'
let r = require('./response-helper')
let accepts = require('accepts')
let traitDataJson = require('./v10-traitData-json')
let traitDataCsv = require('./v10-traitData-csv')

module.exports.handler = (event, context, callback) => {
  let contentTypeIndicator = getContentType(event)
  switch (contentTypeIndicator) {
    case contentTypes.json:
      traitDataJson.handler(event, context, callback)
      break
    case contentTypes.csv:
      traitDataCsv.handler(event, context, callback)
      break
    case contentTypes.unhandled:
      r.json.badRequest(callback, `Cannot handle the specified Accept header '${event.headers.Accept}`)
      break
    default:
      throw new Error(`Programmer error: unknown content type indicator returned '${contentTypeIndicator}'`)
  }
}

const contentTypes = {
  json: 'json',
  csv: 'csv',
  unhandled: 'UNHANDLED'
}

function getContentType (event) {
  let accept = accepts(castEventToFakeExpressReq(event))
  switch (accept.type(['application/json', 'text/csv'])) {
    case 'application/json':
      return contentTypes.json
    case 'text/csv':
      return contentTypes.csv
    default:
      return contentTypes.unhandled
  }
}

function castEventToFakeExpressReq (event) {
  return {
    headers: {
      accept: event.headers.Accept
    }
  }
}

module.exports._testonly = {
  getContentType: getContentType,
  contentTypes: contentTypes
}

'use strict'
let r = require('./response-helper')
let speciesDataJson = require('./v10-speciesData-json')
let speciesDataCsv = require('./v10-speciesData-csv')

module.exports.handler = (event, context, callback) => {
  let contentTypeIndicator = r.getContentType(event)
  switch (contentTypeIndicator) {
    case r.contentTypes.json:
      speciesDataJson.handler(event, context, callback)
      break
    case r.contentTypes.csv:
      speciesDataCsv.handler(event, context, callback)
      break
    case r.contentTypes.unhandled:
      r.json.badRequest(callback, `Cannot handle the specified Accept header '${event.headers.Accept}`)
      break
    default:
      throw new Error(`Programmer error: unknown content type indicator returned '${contentTypeIndicator}'`)
  }
}

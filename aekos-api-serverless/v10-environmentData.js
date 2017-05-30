'use strict'
let r = require('./response-helper')
let environmentDataJson = require('./v10-environmentData-json')
let environmentDataCsv = require('./v10-environmentData-csv')

module.exports.handler = (event, context, callback) => {
  let contentTypeIndicator = r.getContentType(event)
  switch (contentTypeIndicator) {
    case r.contentTypes.json:
      environmentDataJson.handler(event, context, callback)
      break
    case r.contentTypes.csv:
      environmentDataCsv.handler(event, context, callback)
      break
    case r.contentTypes.unhandled:
      r.json.badRequest(callback, `Cannot handle the specified Accept header '${event.headers.Accept}`)
      break
    default:
      throw new Error(`Programmer error: unknown content type indicator returned '${contentTypeIndicator}'`)
  }
}

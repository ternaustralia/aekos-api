'use strict'
let traitDataJson = require('./v10-traitData-json')

module.exports.handler = (event, context, callback) => {
  let processStart = now()
  traitDataJson.handler(event, context, (error, jsonSuccess) => { // FIXME don't call handler, make a direct call and get JSON back
    let records = JSON.parse(jsonSuccess.body).response
    let result = records.reduce((prev, curr) => {
      return prev + '\n' + curr.eventDate
    })
    callback(null, {
      headers: {
        "Content-Type": "'text/csv"
        // TODO add CORS
      },
      body: result,
      statusCode: 200
    })
  })
}

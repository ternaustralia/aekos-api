'use strict'
var r = require('./response-helper')

module.exports.handler = (event, context, callback) => {
  const body = [
    {
      "code": "averageHeight",
      "label": "Average Height"
    },
    {
      "code": "basalArea",
      "label": "Basal Area"
    },
    {
      "code": "basalAreaCount",
      "label": "Basal Area Count"
    }
  ]
  r.ok(callback, body)
}

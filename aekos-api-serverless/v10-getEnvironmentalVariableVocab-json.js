'use strict'
var r = require('response-helper')
module.exports.handler = (event, context, callback) => {
  var body = [
    {
      "code": "aspect",
      "label": "Aspect"
    },
    {
      "code": "clay",
      "label": "Clay Content"
    },
    {
      "code": "disturbanceType",
      "label": "Disturbance Type"
    }
  ]
  r.ok(callback, body)
}

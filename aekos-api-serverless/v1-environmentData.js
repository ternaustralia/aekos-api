'use strict'
let r = require('./response-helper')
let environmentDataJson = require('./v1-environmentData-json')
let environmentDataCsv = require('./v1-environmentData-csv')

module.exports.handler = r.newContentNegotiationHandler(environmentDataJson, environmentDataCsv)

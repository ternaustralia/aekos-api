'use strict'
let r = require('./response-helper')
let environmentDataJson = require('./v2-environmentData-json')
let environmentDataCsv = require('./v2-environmentData-csv')

module.exports.handler = r.newContentNegotiationHandler(environmentDataJson, environmentDataCsv)

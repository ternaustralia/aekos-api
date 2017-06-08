'use strict'
let r = require('./response-helper')
let traitDataJson = require('./v1-traitData-json')
let traitDataCsv = require('./v1-traitData-csv')

module.exports.handler = r.newContentNegotiationHandler(traitDataJson, traitDataCsv)

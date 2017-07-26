'use strict'
let r = require('./response-helper')
let traitDataJson = require('./traitData-json')
let traitDataCsv = require('./traitData-csv')

module.exports.handler = r.newContentNegotiationHandler(traitDataJson, traitDataCsv)

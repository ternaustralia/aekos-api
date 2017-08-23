'use strict'
let r = require('./response-helper')
let traitDataJson = require('./traitData-json')
let traitDataCsv = require('./traitData-csv')

module.exports.doHandle = r.newContentNegotiationHandler(traitDataJson, traitDataCsv)

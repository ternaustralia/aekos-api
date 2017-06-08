'use strict'
let r = require('./response-helper')
let speciesDataJson = require('./v1-speciesData-json')
let speciesDataCsv = require('./v1-speciesData-csv')

module.exports.handler = r.newContentNegotiationHandler(speciesDataJson, speciesDataCsv)

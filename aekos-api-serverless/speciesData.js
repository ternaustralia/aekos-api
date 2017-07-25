'use strict'
let r = require('./response-helper')
let speciesDataJson = require('./speciesData-json')
let speciesDataCsv = require('./speciesData-csv')

module.exports.handler = r.newContentNegotiationHandler(speciesDataJson, speciesDataCsv)

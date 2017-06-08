'use strict'
let r = require('./response-helper')
let allSpeciesDataJson = require('./v1-allSpeciesData-json')
let allSpeciesDataCsv = require('./v1-allSpeciesData-csv')

module.exports.handler = r.newContentNegotiationHandler(allSpeciesDataJson, allSpeciesDataCsv)

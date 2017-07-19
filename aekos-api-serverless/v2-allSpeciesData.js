'use strict'
let r = require('./response-helper')
let allSpeciesDataJson = require('./v2-allSpeciesData-json')
let allSpeciesDataCsv = require('./v2-allSpeciesData-csv')

module.exports.handler = r.newContentNegotiationHandler(allSpeciesDataJson, allSpeciesDataCsv)

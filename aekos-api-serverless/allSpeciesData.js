'use strict'
let r = require('./response-helper')
let allSpeciesDataJson = require('./allSpeciesData-json')
let allSpeciesDataCsv = require('./allSpeciesData-csv')

module.exports.handler = r.newContentNegotiationHandler(allSpeciesDataJson, allSpeciesDataCsv)

'use strict'

var objectUnderTest = require('../v10-speciesAutocomplete-json')

describe('v10-speciesAutocomplete-json', function () {
  it('should map the response when records are found', function () {
    let queryResult = [
      {
        scientificName: 'species one',
        recordsHeld: 123
      }
    ]
    let result = objectUnderTest.mapQueryResult(queryResult)
    expect(result.length).toBe(1)
    let first = result[0]
    expect(first.scientificName).toBe('species one')
    expect(first.recordsHeld).toBe(123)
  })
})

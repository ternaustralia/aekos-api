'use strict'

var objectUnderTest = require('../v10-getTraitVocab-json')

describe('v10-getTraitVocab-json', function () {
  it('should map to the code field', function () {
    let queryResult = [
      {
        traitName: 'averageHeight',
        count: 123
      }
    ]
    let result = objectUnderTest.mapQueryResult(queryResult)
    expect(result.length).toBe(1)
    let first = result[0]
    expect(first.code).toBe('averageHeight')
    expect(first.traitName).toBeUndefined()
    expect(first.label).toBe('Average Height')
    expect(first.count).toBe(123)
  })
})

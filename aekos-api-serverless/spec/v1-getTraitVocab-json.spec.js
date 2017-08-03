'use strict'

var objectUnderTest = require('../traitVocab-json')

describe('/v1/getTraitVocab-json', function () {
  it('should map to the code field', function () {
    let queryResult = [
      {
        code: 'averageHeight',
        count: 123
      }
    ]
    let result = objectUnderTest.mapQueryResult(queryResult)
    expect(result.length).toBe(1)
    let first = result[0]
    expect(first.code).toBe('averageHeight')
    expect(first.label).toBe('Average Height')
    expect(first.count).toBe(123)
  })
})

'use strict'

let objectUnderTest = require('../v10-getEnvironmentalVariableVocab-json')

describe('v10-getEnvironmentalVariableVocab-json', function () {
  // TODO add a test to assert callback is called

  it('should map to the code field', function () {
    let queryResult = [
      {
        varName: 'visibleFireEvidence',
        count: 123
      }
    ]
    let result = objectUnderTest.mapQueryResult(queryResult)
    expect(result.length).toBe(1)
    let first = result[0]
    expect(first.code).toBe('visibleFireEvidence')
    expect(first.traitName).toBeUndefined()
    expect(first.label).toBe('Visible Fire Evidence')
    expect(first.count).toBe(123)
  })
})

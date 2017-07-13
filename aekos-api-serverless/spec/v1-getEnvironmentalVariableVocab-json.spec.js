'use strict'
let objectUnderTest = require('../v1-getEnvironmentalVariableVocab-json')
let StubDB = require('./StubDB')

describe('v1-getEnvironmentalVariableVocab-json', function () {
  describe('.mapQueryResult()', () => {
    it('should map to the code field', function () {
      let queryResult = [
        {
          varName: 'visibleFireEvidence',
          count: 123
        }
      ]
      let result = objectUnderTest._testonly.mapQueryResult(queryResult)
      expect(result.length).toBe(1)
      let first = result[0]
      expect(first.code).toBe('visibleFireEvidence')
      expect(first.traitName).toBeUndefined()
      expect(first.label).toBe('Visible Fire Evidence')
      expect(first.count).toBe(123)
    })
  })

  describe('.doHandle()', () => {
    let result = null
    beforeEach(done => {
      let stubDb = new StubDB()
      stubDb.setExecSelectPromiseResponses([
        [{ varName: 'height', count: 123 }]
      ])
      let callback = (_, callbackResult) => {
        result = callbackResult
        done()
      }
      objectUnderTest._testonly.doHandle(stubDb, callback)
    })

    it('should return the expected result', () => {
      expect(result.statusCode).toBe(200)
      expect(result.headers).toEqual({
        'Access-Control-Allow-Origin': '*',
        'Access-Control-Allow-Credentials': true,
        'Content-Type': "'application/json'"
      })
      expect(result.body).toBe(JSON.stringify(
        [{ count: 123, code: 'height', label: 'Height' }]
      ))
    })
  })
})

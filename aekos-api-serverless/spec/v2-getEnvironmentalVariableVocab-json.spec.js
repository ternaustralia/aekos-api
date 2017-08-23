'use strict'
let objectUnderTest = require('../environmentalVariableVocab-json')
let uberRouter = require('../uberRouter')
let StubDB = require('./StubDB')

describe('/v2/getEnvironmentalVariableVocab-json', function () {
  describe('.doHandle()', () => {
    let result = null
    beforeEach(done => {
      let stubDb = new StubDB()
      stubDb.setExecSelectPromiseResponses([
        [{ code: 'soil', recordsHeld: 123 }]
      ])
      let callback = (_, callbackResult) => {
        result = callbackResult
        done()
      }
      let event = {
        requestContext: {
          path: '/v2/getEnvironmentalVariableVocab.json'
        }
      }
      uberRouter._testonly.doHandle(event, callback, stubDb)
    })

    it('should return the expected result', () => {
      expect(result.statusCode).toBe(200)
      expect(result.headers).toEqual({
        'Access-Control-Allow-Origin': '*',
        'Access-Control-Allow-Credentials': true,
        'Access-Control-Expose-Headers': 'link',
        'Content-Type': "'application/json'"
      })
      expect(JSON.parse(result.body)).toEqual(
        [{ code: 'soil', recordsHeld: 123, label: 'Soil Feature' }]
      )
      let validate = require('jsonschema').validate
      expect(validate(JSON.parse(result.body), objectUnderTest.responseSchema()).valid).toBe(true)
    })
  })

  describe('.mapQueryResult()', () => {
    it('should map to the code field', function () {
      let queryResult = [
        {
          code: 'visibleFireEvidence',
          recordsHeld: 123
        }
      ]
      let result = objectUnderTest.mapQueryResult(queryResult)
      expect(result.length).toBe(1)
      let first = result[0]
      expect(first).toEqual({
        recordsHeld: 123,
        code: 'visibleFireEvidence',
        label: 'Visible Fire Evidence'
      })
    })
  })
})

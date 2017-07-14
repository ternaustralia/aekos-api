'use strict'
let objectUnderTest = require('../v1-getEnvironmentBySpecies-json')
let StubDB = require('./StubDB')

describe('v1-getEnvironmentBySpecies-json', function () {
  describe('.doHandle()', () => {
    let result = null
    beforeEach(done => {
      let stubDb = new StubDB()
      stubDb.setExecSelectPromiseResponses([
        [{ code: 'clay', recordsHeld: 123 }]
      ])
      let event = {
        queryStringParameters: {
          speciesName: 'species one'
        }
      }
      let callback = (_, callbackResult) => {
        result = callbackResult
        done()
      }
      objectUnderTest._testonly.doHandle(stubDb, event, callback)
    })

    it('should return the expected result', () => {
      expect(result.statusCode).toBe(200)
      expect(result.headers).toEqual({
        'Access-Control-Allow-Origin': '*',
        'Access-Control-Allow-Credentials': true,
        'Content-Type': "'application/json'"
      })
      expect(result.body).toBe(JSON.stringify(
        [{ code: 'clay', recordsHeld: 123, label: 'Clay Content' }]
      ))
    })
  })
})

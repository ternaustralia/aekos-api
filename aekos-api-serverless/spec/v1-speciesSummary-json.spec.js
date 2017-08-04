'use strict'
var objectUnderTest = require('../speciesSummary-json')
let StubDB = require('./StubDB')

describe('/v1/speciesSummary-json', () => {
  describe('.responder()', () => {
    let result = null
    beforeEach(done => {
      let postBody = { speciesNames: ['species one'] }
      let stubDb = new StubDB()
      stubDb.setExecSelectPromiseResponses([
        [{ speciesName: 'species one', recordsHeld: 123 }]
      ])
      let extrasProvider = {
        event: {
          requestContext: {
            path: '/v1/speciesSummary.json'
          }
        }
      }
      objectUnderTest._testonly.responder(postBody, stubDb, null, extrasProvider).then(responseBody => {
        result = responseBody
        done()
      })
    })

    it('should handle a single element', () => {
      expect(result).toEqual([
        { speciesName: 'species one', recordsHeld: 123, id: 'notusedanymore' }
      ])
    })
  })
})

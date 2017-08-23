'use strict'
let uberRouter = require('../uberRouter')
let StubDB = require('./StubDB')

describe('/v1/speciesSummary-json', () => {
  describe('.doHandle()', () => {
    let result = null
    beforeEach(done => {
      let stubDb = new StubDB()
      stubDb.setExecSelectPromiseResponses([
        [{ speciesName: 'species one', recordsHeld: 123 }]
      ])
      let event = {
        requestContext: {
          path: '/v1/speciesSummary.json'
        },
        body: JSON.stringify({ speciesNames: ['species one'] })
      }
      let callback = (_, theResult) => {
        result = theResult
        done()
      }
      uberRouter._testonly.doHandle(event, callback, stubDb)
    })

    it('should handle a single element', () => {
      expect(result.statusCode).toBe(200)
      expect(result.headers).toEqual({
        'Access-Control-Allow-Origin': '*',
        'Access-Control-Allow-Credentials': true,
        'Access-Control-Expose-Headers': 'link',
        'Content-Type': "'application/json'"
      })
      expect(JSON.parse(result.body)).toEqual([
        { speciesName: 'species one', recordsHeld: 123, id: 'notusedanymore' }
      ])
    })
  })
})

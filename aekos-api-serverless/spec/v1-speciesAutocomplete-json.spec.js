'use strict'
var objectUnderTest = require('../speciesAutocomplete-json')
let StubDB = require('./StubDB')

describe('/v1/speciesAutocomplete-json', () => {
  describe('.doHandle()', () => {
    let result = null
    beforeEach(done => {
      let event = {
        queryStringParameters: {
          q: 'aca'
        },
        requestContext: {
          path: '/v1/speciesAutocomplete.json'
        }
      }
      let stubDb = new StubDB()
      stubDb.setExecSelectPromiseResponses([
        [{ speciesName: 'acacia whatever', recordsHeld: 123 }]
      ])
      let theCallback = (_, theResult) => {
        result = theResult
        done()
      }
      objectUnderTest._testonly.doHandle(event, theCallback, stubDb)
    })

    it('should add the "id" field for backwards compatibility', () => {
      expect(result.statusCode).toBe(200)
      expect(result.headers).toEqual({
        'Access-Control-Allow-Origin': '*',
        'Access-Control-Allow-Credentials': true,
        'Access-Control-Expose-Headers': 'link',
        'Content-Type': "'application/json'"
      })
      expect(JSON.parse(result.body)).toEqual(
        [{ speciesName: 'acacia whatever', recordsHeld: 123, id: 'notusedanymore' }]
      )
    })
  })
})

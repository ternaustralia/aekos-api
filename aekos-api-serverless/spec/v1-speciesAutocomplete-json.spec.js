'use strict'
let uberRouter = require('../uberRouter')
let StubDB = require('./StubDB')

describe('/v1/speciesAutocomplete-json', () => {
  describe('.doHandle()', () => {
    let result = null
    beforeEach(done => {
      let event = {
        queryStringParameters: {
          q: 'aca'
        },
        requestContext: { path: '/v1/speciesAutocomplete.json' },
        path: '/v1/speciesAutocomplete.json',
        headers: {
          Host: 'api.aekos.org.au',
          'X-Forwarded-Proto': 'https'
        }
      }
      let stubDb = new StubDB()
      stubDb.setExecSelectPromiseResponses([
        [{ speciesName: 'acacia whatever', recordsHeld: 123 }],
        [{ totalRecords: 33 }]
      ])
      let theCallback = (_, theResult) => {
        result = theResult
        done()
      }
      uberRouter._testonly.doHandle(event, theCallback, stubDb)
    })

    it('should add the "id" field for backwards compatibility', () => {
      expect(result.statusCode).toBe(200)
      expect(result.headers).toEqual({
        'Access-Control-Allow-Origin': '*',
        'Access-Control-Allow-Credentials': true,
        'Access-Control-Expose-Headers': 'link',
        'Content-Type': "'application/json'",
        'link': '<https://api.aekos.org.au/v1/speciesAutocomplete.json?q=aca&start=20>; rel="next", ' +
                '<https://api.aekos.org.au/v1/speciesAutocomplete.json?q=aca&start=20>; rel="last"'
      })
      expect(JSON.parse(result.body)).toEqual(
        [{ speciesName: 'acacia whatever', recordsHeld: 123, id: 'notusedanymore' }]
      )
    })
  })
})

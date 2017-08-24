'use strict'
let uberRouter = require('../uberRouter')

describe('/api', () => {
  describe('.doHandle()', () => {
    let result = null
    beforeEach(done => {
      let event = {
        headers: {
          Host: 'api.aekos.org.au',
          'X-Forwarded-Proto': 'https'
        },
        requestContext: { path: '/api' },
        path: '/api'
      }
      let callback = (_, theResult) => {
        result = theResult
        done()
      }
      uberRouter._testonly.doHandle(event, callback, null, null)
    })

    it('should return the full list of possible actions (links)', () => {
      const expectedLinkHeader = '' +
        '<https://api.aekos.org.au/v2/getTraitVocab.json>; rel="x-trait-vocab", ' +
        '<https://api.aekos.org.au/v2/getEnvironmentalVariableVocab.json>; rel="x-env-vocab"'
      expect(result.statusCode).toBe(200)
      expect(result.headers).toEqual({
        'Access-Control-Allow-Origin': '*',
        'Access-Control-Allow-Credentials': true,
        'Access-Control-Expose-Headers': 'link',
        'Content-Type': "'application/json'",
        'link': expectedLinkHeader
      })
      expect(JSON.parse(result.body)).toEqual({
        links: [
          {
            rel: 'x-trait-vocab',
            href: 'https://api.aekos.org.au/v2/getTraitVocab.json'
          }, {
            rel: 'x-env-vocab',
            href: 'https://api.aekos.org.au/v2/getEnvironmentalVariableVocab.json'
          }
        ]
      })
    })
  })
})

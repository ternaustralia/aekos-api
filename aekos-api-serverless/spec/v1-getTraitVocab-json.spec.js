'use strict'
let StubDB = require('./StubDB')

describe('/v1/getTraitVocab-json', function () {
  let uberRouter = require('../uberRouter')

  describe('.doHandle()', () => {
    let expectedSql1 = `
    SELECT traitName AS code, count(*) AS recordsHeld
    FROM traits
    GROUP BY 1
    ORDER BY 1;`
    let stubDb = new StubDB()
    let result = null
    beforeEach(done => {
      let event = {
        requestContext: {
          path: '/v1/getTraitVocab.json'
        },
        headers: {
          Host: 'api.aekos.org.au',
          'X-Forwarded-Proto': 'https'
        }
      }
      stubDb.setExecSelectPromiseResponses([
        [{code: 'height', recordsHeld: 123}]
      ])
      spyOn(stubDb, 'execSelectPromise').and.callThrough()
      let callback = (_, theResult) => {
        result = theResult
        done()
      }
      uberRouter._testonly.doHandle(event, callback, stubDb)
    })

    it('should return the query result', () => {
      expect(stubDb.execSelectPromise).toHaveBeenCalledWith(expectedSql1)
      expect(result.statusCode).toBe(200)
      expect(result.headers).toEqual({
        'Access-Control-Allow-Origin': '*',
        'Access-Control-Allow-Credentials': true,
        'Access-Control-Expose-Headers': 'link',
        'Content-Type': "'application/json'"
      })
      expect(JSON.parse(result.body)).toEqual(
        [{code: 'height', label: 'Height', recordsHeld: 123}]
      )
    })
  })
})

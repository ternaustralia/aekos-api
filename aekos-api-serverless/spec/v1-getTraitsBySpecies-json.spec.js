'use strict'
let StubDB = require('./StubDB')

describe('/v1/getTraitsBySpecies-json', () => {
  let objectUnderTest = require('../traitsBySpecies-json')

  describe('.doHandle()', () => {
    let expectedSql = `
    SELECT t.traitName AS code, count(*) AS recordsHeld
    FROM species AS s
    INNER JOIN traits AS t
    ON t.parentId = s.id
    AND (
      s.scientificName IN ('species one')
      OR s.taxonRemarks IN ('species one')
    )
    GROUP BY 1
    ORDER BY 1
    LIMIT 20 OFFSET 0;`

    let result = null
    let stubDb = new StubDB()
    beforeEach(done => {
      let event = {
        queryStringParameters: null,
        body: JSON.stringify({speciesNames: ['species one']}),
        requestContext: {
          path: '/v1/getTraitBySpecies.json'
        },
        headers: {
          Host: 'api.aekos.org.au',
          'X-Forwarded-Proto': 'https'
        }
      }
      stubDb.setExecSelectPromiseResponses([
        [{ code: 'height', recordsHeld: 12 }],
        [{ totalRecords: 12 }]
      ])
      spyOn(stubDb, 'execSelectPromise').and.callThrough()
      let callback = (_, theResult) => {
        result = theResult
        done()
      }
      objectUnderTest._testonly.doHandle(event, callback, stubDb)
    })
    it('should build the expected SQL from a single species name and no paging params', () => {
      expect(result.statusCode).toBe(200)
      expect(result.headers).toEqual({
        'Access-Control-Allow-Origin': '*',
        'Access-Control-Allow-Credentials': true,
        'Access-Control-Expose-Headers': 'link',
        'Content-Type': "'application/json'",
        'link': ''
      })
      expect(JSON.parse(result.body)).toEqual([
        { code: 'height', label: 'Height', recordsHeld: 12 }
      ])
      expect(stubDb.execSelectPromise).toHaveBeenCalledWith(expectedSql)
    })
  })
})

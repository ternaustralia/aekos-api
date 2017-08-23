'use strict'
let StubDB = require('./StubDB')

describe('/v1/getSpeciesByTrait-json', () => {
  let uberRouter = require('../uberRouter')

  describe('.doHandle()', () => {
    let expectedSql = `
    SELECT speciesName AS name, recordsHeld
    FROM traitcounts
    WHERE traitName IN ('trait one')
    ORDER BY 1
    LIMIT 20 OFFSET 0;`

    let result = null
    let stubDb = new StubDB()
    beforeEach(done => {
      let event = {
        queryStringParameters: null,
        body: JSON.stringify({traitNames: ['trait one']}),
        requestContext: { path: '/v1/getSpeciesByTrait.json' },
        path: '/v1/getSpeciesByTrait.json',
        headers: {
          Host: 'api.aekos.org.au',
          'X-Forwarded-Proto': 'https'
        }
      }
      stubDb.setExecSelectPromiseResponses([
        [{ speciesName: 'species one', recordsHeld: 123 }],
        [{ totalRecords: 19 }]
      ])
      spyOn(stubDb, 'execSelectPromise').and.callThrough()
      let callback = (_, theResult) => {
        result = theResult
        done()
      }
      uberRouter._testonly.doHandle(event, callback, stubDb)
    })
    it('should build the expected SQL from a single trait name and no paging params', () => {
      expect(result.statusCode).toBe(200)
      expect(result.headers).toEqual({
        'Access-Control-Allow-Origin': '*',
        'Access-Control-Allow-Credentials': true,
        'Access-Control-Expose-Headers': 'link',
        'Content-Type': "'application/json'",
        'link': ''
      })
      expect(JSON.parse(result.body)).toEqual([
        { speciesName: 'species one', recordsHeld: 123, id: 'notusedanymore' }
      ])
      expect(stubDb.execSelectPromise).toHaveBeenCalledWith(expectedSql)
    })
  })

  describe('.doHandle()', () => {
    let expectedSql = `
    SELECT speciesName AS name, recordsHeld
    FROM traitcounts
    WHERE traitName IN ('trait one','trait two')
    ORDER BY 1
    LIMIT 50 OFFSET 100;`

    let result = null
    let stubDb = new StubDB()
    beforeEach(done => {
      let event = {
        queryStringParameters: {
          pageNum: '3',
          pageSize: '50'
        },
        body: JSON.stringify({traitNames: ['trait one', 'trait two']}),
        requestContext: { path: '/v1/getSpeciesByTrait.json' },
        path: '/v1/getSpeciesByTrait.json',
        headers: {
          Host: 'api.aekos.org.au',
          'X-Forwarded-Proto': 'https'
        }
      }
      stubDb.setExecSelectPromiseResponses([
        [{ speciesName: 'species one', recordsHeld: 123 }],
        [{ totalRecords: 202 }]
      ])
      spyOn(stubDb, 'execSelectPromise').and.callThrough()
      let callback = (_, theResult) => {
        result = theResult
        done()
      }
      uberRouter._testonly.doHandle(event, callback, stubDb)
    })
    it('should build the expected SQL from multiple trait names and paging params', () => {
      expect(result.statusCode).toBe(200)
      expect(result.headers).toEqual({
        'Access-Control-Allow-Origin': '*',
        'Access-Control-Allow-Credentials': true,
        'Access-Control-Expose-Headers': 'link',
        'Content-Type': "'application/json'",
        'link': '<https://api.aekos.org.au/v1/getSpeciesByTrait.json?pageNum=4&pageSize=50>; rel="next", ' +
                '<https://api.aekos.org.au/v1/getSpeciesByTrait.json?pageNum=2&pageSize=50>; rel="prev", ' +
                '<https://api.aekos.org.au/v1/getSpeciesByTrait.json?pageNum=1&pageSize=50>; rel="first", ' +
                '<https://api.aekos.org.au/v1/getSpeciesByTrait.json?pageNum=5&pageSize=50>; rel="last"'
      })
      expect(JSON.parse(result.body)).toEqual([
        { speciesName: 'species one', recordsHeld: 123, id: 'notusedanymore' }
      ])
      expect(stubDb.execSelectPromise).toHaveBeenCalledWith(expectedSql)
    })
  })
})

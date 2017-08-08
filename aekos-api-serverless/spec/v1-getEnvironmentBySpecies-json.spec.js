'use strict'
let objectUnderTest = require('../environmentBySpecies-json')
let StubDB = require('./StubDB')

describe('/v1/getEnvironmentBySpecies-json', function () {
  describe('.doHandle()', () => {
    let expectedSql = `
    SELECT v.varName AS code, count(*) AS recordsHeld
    FROM species AS s
    INNER JOIN env AS e
    ON s.locationID = e.locationID
    AND s.eventDate = e.eventDate
    AND (
      s.scientificName IN ('species one')
      OR s.taxonRemarks IN ('species one')
    )
    INNER JOIN envvars AS v
    ON v.locationID = e.locationID
    AND v.eventDate = e.eventDate
    GROUP BY 1
    ORDER BY 1
    LIMIT 20 OFFSET 0;`
    let result = null
    let stubDb = new StubDB()
    beforeEach(done => {
      stubDb.setExecSelectPromiseResponses([
        [{ code: 'clay', recordsHeld: 123 }]
      ])
      spyOn(stubDb, 'execSelectPromise').and.callThrough()
      let event = {
        queryStringParameters: null,
        body: JSON.stringify({ speciesNames: ['species one'] }),
        requestContext: {
          path: '/v1/getEnvironmentBySpecies.json'
        }
      }
      let callback = (_, theResult) => {
        result = theResult
        done()
      }
      objectUnderTest._testonly.doHandle(event, callback, stubDb)
    })

    it('should return the expected result', () => {
      expect(result.statusCode).toBe(200)
      expect(result.headers).toEqual({
        'Access-Control-Allow-Origin': '*',
        'Access-Control-Allow-Credentials': true,
        'Access-Control-Expose-Headers': 'link',
        'Content-Type': "'application/json'"
      })
      expect(JSON.parse(result.body)).toEqual([
        { code: 'clay', recordsHeld: 123, label: 'Clay Content' }
      ])
      expect(stubDb.execSelectPromise).toHaveBeenCalledWith(expectedSql)
    })
  })

  describe('.doHandle()', () => {
    let expectedSql = `
    SELECT v.varName AS code, count(*) AS recordsHeld
    FROM species AS s
    INNER JOIN env AS e
    ON s.locationID = e.locationID
    AND s.eventDate = e.eventDate
    AND (
      s.scientificName IN ('species one','species two')
      OR s.taxonRemarks IN ('species one','species two')
    )
    INNER JOIN envvars AS v
    ON v.locationID = e.locationID
    AND v.eventDate = e.eventDate
    GROUP BY 1
    ORDER BY 1
    LIMIT 30 OFFSET 30;`
    let result = null
    let stubDb = new StubDB()
    beforeEach(done => {
      stubDb.setExecSelectPromiseResponses([
        [{ code: 'clay', recordsHeld: 123 }]
      ])
      spyOn(stubDb, 'execSelectPromise').and.callThrough()
      let event = {
        queryStringParameters: {
          pageNum: '2',
          pageSize: '30'
        },
        body: JSON.stringify({ speciesNames: ['species one', 'species two'] }),
        requestContext: {
          path: '/v1/getEnvironmentBySpecies.json'
        }
      }
      let callback = (_, theResult) => {
        result = theResult
        done()
      }
      objectUnderTest._testonly.doHandle(event, callback, stubDb)
    })

    it('should use the paging params in the built SQL', () => {
      expect(result.statusCode).toBe(200)
      expect(result.headers).toEqual({
        'Access-Control-Allow-Origin': '*',
        'Access-Control-Allow-Credentials': true,
        'Access-Control-Expose-Headers': 'link',
        'Content-Type': "'application/json'"
      })
      expect(JSON.parse(result.body)).toEqual([
        { code: 'clay', recordsHeld: 123, label: 'Clay Content' }
      ])
      expect(stubDb.execSelectPromise).toHaveBeenCalledWith(expectedSql)
    })
  })
})

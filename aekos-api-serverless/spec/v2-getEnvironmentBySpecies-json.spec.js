'use strict'
let objectUnderTest = require('../environmentBySpecies-json')
let StubDB = require('./StubDB')

describe('/v2/getEnvironmentBySpecies-json', function () {
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
        [{ code: 'clay', recordsHeld: 123 }],
        [{ totalRecords: 19 }]
      ])
      spyOn(stubDb, 'execSelectPromise').and.callThrough()
      let event = {
        queryStringParameters: null,
        body: JSON.stringify({ speciesNames: ['species one'] }),
        requestContext: {
          path: '/v2/getEnvironmentBySpecies.json'
        },
        headers: {
          Host: 'api.aekos.org.au',
          'X-Forwarded-Proto': 'https'
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
        'Content-Type': "'application/json'",
        'link': ''
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
        [{ code: 'clay', recordsHeld: 123 }],
        [{ totalRecords: 99 }]
      ])
      spyOn(stubDb, 'execSelectPromise').and.callThrough()
      let event = {
        queryStringParameters: {
          pageNum: '2',
          pageSize: '30'
        },
        body: JSON.stringify({ speciesNames: ['species one', 'species two'] }),
        requestContext: {
          path: '/v2/getEnvironmentBySpecies.json'
        },
        headers: {
          Host: 'api.aekos.org.au',
          'X-Forwarded-Proto': 'https'
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
        'Content-Type': "'application/json'",
        'link': '<https://api.aekos.org.au/v2/getEnvironmentBySpecies.json?pageNum=3&pageSize=30>; rel="next", ' +
                '<https://api.aekos.org.au/v2/getEnvironmentBySpecies.json?pageNum=1&pageSize=30>; rel="prev", ' +
                '<https://api.aekos.org.au/v2/getEnvironmentBySpecies.json?pageNum=1&pageSize=30>; rel="first", ' +
                '<https://api.aekos.org.au/v2/getEnvironmentBySpecies.json?pageNum=4&pageSize=30>; rel="last"'
      })
      expect(JSON.parse(result.body)).toEqual([
        { code: 'clay', recordsHeld: 123, label: 'Clay Content' }
      ])
      expect(stubDb.execSelectPromise).toHaveBeenCalledWith(expectedSql)
    })
  })

  describe('validator', () => {
    it('should validate with a single species name', () => {
      let requestBody = {
        speciesNames: ['species one']
      }
      let result = objectUnderTest._testonly.validator(null, requestBody)
      expect(result.isValid).toBe(true)
    })

    it('should fail validation with no species names', () => {
      let requestBody = {
        // no 'speciesNames'
      }
      let result = objectUnderTest._testonly.validator(null, requestBody)
      expect(result.isValid).toBe(false)
    })

    it('should validate with a single species name and paging params', () => {
      let requestBody = {
        speciesNames: ['species one']
      }
      let queryString = {
        pageSize: '33',
        pageNum: '3'
      }
      let result = objectUnderTest._testonly.validator(queryString, requestBody)
      expect(result.isValid).toBe(true)
    })
  })
})

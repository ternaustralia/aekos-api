'use strict'
let StubDB = require('./StubDB')

describe('/v2/getTraitsBySpecies-json', () => {
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
          path: '/v2/getTraitBySpecies.json'
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

  describe('.doHandle()', () => {
    let expectedSql = `
    SELECT t.traitName AS code, count(*) AS recordsHeld
    FROM species AS s
    INNER JOIN traits AS t
    ON t.parentId = s.id
    AND (
      s.scientificName IN ('species one','species two')
      OR s.taxonRemarks IN ('species one','species two')
    )
    GROUP BY 1
    ORDER BY 1
    LIMIT 10 OFFSET 10;`

    let result = null
    let stubDb = new StubDB()
    beforeEach(done => {
      let event = {
        queryStringParameters: {
          pageNum: '2',
          pageSize: '10'
        },
        body: JSON.stringify({speciesNames: ['species one', 'species two']}),
        requestContext: {
          path: '/v2/getTraitBySpecies.json'
        },
        headers: {
          Host: 'api.aekos.org.au',
          'X-Forwarded-Proto': 'https'
        }
      }
      stubDb.setExecSelectPromiseResponses([
        [{ code: 'height', recordsHeld: 12 }],
        [{ totalRecords: 33 }]
      ])
      spyOn(stubDb, 'execSelectPromise').and.callThrough()
      let callback = (_, theResult) => {
        result = theResult
        done()
      }
      objectUnderTest._testonly.doHandle(event, callback, stubDb)
    })
    it('should build the expected SQL with all params supplied', () => {
      expect(result.statusCode).toBe(200)
      expect(result.headers).toEqual({
        'Access-Control-Allow-Origin': '*',
        'Access-Control-Allow-Credentials': true,
        'Access-Control-Expose-Headers': 'link',
        'Content-Type': "'application/json'",
        'link': '<https://api.aekos.org.au/v2/getTraitBySpecies.json?pageNum=3&pageSize=10>; rel="next", ' +
                '<https://api.aekos.org.au/v2/getTraitBySpecies.json?pageNum=1&pageSize=10>; rel="prev", ' +
                '<https://api.aekos.org.au/v2/getTraitBySpecies.json?pageNum=1&pageSize=10>; rel="first", ' +
                '<https://api.aekos.org.au/v2/getTraitBySpecies.json?pageNum=4&pageSize=10>; rel="last"'
      })
      expect(JSON.parse(result.body)).toEqual([
        { code: 'height', label: 'Height', recordsHeld: 12 }
      ])
      expect(stubDb.execSelectPromise).toHaveBeenCalledWith(expectedSql)
    })
  })

  describe('.getRecordsSql()', () => {
    let expectedSql1 = `
    SELECT t.traitName AS code, count(*) AS recordsHeld
    FROM species AS s
    INNER JOIN traits AS t
    ON t.parentId = s.id
    AND (
      s.scientificName IN ('species two')
      OR s.taxonRemarks IN ('species two')
    )
    GROUP BY 1
    ORDER BY 1
    LIMIT 20 OFFSET 0;`
    it('should return the expected SQL with one trait name', () => {
      let stubDb = new StubDB()
      let result = objectUnderTest._testonly.getRecordsSql("'species two'", 1, 20, stubDb)
      expect(result).toBe(expectedSql1)
    })
  })

  describe('.getCountSql()', () => {
    let expectedSql1 = `
    SELECT count(DISTINCT t.traitName) AS totalRecords
    FROM species AS s
    INNER JOIN traits AS t
    ON t.parentId = s.id
    AND (
      s.scientificName IN ('species two')
      OR s.taxonRemarks IN ('species two')
    );`
    it('should return the expected SQL with one trait name', () => {
      let result = objectUnderTest._testonly.getCountSql("'species two'")
      expect(result).toBe(expectedSql1)
    })
  })

  describe('.validator()', () => {
    it('should pass validation with valid input', () => {
      let requestBody = {
        speciesNames: ['species one']
      }
      let result = objectUnderTest._testonly.validator(null, requestBody)
      expect(result.isValid).toBe(true)
    })

    it('should fail validation with invalid input', () => {
      let requestBody = {
        speciesNames: 'species one' // should be an array
      }
      let result = objectUnderTest._testonly.validator(null, requestBody)
      expect(result.isValid).toBe(false)
    })
  })
})

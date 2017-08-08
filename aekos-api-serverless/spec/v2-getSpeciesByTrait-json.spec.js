'use strict'
let StubDB = require('./StubDB')

describe('/v2/getSpeciesByTrait-json', () => {
  let objectUnderTest = require('../speciesByTrait-json')

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
        requestContext: {
          path: '/v2/getSpeciesByTrait.json'
        }
      }
      stubDb.setExecSelectPromiseResponses([
        [{ speciesName: 'species one', recordsHeld: 123 }]
      ])
      spyOn(stubDb, 'execSelectPromise').and.callThrough()
      let callback = (_, theResult) => {
        result = theResult
        done()
      }
      objectUnderTest._testonly.doHandle(event, callback, stubDb)
    })
    it('should build the expected SQL from a single trait name and no paging params', () => {
      expect(result.statusCode).toBe(200)
      expect(result.headers).toEqual({
        'Access-Control-Allow-Origin': '*',
        'Access-Control-Allow-Credentials': true,
        'Access-Control-Expose-Headers': 'link',
        'Content-Type': "'application/json'"
      })
      expect(JSON.parse(result.body)).toEqual([
        { speciesName: 'species one', recordsHeld: 123 }
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
          pageSize: '50',
          pageNum: '3'
        },
        body: JSON.stringify({traitNames: ['trait one', 'trait two']}),
        requestContext: {
          path: '/v2/getSpeciesByTrait.json'
        }
      }
      stubDb.setExecSelectPromiseResponses([
        [{ speciesName: 'species one', recordsHeld: 123 }]
      ])
      spyOn(stubDb, 'execSelectPromise').and.callThrough()
      let callback = (_, theResult) => {
        result = theResult
        done()
      }
      objectUnderTest._testonly.doHandle(event, callback, stubDb)
    })
    it('should build the expected SQL from multiple trait names and paging params', () => {
      expect(result.statusCode).toBe(200)
      expect(result.headers).toEqual({
        'Access-Control-Allow-Origin': '*',
        'Access-Control-Allow-Credentials': true,
        'Access-Control-Expose-Headers': 'link',
        'Content-Type': "'application/json'"
      })
      expect(JSON.parse(result.body)).toEqual([
        { speciesName: 'species one', recordsHeld: 123 }
      ])
      expect(stubDb.execSelectPromise).toHaveBeenCalledWith(expectedSql)
    })
  })

  describe('.getSql()', () => {
    let expectedSql1 = `
    SELECT speciesName AS name, recordsHeld
    FROM traitcounts
    WHERE traitName IN ('height')
    ORDER BY 1
    LIMIT 20 OFFSET 0;`
    it('should return the expected SQL with one trait name', () => {
      let stubDb = new StubDB()
      let result = objectUnderTest._testonly.getSql("'height'", 1, 20, stubDb)
      expect(result).toBe(expectedSql1)
    })
  })

  describe('.validator()', () => {
    it('should pass validation with valid input', () => {
      let requestBody = {
        traitNames: ['trait one']
      }
      let result = objectUnderTest._testonly.validator(null, requestBody)
      expect(result.isValid).toBe(true)
    })

    it('should fail validation with invalid input', () => {
      let requestBody = {
        traitNames: 'trait one' // should be an array
      }
      let result = objectUnderTest._testonly.validator(null, requestBody)
      expect(result.isValid).toBe(false)
    })
  })
})

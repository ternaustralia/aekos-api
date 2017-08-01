'use strict'
let StubDB = require('./StubDB')

describe('/v1/getSpeciesByTrait-json', () => {
  let objectUnderTest = require('../v1-getSpeciesByTrait-json')

  describe('.doHandle()', () => {
    let expectedSql1 = `
    SELECT speciesName AS name, recordsHeld, 'notusedanymore' AS id
    FROM traitcounts
    WHERE traitName IN ('trait one')
    ORDER BY 1
    LIMIT 20 OFFSET 0;`
    it('should build the expected SQL from a single trait name and no paging params', () => {
      let queryStringParameters = null
      let requestBody = {
        traitNames: ['trait one']
      }
      let stubDb = new StubDB()
      stubDb.setExecSelectPromiseResponses([{ someField: 123 }])
      spyOn(stubDb, 'execSelectPromise').and.callThrough()
      objectUnderTest._testonly.responder(requestBody, stubDb, queryStringParameters).then(result => {
        expect(result).toEqual({ someField: 123 })
      })
      expect(stubDb.execSelectPromise).toHaveBeenCalledWith(expectedSql1)
    })
  })

  describe('.getSql()', () => {
    let expectedSql1 = `
    SELECT speciesName AS name, recordsHeld, 'notusedanymore' AS id
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

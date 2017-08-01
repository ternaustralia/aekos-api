'use strict'
let StubDB = require('./StubDB')

describe('/v1/getTraitsBySpecies-json', () => {
  let objectUnderTest = require('../v1-getTraitsBySpecies-json')

  describe('.doHandle()', () => {
    let expectedSql1 = `
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
    it('should build the expected SQL from a single species name and no paging params', () => {
      let queryStringParameters = null
      let requestBody = {
        speciesNames: ['species one']
      }
      let stubDb = new StubDB()
      stubDb.setExecSelectPromiseResponses([
        [{ code: 'height' }]
      ])
      spyOn(stubDb, 'execSelectPromise').and.callThrough()
      objectUnderTest._testonly.responder(requestBody, stubDb, queryStringParameters).then(result => {
        expect(result).toEqual([{
          code: 'height',
          label: 'Height'
        }])
      })
      expect(stubDb.execSelectPromise).toHaveBeenCalledWith(expectedSql1)
    })
  })

  describe('.getSql()', () => {
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
      let result = objectUnderTest._testonly.getSql("'species two'", 1, 20, stubDb)
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

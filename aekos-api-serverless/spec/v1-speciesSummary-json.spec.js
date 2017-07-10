'use strict'
var objectUnderTest = require('../v1-speciesSummary-json')
let StubDB = require('./StubDB')

describe('v1-speciesSummary-json', () => {
  describe('.responder()', () => {
    it('should handle a single element', (done) => {
      let postBody = { speciesNames: ['species one'] }
      let stubDb = new StubDB()
      stubDb.setExecSelectPromiseResponses([
        { speciesName: 'species one', recordsHeld: 123, id: 'notusedanymore' }
      ])
      let result = objectUnderTest._testonly.responder(postBody, stubDb)
      result.then(responseBody => {
        expect(responseBody).toEqual(
          { speciesName: 'species one', recordsHeld: 123, id: 'notusedanymore' }
        )
        done()
      }).catch(error => {
        fail('Responded with error: ' + JSON.stringify(error))
      })
    })
  })

  describe('.validator()', () => {
    it('should validate with one species', () => {
      let requestBody = {
        speciesNames: ['species one']
      }
      let result = objectUnderTest._testonly.validator(requestBody)
      expect(result.isValid).toBe(true)
    })

    it('should be invalid with no species', () => {
      let requestBody = {
        speciesNames: []
      }
      let result = objectUnderTest._testonly.validator(requestBody)
      expect(result.isValid).toBe(false)
    })

    it('should be invalid when the body is not an object', () => {
      let requestBody = 'a string'
      let result = objectUnderTest._testonly.validator(requestBody)
      expect(result.isValid).toBe(false)
    })

    it('should be invalid when the speciesNames field is not present', () => {
      let requestBody = {
        someOtherField: 123
      }
      let result = objectUnderTest._testonly.validator(requestBody)
      expect(result.isValid).toBe(false)
    })

    it('should be invalid when the speciesNames field is the wrong type', () => {
      let requestBody = {
        speciesNames: 'not an array'
      }
      let result = objectUnderTest._testonly.validator(requestBody)
      expect(result.isValid).toBe(false)
    })

    it('should be invalid when the speciesNames field is an array containing the wrong type elements', () => {
      let requestBody = {
        speciesNames: [1, 2, 3]
      }
      let result = objectUnderTest._testonly.validator(requestBody)
      expect(result.isValid).toBe(false)
    })
  })

  describe('.getSql()', () => {
    const expectedSqlWithIndentingThatMatters1 = `
    SELECT speciesName, sum(recordsHeld) AS recordsHeld, 'notusedanymore' AS id
    FROM (
      SELECT scientificName AS speciesName, count(*) AS recordsHeld
      FROM species
      WHERE scientificName in ('species one')
      GROUP BY 1
      UNION
      SELECT taxonRemarks AS speciesName, count(*) AS recordsHeld
      FROM species
      WHERE taxonRemarks in ('species one')
      GROUP BY 1
    ) AS a
    WHERE a.speciesName IS NOT NULL
    GROUP BY 1
    ORDER BY 1;`
    it('should handle a single species', () => {
      let result = objectUnderTest._testonly.getSql(['species one'], new StubDB())
      expect(result).toBe(expectedSqlWithIndentingThatMatters1)
    })

    const expectedSqlWithIndentingThatMatters2 = `
    SELECT speciesName, sum(recordsHeld) AS recordsHeld, 'notusedanymore' AS id
    FROM (
      SELECT scientificName AS speciesName, count(*) AS recordsHeld
      FROM species
      WHERE scientificName in ('species one','species two','species\\' three')
      GROUP BY 1
      UNION
      SELECT taxonRemarks AS speciesName, count(*) AS recordsHeld
      FROM species
      WHERE taxonRemarks in ('species one','species two','species\\' three')
      GROUP BY 1
    ) AS a
    WHERE a.speciesName IS NOT NULL
    GROUP BY 1
    ORDER BY 1;`
    it('should handle multiple species', () => {
      let result = objectUnderTest._testonly.getSql(['species one', 'species two', 'species\' three'], new StubDB())
      expect(result).toBe(expectedSqlWithIndentingThatMatters2)
    })
  })
})
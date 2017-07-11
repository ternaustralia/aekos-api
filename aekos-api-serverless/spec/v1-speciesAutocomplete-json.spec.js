'use strict'

var objectUnderTest = require('../v1-speciesAutocomplete-json')
let StubDB = require('./StubDB')

describe('v1-speciesAutocomplete-json', () => {
  describe('.getSql()', () => {
    let expectedSql1 = `
    SELECT speciesName, sum(recordsHeld) AS recordsHeld, 'notusedanymore' AS id
    FROM (
      SELECT scientificName AS speciesName, count(*) AS recordsHeld
      FROM species
      WHERE scientificName LIKE 'aca%'
      GROUP BY 1
      UNION
      SELECT taxonRemarks AS speciesName, count(*) AS recordsHeld
      FROM species
      WHERE taxonRemarks LIKE 'aca%'
      GROUP BY 1 
    ) AS a
    WHERE speciesName IS NOT NULL
    GROUP BY 1
    ORDER BY 1
    LIMIT 20 OFFSET 0;`
    it('should return the expected SQL with a simple partial name', () => {
      let stubDb = new StubDB()
      let result = objectUnderTest._testonly.getSql('aca', 20, 0, stubDb)
      expect(result).toBe(expectedSql1)
    })

    it('should return the expected SQL with all params supplied', () => {
      let stubDb = new StubDB()
      let result = objectUnderTest._testonly.getSql('species one', 50, 100, stubDb)
      expect(result).toContain('LIMIT 50 OFFSET 100;')
    })
  })

  describe('.doHandle()', () => {
    let expectedSql1 = `
    SELECT speciesName, sum(recordsHeld) AS recordsHeld, 'notusedanymore' AS id
    FROM (
      SELECT scientificName AS speciesName, count(*) AS recordsHeld
      FROM species
      WHERE scientificName LIKE 'aca%'
      GROUP BY 1
      UNION
      SELECT taxonRemarks AS speciesName, count(*) AS recordsHeld
      FROM species
      WHERE taxonRemarks LIKE 'aca%'
      GROUP BY 1 
    ) AS a
    WHERE speciesName IS NOT NULL
    GROUP BY 1
    ORDER BY 1
    LIMIT 20 OFFSET 0;`
    it('should build the expected SQL from just a partial name param', () => {
      let event = {
        queryStringParameters: {
          q: 'aca'
        }
      }
      let stubDb = new StubDB()
      stubDb.setExecSelectPromiseResponses([{ someField: 123 }])
      spyOn(stubDb, 'execSelectPromise').and.callThrough()
      objectUnderTest._testonly.doHandle(event, null, stubDb)
      expect(stubDb.execSelectPromise).toHaveBeenCalledWith(expectedSql1)
    })

    it('should return the result of the query', () => {
      let event = {
        queryStringParameters: {
          q: 'aca'
        }
      }
      let stubDb = new StubDB()
      stubDb.setExecSelectPromiseResponses([{ someField: 123 }])
      let theCallback = (_, result) => {
        expect(result.statusCode).toBe(200)
        expect(result.headers).toEqual({
          'Access-Control-Allow-Origin': '*',
          'Access-Control-Allow-Credentials': true,
          'Content-Type': "'application/json'"
        })
        expect(result.body).toBe(JSON.stringify({ someField: 123 }))
      }
      objectUnderTest._testonly.doHandle(event, theCallback, stubDb)
    })
  })
})

'use strict'
let objectUnderTest = require('../speciesSummary-json')
let uberRouter = require('../uberRouter')
let StubDB = require('./StubDB')

describe('/v1/speciesSummary-json', () => {
  describe('.doHandle()', () => {
    let result = null
    beforeEach(done => {
      let stubDb = new StubDB()
      stubDb.setExecSelectPromiseResponses([
        [{ speciesName: 'species one', recordsHeld: 123 }]
      ])
      let event = {
        path: '/v2/speciesSummary.json',
        body: JSON.stringify({ speciesNames: ['species one'] })
      }
      let callback = (_, theResult) => {
        result = theResult
        done()
      }
      uberRouter._testonly.doHandle(event, callback, stubDb)
    })

    it('should handle a single element', () => {
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
    })
  })

  describe('.getSql()', () => {
    const expectedSqlWithIndentingThatMatters1 = `
    SELECT speciesName, sum(recordsHeld) AS recordsHeld
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
    SELECT speciesName, sum(recordsHeld) AS recordsHeld
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

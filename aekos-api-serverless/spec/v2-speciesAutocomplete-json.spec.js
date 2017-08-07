'use strict'
var objectUnderTest = require('../speciesAutocomplete-json')
let StubDB = require('./StubDB')

describe('/v2/speciesAutocomplete-json', () => {
  describe('.getSql()', () => {
    let expectedSql1 = `
    SELECT speciesName, sum(recordsHeld) AS recordsHeld
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
    SELECT speciesName, sum(recordsHeld) AS recordsHeld
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
        },
        requestContext: {
          path: '/v2/speciesAutocomplete.json'
        }
      }
      let stubDb = new StubDB()
      stubDb.setExecSelectPromiseResponses([{ someField: 123 }])
      spyOn(stubDb, 'execSelectPromise').and.callThrough()
      objectUnderTest._testonly.doHandle(event, () => {}, stubDb)
      expect(stubDb.execSelectPromise).toHaveBeenCalledWith(expectedSql1)
    })
  })

  describe('.doHandle()', () => {
    let expectedSql1 = `
    SELECT speciesName, sum(recordsHeld) AS recordsHeld
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
    LIMIT 10 OFFSET 10;`
    it('should build the expected SQL from all params', () => {
      let event = {
        queryStringParameters: {
          q: 'aca',
          start: '10',
          rows: '10'
        },
        requestContext: {
          path: '/v2/speciesAutocomplete.json'
        }
      }
      let stubDb = new StubDB()
      stubDb.setExecSelectPromiseResponses([{ someField: 123 }])
      spyOn(stubDb, 'execSelectPromise').and.callThrough()
      objectUnderTest._testonly.doHandle(event, () => {}, stubDb)
      expect(stubDb.execSelectPromise).toHaveBeenCalledWith(expectedSql1)
    })
  })

  describe('.doHandle()', () => {
    let result = null
    beforeEach(done => {
      let event = {
        queryStringParameters: {
          q: 'aca'
        },
        requestContext: {
          path: '/v2/speciesAutocomplete.json'
        }
      }
      let stubDb = new StubDB()
      stubDb.setExecSelectPromiseResponses([
        [{ speciesName: 'acacia whatever', recordsHeld: 123 }]
      ])
      let theCallback = (_, theResult) => {
        result = theResult
        done()
      }
      objectUnderTest._testonly.doHandle(event, theCallback, stubDb)
    })

    it('should return the result of the query', () => {
      expect(result.statusCode).toBe(200)
      expect(result.headers).toEqual({
        'Access-Control-Allow-Origin': '*',
        'Access-Control-Allow-Credentials': true,
        'Access-Control-Expose-Headers': 'link',
        'Content-Type': "'application/json'"
      })
      expect(JSON.parse(result.body)).toEqual(
        [{ speciesName: 'acacia whatever', recordsHeld: 123 }]
      )
    })
  })

  describe('.doHandle()', () => {
    let result = null
    beforeEach(done => {
      let event = {
        queryStringParameters: null // no 'q' param
      }
      let stubDb = new StubDB()
      let theCallback = (_, theResult) => {
        result = theResult
        done()
      }
      objectUnderTest._testonly.doHandle(event, theCallback, stubDb)
    })

    it('should return 400 when we do not supply the q param', () => {
      expect(result.statusCode).toBe(400)
      expect(result.headers).toEqual({
        'Access-Control-Allow-Origin': '*',
        'Access-Control-Allow-Credentials': true,
        'Access-Control-Expose-Headers': 'link',
        'Content-Type': "'application/json'"
      })
      expect(JSON.parse(result.body)).toEqual(
        { message: "the 'q' query string parameter must be supplied", statusCode: 400 }
      )
    })
  })
})

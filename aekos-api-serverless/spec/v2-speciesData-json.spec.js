'use strict'
let objectUnderTest = require('../speciesData-json')
let uberRouter = require('../uberRouter')
let StubDB = require('./StubDB')
let ConsoleSilencer = require('./ConsoleSilencer')
let consoleSilencer = new ConsoleSilencer()

describe('/v2/speciesData-json', () => {
  describe('doHandle', () => {
    let result = null
    beforeEach(done => {
      let stubDb = new StubDB()
      stubDb.setExecSelectPromiseResponses([
        [{
          recordNum: 1,
          locationName: 'location1',
          datasetName: 'dataset1'
        }, {
          recordNum: 2,
          locationName: 'location2',
          datasetName: 'dataset2'
        }],
        [ {recordsHeld: 31} ]
      ])
      let event = {
        body: JSON.stringify({speciesNames: ['species one']}),
        queryStringParameters: {
          rows: '15',
          start: '0'
        },
        headers: {
          Host: 'api.aekos.org.au',
          'X-Forwarded-Proto': 'https'
        },
        requestContext: { path: '/v2/speciesData.json' },
        path: '/v2/speciesData.json'
      }
      let callback = (_, theResult) => {
        result = theResult
        done()
      }
      uberRouter._testonly.doHandle(event, callback, stubDb, () => { return 42 })
    })

    it('should return a 200 response when all params are supplied', () => {
      expect(result.statusCode).toBe(200)
      expect(result.headers).toEqual({
        'Access-Control-Allow-Origin': '*',
        'Access-Control-Allow-Credentials': true,
        'Access-Control-Expose-Headers': 'link',
        'Content-Type': "'application/json'",
        link: '<https://api.aekos.org.au/v2/speciesData.json?rows=15&start=15>; rel="next", ' +
              '<https://api.aekos.org.au/v2/speciesData.json?rows=15&start=30>; rel="last"'
      })
      expect(JSON.parse(result.body)).toEqual({
        responseHeader: {
          elapsedTime: 42,
          numFound: 31,
          pageNumber: 1,
          params: {
            rows: 15,
            start: 0,
            speciesNames: ['species one']
          },
          totalPages: 3
        },
        response: [
          {
            recordNum: 1,
            locationName: 'location1',
            datasetName: 'dataset1'
          }, {
            recordNum: 2,
            locationName: 'location2',
            datasetName: 'dataset2'
          }
        ]
      })
    })
  })

  describe('doHandle', () => {
    let result = null
    beforeEach(done => {
      let stubDb = new StubDB()
      stubDb.setExecSelectPromiseResponses([
        [ {recordNum: 1} ],
        [ {recordsHeld: 31} ]
      ])
      let event = {
        body: JSON.stringify({speciesNames: ['species one']}),
        queryStringParameters: {
          start: '0'
        },
        headers: {
          Host: 'api.aekos.org.au',
          'X-Forwarded-Proto': 'https'
        },
        requestContext: { path: '/v2/speciesData.json' },
        path: '/v2/speciesData.json'
      }
      let callback = (_, theResult) => {
        result = theResult
        done()
      }
      uberRouter._testonly.doHandle(event, callback, stubDb, () => { return 0 })
    })

    it('should calculate paging information when only start is provided', () => {
      expect(result.statusCode).toBe(200)
      let responseHeader = JSON.parse(result.body).responseHeader
      expect(responseHeader.pageNumber).toBe(1)
      expect(responseHeader.totalPages).toBe(2)
    })
  })

  describe('doHandle', () => {
    let result = null
    beforeEach(done => {
      let stubDb = new StubDB()
      stubDb.setExecSelectPromiseResponses([
        [ {recordNum: 1} ],
        [ {recordsHeld: 14} ]
      ])
      let event = {
        body: JSON.stringify({speciesNames: ['species one']}),
        queryStringParameters: {
          start: '10' // start is less than the default rows value
        },
        headers: {
          Host: 'api.aekos.org.au',
          'X-Forwarded-Proto': 'https'
        },
        requestContext: { path: '/v2/speciesData.json' },
        path: '/v2/speciesData.json'
      }
      let callback = (_, theResult) => {
        result = theResult
        done()
      }
      uberRouter._testonly.doHandle(event, callback, stubDb, () => { return 0 })
    })

    it('should behave weirdly when the user supplies wierd params', () => {
      expect(result.statusCode).toBe(200)
      let responseHeader = JSON.parse(result.body).responseHeader
      expect(responseHeader.params.rows).toBe(20) // we get default rows which is greater than the start
      expect(responseHeader.pageNumber).toBe(1) // should probably be 2 but it's a weird edge case
      expect(responseHeader.totalPages).toBe(1) // are there 1,2 or 3 pages?
    })
  })

  describe('doHandle', () => {
    let result = null
    beforeEach(done => {
      let stubDb = new StubDB()
      stubDb.setExecSelectPromiseResponses([
        [ /* no records, will cause Error */ ],
        [ {recordsHeld: 9} ]
      ])
      let event = {
        body: JSON.stringify({speciesNames: ['species one']}),
        queryStringParameters: null,
        headers: {
          Host: 'api.aekos.org.au',
          'X-Forwarded-Proto': 'https'
        },
        requestContext: { path: '/v2/speciesData.json' },
        path: '/v2/speciesData.json'
      }
      let callback = (_, theResult) => {
        consoleSilencer.resetConsoleError()
        result = theResult
        done()
      }
      consoleSilencer.silenceConsoleError()
      uberRouter._testonly.doHandle(event, callback, stubDb, () => { return 0 })
    })

    it('should catch an error thrown during query result processing and respond with a 500', () => {
      expect(result.statusCode).toBe(500)
    })
  })

  describe('doHandle', () => {
    let result = null
    beforeEach(done => {
      const invalidJsonString = `{speciesNames:['species one']}`
      let event = {
        body: invalidJsonString,
        queryStringParameters: {
          rows: '15',
          start: '0'
        },
        headers: {
          Host: 'api.aekos.org.au',
          'X-Forwarded-Proto': 'https'
        },
        requestContext: { path: '/v2/speciesData.json' },
        path: '/v2/speciesData.json'
      }
      let callback = (_, theResult) => {
        consoleSilencer.resetConsoleError()
        result = theResult
        done()
      }
      consoleSilencer.silenceConsoleError()
      uberRouter._testonly.doHandle(event, callback, null, () => { return 42 })
    })

    it('should return 400 for invalid JSON supplied in the POST body', () => {
      expect(result.statusCode).toBe(400)
      expect(result.headers).toEqual({
        'Access-Control-Allow-Origin': '*',
        'Access-Control-Allow-Credentials': true,
        'Access-Control-Expose-Headers': 'link',
        'Content-Type': "'application/json'"
      })
    })
  })

  describe('extractParams', () => {
    it('should extract the params when they are present', () => {
      let requestBody = {
        speciesNames: ['species one']
      }
      let queryStringParameters = {
        rows: '15',
        start: '0'
      }
      let result = objectUnderTest.extractParams(requestBody, queryStringParameters, new StubDB())
      expect(result.speciesNames).toBe("'species one'")
      expect(result.unescapedSpeciesNames).toEqual(['species one'])
      expect(result.rows).toBe(15)
      expect(result.start).toBe(0)
    })

    it('should default the paging info', () => {
      let requestBody = {
        speciesNames: ['species two']
      }
      let queryStringParameters = null
      let result = objectUnderTest.extractParams(requestBody, queryStringParameters, new StubDB())
      expect(result.speciesNames).toBe("'species two'")
      expect(result.unescapedSpeciesNames).toEqual(['species two'])
      expect(result.rows).toBe(20)
      expect(result.start).toBe(0)
    })
  })

  let expectedRecordsSql1 = `
    SELECT
    s.scientificName,
    s.taxonRemarks,
    s.individualCount,
    s.eventDate,
    e.\`month\`,
    e.\`year\`,
    e.decimalLatitude,
    e.decimalLongitude,
    e.geodeticDatum,
    s.locationID,
    e.locationName,
    e.samplingProtocol,
    c.bibliographicCitation,
    c.datasetName
    FROM (
      SELECT id
      FROM species
      WHERE (
        scientificName IN ('species one')
        OR taxonRemarks IN ('species one')
      )
      ORDER BY 1
      LIMIT 33 OFFSET 0
    ) AS lateRowLookup
    INNER JOIN species AS s
    ON lateRowLookup.id = s.id
    LEFT JOIN env AS e
    ON s.locationID = e.locationID
    AND s.eventDate = e.eventDate
    LEFT JOIN citations AS c
    ON e.samplingProtocol = c.samplingProtocol;`
  describe('getRecordsSql', () => {
    it('should be able to handle a single species', () => {
      let result = objectUnderTest._testonly.getRecordsSql("'species one'", 0, 33)
      expect(result).toBe(expectedRecordsSql1)
    })

    it('should throw an error when we do not supply a species', () => {
      let undefinedSpeciesName
      expect(() => {
        objectUnderTest._testonly.getRecordsSql(undefinedSpeciesName)
      }).toThrow()
    })
  })

  let expectedCountSql1 = `
    SELECT count(*) AS recordsHeld
    FROM species AS s
      WHERE (
        scientificName IN ('species one', 'species two')
        OR taxonRemarks IN ('species one', 'species two')
      );`
  describe('getCountSql', () => {
    it('should be able to handle supplied species', () => {
      let result = objectUnderTest._testonly.getCountSql("'species one', 'species two'")
      expect(result).toBe(expectedCountSql1)
    })
  })
})

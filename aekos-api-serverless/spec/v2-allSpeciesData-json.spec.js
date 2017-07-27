'use strict'
let objectUnderTest = require('../allSpeciesData-json')
let StubDB = require('./StubDB')
let ConsoleSilencer = require('./ConsoleSilencer')
let consoleSilencer = new ConsoleSilencer()

describe('/v2/allSpeciesData-json', () => {
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
        [{ recordsHeld: 31 }]
      ])
      let event = {
        queryStringParameters: {
          rows: '15',
          start: '0'
        },
        headers: {
          Host: 'api.aekos.org.au',
          'X-Forwarded-Proto': 'https'
        },
        requestContext: {
          path: '/v2/allSpeciesData.json'
        }
      }
      let callback = (_, theResult) => {
        result = theResult
        done()
      }
      objectUnderTest._testonly.doHandle(event, callback, stubDb, () => { return 42 })
    })

    it('should return a 200 response when all params are supplied', () => {
      expect(result.statusCode).toBe(200)
      expect(result.headers).toEqual({
        'Access-Control-Allow-Origin': '*',
        'Access-Control-Allow-Credentials': true,
        'Content-Type': "'application/json'",
        link: '<https://api.aekos.org.au/v2/allSpeciesData.json?rows=15&start=15>; rel="next", ' +
              '<https://api.aekos.org.au/v2/allSpeciesData.json?rows=15&start=30>; rel="last"'
      })
      expect(JSON.parse(result.body)).toEqual({
        responseHeader: {
          elapsedTime: 42,
          numFound: 31,
          pageNumber: 1,
          params: {
            rows: 15,
            start: 0
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
        [{ recordNum: 1 }, { recordNum: 2 }],
        [{ recordsHeld: 31 }]
      ])
      let event = {
        queryStringParameters: null, // no params
        headers: {
          Host: 'api.aekos.org.au',
          'X-Forwarded-Proto': 'https'
        },
        requestContext: {
          path: '/v2/allSpeciesData.json'
        }
      }
      let callback = (_, theResult) => {
        result = theResult
        done()
      }
      objectUnderTest._testonly.doHandle(event, callback, stubDb, () => { return 42 })
    })

    it('should return a 200 response and assume defaults when no parameters are supplied', () => {
      expect(result.statusCode).toBe(200)
      expect(result.headers).toEqual({
        'Access-Control-Allow-Origin': '*',
        'Access-Control-Allow-Credentials': true,
        'Content-Type': "'application/json'",
        link: '<https://api.aekos.org.au/v2/allSpeciesData.json?start=20>; rel="next", ' +
              '<https://api.aekos.org.au/v2/allSpeciesData.json?start=20>; rel="last"'
      })
      expect(JSON.parse(result.body)).toEqual({
        responseHeader: {
          elapsedTime: 42,
          numFound: 31,
          pageNumber: 1,
          params: {
            rows: 20,
            start: 0
          },
          totalPages: 2
        },
        response: [
          { recordNum: 1 },
          { recordNum: 2 }
        ]
      })
    })
  })

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
        [{ recordsHeld: 31 }]
      ])
      let event = {
        queryStringParameters: {
          rows: '15',
          start: '0'
        },
        headers: {
          Host: 'maazptt3zb.execute-api.us-west-1.amazonaws.com',
          'X-Forwarded-Proto': 'https'
        },
        requestContext: {
          path: '/dev/v2/allSpeciesData.json'
        }
      }
      let callback = (_, theResult) => {
        result = theResult
        done()
      }
      objectUnderTest._testonly.doHandle(event, callback, stubDb, () => { return 42 })
    })

    it('should work as expected when we use the out-of-the-box AWS URL including stage in the path (not a custom domain)', () => {
      expect(result.statusCode).toBe(200)
      expect(result.headers).toEqual({
        'Access-Control-Allow-Origin': '*',
        'Access-Control-Allow-Credentials': true,
        'Content-Type': "'application/json'",
        link: '<https://maazptt3zb.execute-api.us-west-1.amazonaws.com/dev/v2/allSpeciesData.json?rows=15&start=15>; rel="next", ' +
              '<https://maazptt3zb.execute-api.us-west-1.amazonaws.com/dev/v2/allSpeciesData.json?rows=15&start=30>; rel="last"'
      })
      expect(JSON.parse(result.body)).toEqual({
        responseHeader: {
          elapsedTime: 42,
          numFound: 31,
          pageNumber: 1,
          params: {
            rows: 15,
            start: 0
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
    let errorMessage = null
    beforeEach(done => {
      let stubDb = new StubDB()
      stubDb.setExecSelectPromiseResponses([
        [ /* no records */ ],
        [{ recordsHeld: 31 }]
      ])
      let event = {
        queryStringParameters: {
          rows: '15',
          start: '0'
        },
        headers: {
          Host: 'api.aekos.org.au',
          'X-Forwarded-Proto': 'https'
        },
        requestContext: {
          path: '/v2/allSpeciesData.json'
        }
      }
      objectUnderTest.prepareResult(event, stubDb, () => { return 42 }).then(_ => {
        fail()
      }).catch(error => {
        errorMessage = error.message
        done()
      })
    })

    it('should throw the expected exception when recordsHeld > 0 but no actual records are returned', () => {
      expect(errorMessage).toBe('Data problem: records.length=0 while numFound=31, suspect the record query is broken')
    })
  })

  describe('extractParams', () => {
    it('should extract the params when they are present', () => {
      let event = {
        queryStringParameters: {
          rows: '15',
          start: '0'
        }
      }
      let result = objectUnderTest.extractParams(event, new StubDB())
      expect(result.rows).toBe(15)
      expect(result.start).toBe(0)
    })

    it('should default the paging info', () => {
      let event = {
        queryStringParameters: {}
      }
      let result = objectUnderTest.extractParams(event, new StubDB())
      expect(result.rows).toBe(20)
      expect(result.start).toBe(0)
    })
  })

  describe('doHandle', () => {
    let result = null
    beforeEach(done => {
      let stubDb = new StubDB()
      spyOn(stubDb, 'execSelectPromise').and.throwError('some error')
      let event = {}
      let callback = (_, theResult) => {
        consoleSilencer.resetConsoleError()
        result = theResult
        done()
      }
      consoleSilencer.silenceConsoleError()
      objectUnderTest._testonly.doHandle(event, callback, stubDb, () => { return 42 })
    })

    it('should return a 500 response when executing a query fails', () => {
      expect(result.statusCode).toBe(500)
      expect(result.headers).toEqual({
        'Access-Control-Allow-Origin': '*',
        'Access-Control-Allow-Credentials': true,
        'Content-Type': "'application/json'"
      })
      expect(JSON.parse(result.body)).toEqual({
        message: 'Sorry about that, something has gone wrong',
        statusCode: 500
      })
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
  let expectedRecordsSql2 = `
    SELECT
    s.id,
    s.scientificName,`
  describe('getRecordsSql', () => {
    it('should be able to handle no where fragment', () => {
      let result = objectUnderTest.getRecordsSql(0, 33, false, '')
      expect(result).toBe(expectedRecordsSql1)
    })

    it('should be able to include the species ID fragment', () => {
      let includeSpeciesId = true
      let result = objectUnderTest.getRecordsSql(0, 33, includeSpeciesId, '')
      expect(result.substr(0, expectedRecordsSql2.length)).toBe(expectedRecordsSql2)
    })
  })
})

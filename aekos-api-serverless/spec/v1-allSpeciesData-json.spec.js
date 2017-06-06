'use strict'
let objectUnderTest = require('../v1-allSpeciesData-json')
let StubDB = require('./StubDB')

describe('v1-allSpeciesData-json', () => {
  describe('doHandle', () => {
    it('should return a 200 response when all params are supplied', (done) => {
      let stubDb = new StubDB()
      stubDb.setExecSelectPromiseResponses([
        [ {recordNum: 1}, {recordNum: 2} ],
        [ {recordsHeld: 31} ]
      ])
      let event = {
        queryStringParameters: {
          rows: '15',
          start: '0'
        }
      }
      let callback = (error, result) => {
        if (error) {
          fail('Responded with error: ' + JSON.stringify(error))
        }
        expect(result.statusCode).toBe(200)
        expect(result.headers).toEqual({
          'Access-Control-Allow-Origin': '*',
          'Access-Control-Allow-Credentials': true,
          'Content-Type': "'application/json'"
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
            {recordNum: 1},
            {recordNum: 2}
          ]
        })
        done()
      }
      objectUnderTest._testonly.doHandle(event, callback, stubDb, () => { return 42 })
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
        queryStringParameters: { }
      }
      let result = objectUnderTest.extractParams(event, new StubDB())
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
    FROM species AS s
    LEFT JOIN env AS e
    ON s.locationID = e.locationID
    AND s.eventDate = e.eventDate
    LEFT JOIN citations AS c
    ON e.samplingProtocol = c.samplingProtocol
    
    ORDER BY 1
    LIMIT 33 OFFSET 0;`
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

'use strict'
let Set = require('collections/set')
let StubDB = require('./StubDB')
let objectUnderTest = require('../environmentData-json')
let uberRouter = require('../uberRouter')

describe('/v2/environmentData-json', () => {
  describe('.doHandle()', () => {
    let expectedRecordsSql = `
    SELECT
    CONCAT(e.locationID, '#', e.eventDate) AS visitKey,
    e.eventDate,
    e.\`month\`,
    e.\`year\`,
    e.decimalLatitude,
    e.decimalLongitude,
    e.geodeticDatum,
    e.locationID,
    e.locationName,
    e.samplingProtocol,
    c.bibliographicCitation,
    c.datasetName,
    v.varName,
    v.varValue,
    v.varUnit
    FROM (
      SELECT DISTINCT
      e.eventDate,
      e.locationID
      FROM species AS s
      INNER JOIN env AS e
      ON s.locationID = e.locationID
      AND s.eventDate = e.eventDate
      AND (
        s.scientificName IN ('species one')
        OR s.taxonRemarks IN ('species one')
      )
      ORDER BY 2,1
      LIMIT 20 OFFSET 0
    ) AS coreData
    INNER JOIN env AS e
    ON coreData.locationID = e.locationID
    AND coreData.eventDate = e.eventDate
    INNER JOIN citations AS c
    ON e.samplingProtocol = c.samplingProtocol
    LEFT JOIN envvars AS v
    ON coreData.locationID = v.locationID
    AND coreData.eventDate = v.eventDate
    ;`
    let expectedCountSql = `
    SELECT count(DISTINCT e.locationID, e.eventDate) AS recordsHeld
    FROM species AS s
    INNER JOIN env AS e
    ON s.locationID = e.locationID
    AND s.eventDate = e.eventDate
    AND (
      s.scientificName IN ('species one')
      OR s.taxonRemarks IN ('species one')
    )
    ;`
    let expectedSpeciesSql = `
    SELECT DISTINCT
    CONCAT(locationID, '#', eventDate) AS visitKey,
    scientificName,
    taxonRemarks
    FROM species
    WHERE (
      scientificName IN ('species one')
      OR taxonRemarks IN ('species one')
    )
    AND (locationID, eventDate) in (('location1','2017-07-07'),
    ('location2','2008-02-02'))
    ORDER BY 1;`
    let result = null
    let stubDb = new StubDB()
    beforeEach(done => {
      const recordsResult = [{
        locationName: 'location1',
        datasetName: 'dataset1',
        visitKey: 'location1#2017-07-07',
        varName: 'windSpeed',
        varValue: '6',
        varUnit: 'km/h'
      }, {
        locationName: 'location1',
        datasetName: 'dataset1',
        visitKey: 'location1#2017-07-07',
        varName: 'ph',
        varValue: '7',
        varUnit: null
      }, {
        locationName: 'location2',
        datasetName: 'dataset2',
        visitKey: 'location2#2008-02-02',
        varName: 'windSpeed',
        varValue: '7',
        varUnit: 'km/h'
      }]
      const countResult = [{ recordsHeld: 31 }]
      const speciesNamesResult = [{
        visitKey: 'location1#2017-07-07',
        scientificName: null,
        taxonRemarks: 'taxon name'
      }, {
        visitKey: 'location2#2008-02-02',
        scientificName: 'sci name',
        taxonRemarks: null
      }]
      stubDb.setExecSelectPromiseResponses([
        recordsResult,
        countResult,
        speciesNamesResult
      ])
      spyOn(stubDb, 'execSelectPromise').and.callThrough()
      let event = {
        body: JSON.stringify({
          speciesNames: ['species one']
          // don't supply 'varNames'
        }),
        queryStringParameters: null,
        headers: {
          Host: 'api.aekos.org.au',
          'X-Forwarded-Proto': 'https'
        },
        requestContext: { path: '/v2/environmentData.json' },
        path: '/v2/environmentData.json'
      }
      let callback = (_, theResult) => {
        result = theResult
        done()
      }
      uberRouter._testonly.doHandle(event, callback, stubDb, () => { return 42 })
    })

    it('should return a 200 response when the minimum required parameters are supplied', () => {
      expect(result.statusCode).toBe(200)
      expect(result.headers).toEqual({
        'Access-Control-Allow-Origin': '*',
        'Access-Control-Allow-Credentials': true,
        'Access-Control-Expose-Headers': 'link',
        'Content-Type': "'application/json'",
        'link': '<https://api.aekos.org.au/v2/environmentData.json?start=20>; rel="next", ' +
                '<https://api.aekos.org.au/v2/environmentData.json?start=20>; rel="last"'
      })
      expect(JSON.parse(result.body)).toEqual({
        responseHeader: {
          elapsedTime: 42,
          numFound: 31,
          pageNumber: 1,
          params: {
            rows: 20,
            start: 0,
            speciesNames: ['species one'],
            varNames: null
          },
          totalPages: 2
        },
        response: [
          {
            variables: [
              { varName: 'windSpeed', varValue: '6', varUnit: 'km/h' },
              { varName: 'ph', varValue: '7', varUnit: null }
            ],
            scientificNames: [],
            taxonRemarks: ['taxon name'],
            locationName: 'location1',
            datasetName: 'dataset1'
          }, {
            variables: [{ varName: 'windSpeed', varValue: '7', varUnit: 'km/h' }],
            scientificNames: ['sci name'],
            taxonRemarks: [],
            locationName: 'location2',
            datasetName: 'dataset2'
          }
        ]
      })
      expect(stubDb.execSelectPromise).toHaveBeenCalledWith(expectedRecordsSql)
      expect(stubDb.execSelectPromise).toHaveBeenCalledWith(expectedCountSql)
      expect(stubDb.execSelectPromise).toHaveBeenCalledWith(expectedSpeciesSql)
    })
  })

  describe('.doHandle()', () => {
    let expectedRecordsSql = `
    SELECT
    CONCAT(e.locationID, '#', e.eventDate) AS visitKey,
    e.eventDate,
    e.\`month\`,
    e.\`year\`,
    e.decimalLatitude,
    e.decimalLongitude,
    e.geodeticDatum,
    e.locationID,
    e.locationName,
    e.samplingProtocol,
    c.bibliographicCitation,
    c.datasetName,
    v.varName,
    v.varValue,
    v.varUnit
    FROM (
      SELECT DISTINCT
      e.eventDate,
      e.locationID
      FROM species AS s
      INNER JOIN env AS e
      ON s.locationID = e.locationID
      AND s.eventDate = e.eventDate
      AND (
        s.scientificName IN ('species one','species two')
        OR s.taxonRemarks IN ('species one','species two')
      )
      INNER JOIN envvars AS v
      ON v.locationID = e.locationID
      AND v.eventDate = e.eventDate
      AND v.varName IN ('windSpeed','ph')
      ORDER BY 2,1
      LIMIT 10 OFFSET 20
    ) AS coreData
    INNER JOIN env AS e
    ON coreData.locationID = e.locationID
    AND coreData.eventDate = e.eventDate
    INNER JOIN citations AS c
    ON e.samplingProtocol = c.samplingProtocol
    LEFT JOIN envvars AS v
    ON coreData.locationID = v.locationID
    AND coreData.eventDate = v.eventDate
    AND v.varName IN ('windSpeed','ph');`
    let expectedCountSql = `
    SELECT count(DISTINCT e.locationID, e.eventDate) AS recordsHeld
    FROM species AS s
    INNER JOIN env AS e
    ON s.locationID = e.locationID
    AND s.eventDate = e.eventDate
    AND (
      s.scientificName IN ('species one','species two')
      OR s.taxonRemarks IN ('species one','species two')
    )
      INNER JOIN envvars AS v
      ON v.locationID = e.locationID
      AND v.eventDate = e.eventDate
      AND v.varName IN ('windSpeed','ph')
    ;`
    let expectedSpeciesSql = `
    SELECT DISTINCT
    CONCAT(locationID, '#', eventDate) AS visitKey,
    scientificName,
    taxonRemarks
    FROM species
    WHERE (
      scientificName IN ('species one','species two')
      OR taxonRemarks IN ('species one','species two')
    )
    AND (locationID, eventDate) in (('location1','2017-07-07'),
    ('location2','2008-02-02'))
    ORDER BY 1;`
    let result = null
    let stubDb = new StubDB()
    beforeEach(done => {
      const recordsResult = [{
        locationName: 'location1',
        datasetName: 'dataset1',
        visitKey: 'location1#2017-07-07',
        varName: 'windSpeed',
        varValue: '6',
        varUnit: 'km/h'
      }, {
        locationName: 'location1',
        datasetName: 'dataset1',
        visitKey: 'location1#2017-07-07',
        varName: 'ph',
        varValue: '7',
        varUnit: null
      }, {
        locationName: 'location2',
        datasetName: 'dataset2',
        visitKey: 'location2#2008-02-02',
        varName: 'windSpeed',
        varValue: '7',
        varUnit: 'km/h'
      }]
      const countResult = [{ recordsHeld: 41 }]
      const speciesNamesResult = [{
        visitKey: 'location1#2017-07-07',
        scientificName: null,
        taxonRemarks: 'species two'
      }, {
        visitKey: 'location2#2008-02-02',
        scientificName: 'species one',
        taxonRemarks: null
      }]
      stubDb.setExecSelectPromiseResponses([
        recordsResult,
        countResult,
        speciesNamesResult
      ])
      spyOn(stubDb, 'execSelectPromise').and.callThrough()
      let event = {
        body: JSON.stringify({
          speciesNames: ['species one', 'species two'],
          varNames: ['windSpeed', 'ph']
        }),
        queryStringParameters: {
          start: '20',
          rows: '10'
        },
        headers: {
          Host: 'api.aekos.org.au',
          'X-Forwarded-Proto': 'https'
        },
        requestContext: { path: '/v2/environmentData.json' },
        path: '/v2/environmentData.json'
      }
      let callback = (_, theResult) => {
        result = theResult
        done()
      }
      uberRouter._testonly.doHandle(event, callback, stubDb, () => { return 42 })
    })

    it('should return a 200 response when all parameters are supplied', () => {
      expect(result.statusCode).toBe(200)
      expect(result.headers).toEqual({
        'Access-Control-Allow-Origin': '*',
        'Access-Control-Allow-Credentials': true,
        'Access-Control-Expose-Headers': 'link',
        'Content-Type': "'application/json'",
        'link': '<https://api.aekos.org.au/v2/environmentData.json?start=30&rows=10>; rel="next", ' +
                '<https://api.aekos.org.au/v2/environmentData.json?start=10&rows=10>; rel="prev", ' +
                '<https://api.aekos.org.au/v2/environmentData.json?start=0&rows=10>; rel="first", ' +
                '<https://api.aekos.org.au/v2/environmentData.json?start=40&rows=10>; rel="last"'
      })
      expect(JSON.parse(result.body)).toEqual({
        responseHeader: {
          elapsedTime: 42,
          numFound: 41,
          pageNumber: 3,
          params: {
            rows: 10,
            start: 20,
            speciesNames: ['species one', 'species two'],
            varNames: ['windSpeed', 'ph']
          },
          totalPages: 5
        },
        response: [
          {
            variables: [
              { varName: 'windSpeed', varValue: '6', varUnit: 'km/h' },
              { varName: 'ph', varValue: '7', varUnit: null }
            ],
            scientificNames: [],
            taxonRemarks: ['species two'],
            locationName: 'location1',
            datasetName: 'dataset1'
          }, {
            variables: [{ varName: 'windSpeed', varValue: '7', varUnit: 'km/h' }],
            scientificNames: ['species one'],
            taxonRemarks: [],
            locationName: 'location2',
            datasetName: 'dataset2'
          }
        ]
      })
      expect(stubDb.execSelectPromise).toHaveBeenCalledWith(expectedRecordsSql)
      expect(stubDb.execSelectPromise).toHaveBeenCalledWith(expectedCountSql)
      expect(stubDb.execSelectPromise).toHaveBeenCalledWith(expectedSpeciesSql)
    })
  })

  describe('.doHandle()', () => {
    let result = null
    beforeEach(done => {
      let stubDb = new StubDB()
      const recordsResult = [{
        locationName: 'location1',
        datasetName: 'dataset1',
        visitKey: 'location1#2017-07-07',
        varName: null,
        varValue: null,
        varUnit: null
      }]
      const countResult = [{ recordsHeld: 1 }]
      const speciesNamesResult = [{
        visitKey: 'location1#2017-07-07',
        scientificName: null,
        taxonRemarks: 'taxon name'
      }]
      stubDb.setExecSelectPromiseResponses([
        recordsResult,
        countResult,
        speciesNamesResult
      ])
      let event = {
        body: JSON.stringify({
          speciesNames: ['species one']
          // don't supply 'varNames'
        }),
        queryStringParameters: null,
        headers: {
          Host: 'api.aekos.org.au',
          'X-Forwarded-Proto': 'https'
        },
        requestContext: { path: '/v2/environmentData.json' },
        path: '/v2/environmentData.json'
      }
      let callback = (_, theResult) => {
        result = theResult
        done()
      }
      uberRouter._testonly.doHandle(event, callback, stubDb, () => { return 42 })
    })

    it('should handle site visit records that have no variables', () => {
      expect(result.statusCode).toBe(200)
      expect(result.headers).toEqual({
        'Access-Control-Allow-Origin': '*',
        'Access-Control-Allow-Credentials': true,
        'Access-Control-Expose-Headers': 'link',
        'Content-Type': "'application/json'",
        'link': ''
      })
      expect(JSON.parse(result.body)).toEqual({
        responseHeader: {
          elapsedTime: 42,
          numFound: 1,
          pageNumber: 1,
          params: {
            rows: 20,
            start: 0,
            speciesNames: ['species one'],
            varNames: null
          },
          totalPages: 1
        },
        response: [
          {
            variables: [],
            scientificNames: [],
            taxonRemarks: ['taxon name'],
            locationName: 'location1',
            datasetName: 'dataset1'
          }
        ]
      })
    })
  })

  describe('.doHandle()', () => {
    let result = null
    beforeEach(done => {
      let stubDb = new StubDB()
      let event = {
        body: null, // don't supply 'speciesNames'
        queryStringParameters: null,
        requestContext: { path: '/v2/environmentData.json' },
        path: '/v2/environmentData.json'
      }
      let callback = (_, theResult) => {
        result = theResult
        done()
      }
      uberRouter._testonly.doHandle(event, callback, stubDb, () => { return 42 })
    })

    it('should return a 400 response when we do not supply speciesNames', () => {
      expect(result.statusCode).toBe(400)
      expect(result.headers).toEqual({
        'Access-Control-Allow-Origin': '*',
        'Access-Control-Allow-Credentials': true,
        'Access-Control-Expose-Headers': 'link',
        'Content-Type': "'application/json'"
      })
      expect(JSON.parse(result.body)).toEqual({
        message: 'No request body was supplied',
        statusCode: 400
      })
    })
  })

  describe('.validator()', () => {
    it('should be valid with just species names', () => {
      let queryStringObj = null
      let requestBody = {
        speciesNames: ['species one']
      }
      let result = objectUnderTest.validator(queryStringObj, requestBody)
      expect(result.isValid).toBe(true)
    })

    it('should be valid with species names and var names', () => {
      let queryStringObj = null
      let requestBody = {
        speciesNames: ['species one', 'species two'],
        varNames: ['var one', 'var two']
      }
      let result = objectUnderTest.validator(queryStringObj, requestBody)
      expect(result.isValid).toBe(true)
    })

    it('should be invalid without species names', () => {
      let queryStringObj = null
      let requestBody = {
        // no 'speciesNames'
        varNames: ['var one', 'var two']
      }
      let result = objectUnderTest.validator(queryStringObj, requestBody)
      expect(result.isValid).toBe(false)
    })

    it('should be invalid when var names is not an array', () => {
      let queryStringObj = null
      let requestBody = {
        speciesNames: ['species one', 'species two'],
        varNames: 'var one' // not an array
      }
      let result = objectUnderTest.validator(queryStringObj, requestBody)
      expect(result.isValid).toBe(false)
    })
  })

  describe('appendSpeciesNames', () => {
    it('should map species names to records', () => {
      let records = [
        {
          visitKey: 'aekos.org.au/collection/adelaide.edu.au/TAF/NSFSEC0001#2014-05-09',
          eventDate: '2014-05-09',
          month: 5,
          year: 2014,
          decimalLatitude: -36.759165,
          decimalLongitude: 149.435125,
          geodeticDatum: 'GDA94',
          locationID: 'aekos.org.au/collection/adelaide.edu.au/TAF/NSFSEC0001',
          locationName: 'NSFSEC0001',
          samplingProtocol: 'aekos.org.au/collection/adelaide.edu.au/TAF',
          bibliographicCitation: 'Wood SW, Bowman DMJ...',
          datasetName: 'TERN AusPlots Forests Monitoring Network'
        }
      ]
      let speciesNameLookup = {
        'aekos.org.au/collection/adelaide.edu.au/TAF/NSFSEC0001#2014-05-09': {
          scientificNames: new Set(['Acacia dealbata', 'species two']),
          taxonRemarks: new Set(['Big tree', 'Some other taxon remark', 'taxon three'])
        }
      }
      objectUnderTest._testonly.appendSpeciesNames(records, speciesNameLookup)
      let result = records
      let firstRecord = result[0]
      expect(firstRecord.visitKey).toBe('aekos.org.au/collection/adelaide.edu.au/TAF/NSFSEC0001#2014-05-09')
      expect(firstRecord.scientificNames.length).toBe(2)
      expect(firstRecord.scientificNames).toContain('Acacia dealbata', 'species two')
      expect(firstRecord.scientificNames.constructor).toBe(Array)
      expect(firstRecord.taxonRemarks.length).toBe(3)
      expect(firstRecord.taxonRemarks).toContain('Big tree', 'Some other taxon remark', 'taxon three')
      expect(firstRecord.taxonRemarks.constructor).toBe(Array)
    })
  })

  describe('stripVisitKeys', () => {
    it('should remove the visitKey from all records', () => {
      let records = [
        {
          visitKey: 'aekos.org.au/collection/test.edu.au/ONE/RECORD0001#2001-01-01',
          datasetName: 'Record 1'
        }, {
          visitKey: 'aekos.org.au/collection/test.edu.au/TWO/RECORD0002#2002-02-02',
          datasetName: 'Record 2'
        }
      ]
      objectUnderTest._testonly.stripVisitKeys(records)
      let result = records
      result.forEach(e => {
        expect(e.visitKey).toBeUndefined()
      })
    })
  })

  let expectedRecordsSql1 = `
    SELECT
    CONCAT(e.locationID, '#', e.eventDate) AS visitKey,
    e.eventDate,
    e.\`month\`,
    e.\`year\`,
    e.decimalLatitude,
    e.decimalLongitude,
    e.geodeticDatum,
    e.locationID,
    e.locationName,
    e.samplingProtocol,
    c.bibliographicCitation,
    c.datasetName,
    v.varName,
    v.varValue,
    v.varUnit
    FROM (
      SELECT DISTINCT
      e.eventDate,
      e.locationID
      FROM species AS s
      INNER JOIN env AS e
      ON s.locationID = e.locationID
      AND s.eventDate = e.eventDate
      AND (
        s.scientificName IN ('species one')
        OR s.taxonRemarks IN ('species one')
      )
      ORDER BY 2,1
      LIMIT 20 OFFSET 0
    ) AS coreData
    INNER JOIN env AS e
    ON coreData.locationID = e.locationID
    AND coreData.eventDate = e.eventDate
    INNER JOIN citations AS c
    ON e.samplingProtocol = c.samplingProtocol
    LEFT JOIN envvars AS v
    ON coreData.locationID = v.locationID
    AND coreData.eventDate = v.eventDate
    ;`
  let expectedRecordsSql2 = `
    SELECT
    CONCAT(e.locationID, '#', e.eventDate) AS visitKey,
    e.eventDate,
    e.\`month\`,
    e.\`year\`,
    e.decimalLatitude,
    e.decimalLongitude,
    e.geodeticDatum,
    e.locationID,
    e.locationName,
    e.samplingProtocol,
    c.bibliographicCitation,
    c.datasetName,
    v.varName,
    v.varValue,
    v.varUnit
    FROM (
      SELECT DISTINCT
      e.eventDate,
      e.locationID
      FROM species AS s
      INNER JOIN env AS e
      ON s.locationID = e.locationID
      AND s.eventDate = e.eventDate
      AND (
        s.scientificName IN ('species one')
        OR s.taxonRemarks IN ('species one')
      )
      INNER JOIN envvars AS v
      ON v.locationID = e.locationID
      AND v.eventDate = e.eventDate
      AND v.varName IN ('var one','var two')
      ORDER BY 2,1
      LIMIT 20 OFFSET 100
    ) AS coreData
    INNER JOIN env AS e
    ON coreData.locationID = e.locationID
    AND coreData.eventDate = e.eventDate
    INNER JOIN citations AS c
    ON e.samplingProtocol = c.samplingProtocol
    LEFT JOIN envvars AS v
    ON coreData.locationID = v.locationID
    AND coreData.eventDate = v.eventDate
    AND v.varName IN ('var one','var two');`
  describe('getRecordsSql', () => {
    it('should be able to handle no var filter', () => {
      let result = objectUnderTest._testonly.getRecordsSql("'species one'", null, 0, 20)
      expect(result).toBe(expectedRecordsSql1)
    })

    it('should be able to handle a supplied var filter', () => {
      let result = objectUnderTest._testonly.getRecordsSql("'species one'", "'var one','var two'", 100, 20)
      expect(result).toBe(expectedRecordsSql2)
    })

    it('should throw an error when we do not supply a species', () => {
      let undefinedSpeciesName
      expect(() => {
        objectUnderTest._testonly.getRecordsSql(undefinedSpeciesName, 0, 20)
      }).toThrow()
    })
  })

  let expectedCountSql1 = `
    SELECT count(DISTINCT e.locationID, e.eventDate) AS recordsHeld
    FROM species AS s
    INNER JOIN env AS e
    ON s.locationID = e.locationID
    AND s.eventDate = e.eventDate
    AND (
      s.scientificName IN ('species one','species two')
      OR s.taxonRemarks IN ('species one','species two')
    )
    ;`
  let expectedCountSql2 = `
    SELECT count(DISTINCT e.locationID, e.eventDate) AS recordsHeld
    FROM species AS s
    INNER JOIN env AS e
    ON s.locationID = e.locationID
    AND s.eventDate = e.eventDate
    AND (
      s.scientificName IN ('species one','species two')
      OR s.taxonRemarks IN ('species one','species two')
    )
      INNER JOIN envvars AS v
      ON v.locationID = e.locationID
      AND v.eventDate = e.eventDate
      AND v.varName IN ('var one','var two')
    ;`
  describe('getCountSql', () => {
    it('should be able to handle no variable filter', () => {
      let result = objectUnderTest._testonly.getCountSql("'species one','species two'", null)
      expect(result).toBe(expectedCountSql1)
    })

    it('should be able to handle variable filter', () => {
      let result = objectUnderTest._testonly.getCountSql("'species one','species two'", "'var one','var two'")
      expect(result).toBe(expectedCountSql2)
    })

    it('should throw an error when we do not supply a species', () => {
      let undefinedSpeciesName
      expect(() => {
        objectUnderTest._testonly.getCountSql(undefinedSpeciesName)
      }).toThrow()
    })
  })

  let expectedSpeciesNamesSql1 = `
    SELECT DISTINCT
    CONCAT(locationID, '#', eventDate) AS visitKey,
    scientificName,
    taxonRemarks
    FROM species
    WHERE (
      scientificName IN ('species one')
      OR taxonRemarks IN ('species one')
    )
    AND (locationID, eventDate) in (('location-1','2001-01-01'), ('location-2','2002-02-02'))
    ORDER BY 1;`
  describe('getSpeciesNamesSql', () => {
    it('should produce the expected SQL when a visit key clause and a species name clause are supplied', () => {
      let result = objectUnderTest._testonly.getSpeciesNamesSql("('location-1','2001-01-01'), ('location-2','2002-02-02')", "'species one'")
      expect(result).toBe(expectedSpeciesNamesSql1)
    })

    it('should throw an error when we do not supply any visit key clauses', () => {
      let undefinedVisitKeyClauses
      expect(() => {
        objectUnderTest._testonly.getSpeciesNamesSql(undefinedVisitKeyClauses, "'species one'")
      }).toThrow()
    })

    it('should throw an error when we do not supply any species name clauses', () => {
      let undefinedSpeciesNameClause
      expect(() => {
        objectUnderTest._testonly.getSpeciesNamesSql("('location-1#2001-01-01', 'location-2#2002-02-02')", undefinedSpeciesNameClause)
      }).toThrow()
    })
  })

  describe('getVisitKeyClauses', () => {
    it('should produce the expected SQL fragment', () => {
      let result = objectUnderTest._testonly.getVisitKeyClauses(['location-1#2001-01-01', 'location-2#2002-02-02'])
      expect(result).toBe("('location-1','2001-01-01'),\n    ('location-2','2002-02-02')")
    })
  })

  describe('extractParams', () => {
    let stubDb = new StubDB()
    it('should extract the params when they are all present', () => {
      let requestBody = {
        speciesNames: ['species one'],
        varNames: ['var one']
      }
      let queryStringParameters = {
        rows: 15,
        start: 0
      }
      let result = objectUnderTest.extractParams(requestBody, queryStringParameters, stubDb)
      expect(result.speciesNames).toBe("'species one'")
      expect(result.unescapedSpeciesNames).toEqual(['species one'])
      expect(result.varNames).toBe("'var one'")
      expect(result.unescapedVarNames).toEqual(['var one'])
      expect(result.rows).toBe(15)
      expect(result.start).toBe(0)
    })

    it('should default paging information when it is not supplied', () => {
      let requestBody = {
        speciesNames: ['species one']
        // don't supply 'varNames'
      }
      let queryStringParameters = null
      let result = objectUnderTest.extractParams(requestBody, queryStringParameters, stubDb)
      expect(result.speciesNames).toBe("'species one'")
      expect(result.unescapedSpeciesNames).toEqual(['species one'])
      expect(result.varNames).toBeNull()
      expect(result.rows).toBe(20)
      expect(result.start).toBe(0)
    })
  })
})

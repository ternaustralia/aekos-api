'use strict'
let objectUnderTest = require('../traitData-json')
let uberRouter = require('../uberRouter')
let StubDB = require('./StubDB')
let ConsoleSilencer = require('./ConsoleSilencer')
let consoleSilencer = new ConsoleSilencer()

describe('/v2/traitData-json', () => {
  describe('doHandle', () => {
    let expectedSql = `
    SELECT count(*) AS recordsHeld
    FROM species AS s
      WHERE id IN (
        SELECT parentId
        FROM traits AS t
        --
      )
      AND (
        scientificName IN ('species one')
        OR taxonRemarks IN ('species one')
      );`

    let result = null
    let stubDb = new StubDB()
    beforeEach(done => {
      stubDb.setExecSelectPromiseResponses([
        [{
          id: 'species1',
          scientificName: 'species one',
          locationName: 'loc1',
          datasetName: 'dataset1',
          traitName: 'trait1',
          traitValue: 'value1',
          traitUnit: 'unit1'
        }, {
          id: 'species1',
          scientificName: 'species one',
          locationName: 'loc1',
          datasetName: 'dataset1',
          traitName: 'trait2',
          traitValue: 'value2',
          traitUnit: 'unit2'
        }],
        [{ recordsHeld: 31 }]
      ])
      spyOn(stubDb, 'execSelectPromise').and.callThrough()
      let event = {
        body: JSON.stringify({
          speciesNames: ['species one']
          // don't supply 'traitNames'
        }),
        queryStringParameters: {
          rows: '15',
          start: '0'
        },
        headers: {
          Host: 'api.aekos.org.au',
          'X-Forwarded-Proto': 'https'
        },
        requestContext: { path: '/v2/traitData.json' },
        path: '/v2/traitData.json'
      }
      let callback = (_, theResult) => {
        result = theResult
        done()
      }
      uberRouter._testonly.doHandle(event, callback, stubDb, () => { return 42 })
    })

    it('should return a 200 response when we request all traits for a species', () => {
      expect(result.statusCode).toBe(200)
      expect(result.headers).toEqual({
        'Access-Control-Allow-Origin': '*',
        'Access-Control-Allow-Credentials': true,
        'Access-Control-Expose-Headers': 'link',
        'Content-Type': "'application/json'",
        link: '<https://api.aekos.org.au/v2/traitData.json?rows=15&start=15>; rel="next", ' +
        '<https://api.aekos.org.au/v2/traitData.json?rows=15&start=30>; rel="last"'
      })
      expect(JSON.parse(result.body)).toEqual({
        responseHeader: {
          elapsedTime: 42,
          numFound: 31,
          pageNumber: 1,
          params: {
            rows: 15,
            start: 0,
            speciesNames: ['species one'],
            traitNames: null
          },
          totalPages: 3
        },
        response: [
          {
            scientificName: 'species one',
            locationName: 'loc1',
            datasetName: 'dataset1',
            traits: [
              { traitName: 'trait1', traitValue: 'value1', traitUnit: 'unit1' },
              { traitName: 'trait2', traitValue: 'value2', traitUnit: 'unit2' }
            ]
          }
        ]
      })
      expect(stubDb.execSelectPromise).toHaveBeenCalledWith(expectedSql)
    })
  })

  describe('doHandle', () => {
    let expectedSql = `
    SELECT count(*) AS recordsHeld
    FROM species AS s
      WHERE id IN (
        SELECT parentId
        FROM traits AS t
        WHERE t.traitName IN ('trait one')
      )
      AND (
        scientificName IN ('species eleven')
        OR taxonRemarks IN ('species eleven')
      );`

    let result = null
    let stubDb = new StubDB()
    beforeEach(done => {
      stubDb.setExecSelectPromiseResponses([
        [{ id: 'species1', scientificName: 'species one' }],
        [{ recordsHeld: 31 }]
      ])
      spyOn(stubDb, 'execSelectPromise').and.callThrough()
      let event = {
        body: JSON.stringify({
          speciesNames: ['species eleven'],
          traitNames: ['trait one']
        }),
        queryStringParameters: {
          rows: '15',
          start: '0'
        },
        headers: {
          Host: 'api.aekos.org.au',
          'X-Forwarded-Proto': 'https'
        },
        requestContext: { path: '/v2/traitData.json' },
        path: '/v2/traitData.json'
      }
      let callback = (_, theResult) => {
        result = theResult
        done()
      }
      uberRouter._testonly.doHandle(event, callback, stubDb, () => { return 42 })
    })

    it('should echo the supplied trait name', () => {
      expect(result.statusCode).toBe(200)
      let responseHeader = JSON.parse(result.body).responseHeader
      expect(responseHeader.params.traitNames).toEqual(['trait one'])
      expect(stubDb.execSelectPromise).toHaveBeenCalledWith(expectedSql)
    })
  })

  describe('doHandle', () => {
    let result = null
    beforeEach(done => {
      let stubDb = new StubDB()
      stubDb.setExecSelectPromiseResponses([
        [{ id: 'species1', scientificName: 'species one' }],
        [{ recordsHeld: 31 }]
      ])
      let event = {
        body: JSON.stringify({
          // don't supply 'speciesNames'
          traitNames: ['trait one']
        }),
        queryStringParameters: {
          start: '0'
        },
        requestContext: { path: '/v2/traitData.json' },
        path: '/v2/traitData.json'
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
        message: "The 'speciesNames' field was not supplied",
        statusCode: 400
      })
    })
  })

  describe('doHandle', () => {
    let result = null
    let stubDb = new StubDB()
    beforeEach(done => {
      stubDb.setExecSelectPromiseResponses([
        [ /* no records, will cause Error */ ],
        [{ recordsHeld: 31 }]
      ])
      spyOn(stubDb, 'execSelectPromise').and.callThrough()
      let event = {
        body: JSON.stringify({speciesNames: ['species eleven']}),
        queryStringParameters: null,
        headers: {
          Host: 'api.aekos.org.au',
          'X-Forwarded-Proto': 'https'
        },
        requestContext: { path: '/v2/traitData.json' },
        path: '/v2/traitData.json'
      }
      let callback = (_, theResult) => {
        consoleSilencer.resetConsoleError()
        result = theResult
        done()
      }
      consoleSilencer.silenceConsoleError()
      uberRouter._testonly.doHandle(event, callback, stubDb, () => { return 42 })
    })

    it('should catch an error thrown during query result processing and respond with a 500', () => {
      expect(result.statusCode).toBe(500)
    })
  })

  describe('extractParams', () => {
    it('should extract the params when they are present', () => {
      let queryStringParameters = {
        rows: '15',
        start: '0'
      }
      let requestBody = {
        speciesNames: ['species five'],
        traitNames: ['trait one']
      }
      let result = objectUnderTest.extractParams(requestBody, queryStringParameters, new StubDB())
      expect(result.speciesNames).toBe("'species five'")
      expect(result.unescapedSpeciesNames).toEqual(['species five'])
      expect(result.traitNames).toBe("'trait one'")
      expect(result.unescapedTraitNames).toEqual(['trait one'])
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
      expect(result.traitNames).toBeNull()
      expect(result.unescapedTraitNames).toBeNull()
      expect(result.rows).toBe(20)
      expect(result.start).toBe(0)
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

    it('should be valid with species names and trait names', () => {
      let queryStringObj = null
      let requestBody = {
        speciesNames: ['species one', 'species two'],
        traitNames: ['trait one', 'trait two']
      }
      let result = objectUnderTest.validator(queryStringObj, requestBody)
      expect(result.isValid).toBe(true)
    })

    it('should be invalid without species names', () => {
      let queryStringObj = null
      let requestBody = {
        // no 'speciesNames'
        traitNames: ['trait one', 'trait two']
      }
      let result = objectUnderTest.validator(queryStringObj, requestBody)
      expect(result.isValid).toBe(false)
    })

    it('should be invalid when trait names is not an array', () => {
      let queryStringObj = null
      let requestBody = {
        speciesNames: ['species one', 'species two'],
        traitNames: 'trait one' // not an array
      }
      let result = objectUnderTest.validator(queryStringObj, requestBody)
      expect(result.isValid).toBe(false)
    })
  })

  let expectedRecordsSql1 = `
    SELECT
    s.id,
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
    c.datasetName,
    t.traitName,
    t.traitValue,
    t.traitUnit
    FROM (
      SELECT id
      FROM species
      WHERE id IN (
        SELECT parentId
        FROM traits AS t
        --
      )
      AND (
        scientificName IN ('species one', 'species two')
        OR taxonRemarks IN ('species one', 'species two')
      )
      ORDER BY 1
      LIMIT 22 OFFSET 0
    ) AS lateRowLookup
    INNER JOIN species AS s
    ON lateRowLookup.id = s.id
    INNER JOIN traits AS t
    ON t.parentId = s.id
    --
    LEFT JOIN env AS e
    ON s.locationID = e.locationID
    AND s.eventDate = e.eventDate
    LEFT JOIN citations AS c
    ON e.samplingProtocol = c.samplingProtocol;`
  let expectedRecordsSql2 = `
    SELECT
    s.id,
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
    c.datasetName,
    t.traitName,
    t.traitValue,
    t.traitUnit
    FROM (
      SELECT id
      FROM species
      WHERE id IN (
        SELECT parentId
        FROM traits AS t
        WHERE t.traitName IN ('trait one', 'trait two')
      )
      AND (
        scientificName IN ('species one', 'species two')
        OR taxonRemarks IN ('species one', 'species two')
      )
      ORDER BY 1
      LIMIT 22 OFFSET 0
    ) AS lateRowLookup
    INNER JOIN species AS s
    ON lateRowLookup.id = s.id
    INNER JOIN traits AS t
    ON t.parentId = s.id
    AND t.traitName IN ('trait one', 'trait two')
    LEFT JOIN env AS e
    ON s.locationID = e.locationID
    AND s.eventDate = e.eventDate
    LEFT JOIN citations AS c
    ON e.samplingProtocol = c.samplingProtocol;`
  describe('getRecordsSql', () => {
    it('should be able to handle species with no trait filter', () => {
      let speciesNames = "'species one', 'species two'"
      let traitNames = null
      let result = objectUnderTest._testonly.getRecordsSql(speciesNames, 0, 22, traitNames)
      expect(result).toBe(expectedRecordsSql1)
    })

    it('should be able to handle when a trait filter is applied', () => {
      let speciesNames = "'species one', 'species two'"
      let traitNames = "'trait one', 'trait two'"
      let result = objectUnderTest._testonly.getRecordsSql(speciesNames, 0, 22, traitNames)
      expect(result).toBe(expectedRecordsSql2)
    })
  })

  let expectedCountSql1 = `
    SELECT count(*) AS recordsHeld
    FROM species AS s
      WHERE id IN (
        SELECT parentId
        FROM traits AS t
        --
      )
      AND (
        scientificName IN ('species one', 'species two')
        OR taxonRemarks IN ('species one', 'species two')
      );`
  let expectedCountSql2 = `
    SELECT count(*) AS recordsHeld
    FROM species AS s
      WHERE id IN (
        SELECT parentId
        FROM traits AS t
        WHERE t.traitName IN ('trait one', 'trait two')
      )
      AND (
        scientificName IN ('species one', 'species two')
        OR taxonRemarks IN ('species one', 'species two')
      );`
  describe('getCountSql', () => {
    it('should be able to handle species with no trait filter', () => {
      let speciesNames = "'species one', 'species two'"
      let traitNames = null
      let result = objectUnderTest._testonly.getCountSql(speciesNames, traitNames)
      expect(result).toBe(expectedCountSql1)
    })

    it('should be able to handle when a trait filter is applied', () => {
      let speciesNames = "'species one', 'species two'"
      let traitNames = "'trait one', 'trait two'"
      let result = objectUnderTest._testonly.getCountSql(speciesNames, traitNames)
      expect(result).toBe(expectedCountSql2)
    })
  })

  describe('.rollupRecords()', () => {
    it('should throw the expected error when we supply a record without a key field', () => {
      let responseObj = {
        responseHeader: {},
        response: [{ someField: 'this record has no id field' }]
      }
      try {
        objectUnderTest._testonly.rollupRecords(responseObj)
        fail()
      } catch (error) {
        expect(error.message).toBe("Data problem: record did not have the 'id' field")
      }
    })

    it('should be able to handle no records', () => {
      let responseObj = {
        responseHeader: {},
        response: []
      }
      let result = objectUnderTest._testonly.rollupRecords(responseObj)
      expect(result).toEqual({
        responseHeader: {},
        response: []
      })
    })

    it('should be able to handle one record', () => {
      let responseObj = {
        responseHeader: {},
        response: [{
          id: 123,
          otherField1: 'foo',
          otherField2: 'bar',
          traitName: 'height',
          traitValue: '3',
          traitUnit: 'metres'
        }]
      }
      let result = objectUnderTest._testonly.rollupRecords(responseObj)
      expect(result).toEqual({
        responseHeader: {},
        response: [{
          otherField1: 'foo',
          otherField2: 'bar',
          traits: [
            {
              traitName: 'height',
              traitValue: '3',
              traitUnit: 'metres'
            }
          ]
        }]
      })
    })

    it('should be able to rollup two rollable records', () => {
      let responseObj = {
        responseHeader: {},
        response: [{
          id: 123,
          otherField1: 'foo',
          otherField2: 'bar',
          traitName: 'height',
          traitValue: '3',
          traitUnit: 'metres'
        }, {
          id: 123,
          otherField1: 'foo',
          otherField2: 'bar',
          traitName: 'canopyCover',
          traitValue: '21',
          traitUnit: 'percent'
        }]
      }
      let result = objectUnderTest._testonly.rollupRecords(responseObj)
      expect(result).toEqual({
        responseHeader: {},
        response: [{
          otherField1: 'foo',
          otherField2: 'bar',
          traits: [
            {
              traitName: 'height',
              traitValue: '3',
              traitUnit: 'metres'
            }, {
              traitName: 'canopyCover',
              traitValue: '21',
              traitUnit: 'percent'
            }
          ]
        }]
      })
    })
  })
})

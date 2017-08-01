'use strict'
let objectUnderTest = require('../traitData-json')
let StubDB = require('./StubDB')

describe('/v2/traitData-json', () => {
  describe('doHandle', () => {
    let result = null
    beforeEach(done => {
      let stubDb = new StubDB()
      stubDb.setExecSelectPromiseResponses([
        [ {
          id: 'species1',
          scientificName: 'species one',
          locationName: 'loc1',
          datasetName: 'dataset1'
        } ],
        [ {recordsHeld: 31} ],
        [
          { traitName: 'trait1', traitValue: 'value1', traitUnit: 'unit1' },
          { traitName: 'trait2', traitValue: 'value2', traitUnit: 'unit2' }
        ]
      ])
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
        requestContext: {
          path: '/v2/traitData.json'
        }
      }
      let callback = (_, theResult) => {
        result = theResult
        done()
      }
      objectUnderTest._testonly.doHandle(event, callback, stubDb, () => { return 42 })
    })

    it('should return a 200 response when we request all traits for a species', () => {
      expect(result.statusCode).toBe(200)
      expect(result.headers).toEqual({
        'Access-Control-Allow-Origin': '*',
        'Access-Control-Allow-Credentials': true,
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
    })
  })

  describe('doHandle', () => {
    let result = null
    beforeEach(done => {
      let stubDb = new StubDB()
      stubDb.setExecSelectPromiseResponses([
        [ {id: 'species1', scientificName: 'species one'} ],
        [ {recordsHeld: 31} ],
        [ ]
      ])
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
        requestContext: {
          path: '/v2/traitData.json'
        }
      }
      let callback = (_, theResult) => {
        result = theResult
        done()
      }
      objectUnderTest._testonly.doHandle(event, callback, stubDb, () => { return 42 })
    })

    it('should echo the supplied trait name', () => {
      expect(result.statusCode).toBe(200)
      let responseHeader = JSON.parse(result.body).responseHeader
      expect(responseHeader.params.traitNames).toEqual(['trait one'])
    })
  })

  describe('doHandle', () => {
    let result = null
    beforeEach(done => {
      let stubDb = new StubDB()
      stubDb.setExecSelectPromiseResponses([
        [ {id: 'species1', scientificName: 'species one'} ],
        [ {recordsHeld: 31} ],
        [ ]
      ])
      let event = {
        body: JSON.stringify({
          // don't supply 'speciesNames'
          traitNames: ['trait one']
        }),
        queryStringParameters: {
          start: '0'
        }
      }
      let callback = (_, theResult) => {
        result = theResult
        done()
      }
      objectUnderTest._testonly.doHandle(event, callback, stubDb, () => { return 42 })
    })

    it('should return a 400 response when we do not supply speciesNames', () => {
      expect(result.statusCode).toBe(400)
      expect(result.headers).toEqual({
        'Access-Control-Allow-Origin': '*',
        'Access-Control-Allow-Credentials': true,
        'Content-Type': "'application/json'"
      })
      expect(JSON.parse(result.body)).toEqual({
        message: "The 'speciesNames' field was not supplied",
        statusCode: 400
      })
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

  let expectedTraitSql1 = `
    SELECT
    traitName,
    traitValue,
    traitUnit
    FROM traits
    WHERE parentId = 'some-parent-id123'
    AND traitName in ('trait one');`
  let expectedTraitSql2 = `
    SELECT
    traitName,
    traitValue,
    traitUnit
    FROM traits
    WHERE parentId = 'some-parent-id123'
    ;`
  describe('getTraitSql', () => {
    it('should be able to handle a single trait', () => {
      let result = objectUnderTest._testonly.getTraitSql('some-parent-id123', "'trait one'")
      expect(result).toBe(expectedTraitSql1)
    })

    it('should throw an error when we do not supply a parent ID', () => {
      let undefinedParentId
      expect(() => {
        objectUnderTest._testonly.getTraitSql(undefinedParentId, "'trait one'")
      }).toThrow()
    })

    it('should be able to handle when no trait filter is applied', () => {
      let noTraits = null
      let result = objectUnderTest._testonly.getTraitSql('some-parent-id123', noTraits)
      expect(result).toBe(expectedTraitSql2)
    })
  })
})

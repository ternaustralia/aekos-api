'use strict'
let objectUnderTest = require('../traitData-json')
let StubDB = require('./StubDB')

describe('/v1/traitData-json', () => {
  describe('doHandle', () => {
    let result = null
    beforeEach(done => {
      let stubDb = new StubDB()
      stubDb.setExecSelectPromiseResponses([
        [ {id: 'species1', scientificName: 'species one'} ],
        [ {recordsHeld: 31} ],
        [
          { traitName: 'trait1', traitValue: 'value1', traitUnit: 'unit1' },
          { traitName: 'trait2', traitValue: 'value2', traitUnit: 'unit2' }
        ]
      ])
      let event = {
        queryStringParameters: {
          speciesName: 'species one',
          // don't supply 'traitName'
          rows: '15',
          start: '0'
        },
        headers: {
          Host: 'api.aekos.org.au',
          'X-Forwarded-Proto': 'https'
        },
        requestContext: {
          path: '/v1/traitData.json'
        }
      }
      let callback = (_, theResult) => {
        result = theResult
        done()
      }
      objectUnderTest._testonly.doHandle(event, callback, stubDb, () => { return 42 })
    })

    it('should return a 200 response when we return all traits for a species', () => {
      expect(result.statusCode).toBe(200)
      expect(result.headers).toEqual({
        'Access-Control-Allow-Origin': '*',
        'Access-Control-Allow-Credentials': true,
        'Content-Type': "'application/json'",
        link: '<https://api.aekos.org.au/v1/traitData.json?speciesName=species%20one&rows=15&start=15>; rel="next", ' +
              '<https://api.aekos.org.au/v1/traitData.json?speciesName=species%20one&rows=15&start=30>; rel="last"'
      })
      expect(JSON.parse(result.body)).toEqual({
        responseHeader: {
          elapsedTime: 42,
          numFound: 31,
          pageNumber: 1,
          params: {
            rows: 15,
            start: 0,
            speciesName: 'species one',
            traitName: null
          },
          totalPages: 3
        },
        response: [
          {
            scientificName: 'species one',
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
        queryStringParameters: {
          speciesName: 'species one',
          traitName: 'trait one',
          rows: '15',
          start: '0'
        },
        headers: {
          Host: 'api.aekos.org.au',
          'X-Forwarded-Proto': 'https'
        },
        requestContext: {
          path: '/v1/traitData.json'
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
      expect(responseHeader.params.traitName).toBe('trait one')
    })
  })

  describe('extractParams', () => {
    it('should extract the params when they are present', () => {
      let event = {
        queryStringParameters: {
          speciesName: 'species one',
          traitName: 'trait one',
          rows: '15',
          start: '0'
        }
      }
      let result = objectUnderTest.extractParams(event, new StubDB())
      expect(result.speciesName).toBe("'species one'")
      expect(result.unescapedSpeciesName).toBe('species one')
      expect(result.traitName).toBe("'trait one'")
      expect(result.unescapedTraitName).toBe('trait one')
      expect(result.rows).toBe(15)
      expect(result.start).toBe(0)
    })

    it('should default the paging info', () => {
      let event = {
        queryStringParameters: {
          speciesName: 'species two'
        }
      }
      let result = objectUnderTest.extractParams(event, new StubDB())
      expect(result.speciesName).toBe("'species two'")
      expect(result.unescapedSpeciesName).toBe('species two')
      expect(result.traitName).toBeNull()
      expect(result.unescapedTraitName).toBeNull()
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

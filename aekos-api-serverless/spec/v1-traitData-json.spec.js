'use strict'
let objectUnderTest = require('../traitData-json')
let StubDB = require('./StubDB')

describe('/v1/traitData-json', () => {
  describe('doHandle', () => {
    let result = null
    beforeEach(done => {
      let stubDb = new StubDB()
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
        [ {recordsHeld: 31} ]
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
        'Access-Control-Expose-Headers': 'link',
        'Content-Type': "'application/json'",
        link: '<https://api.aekos.org.au/v1/traitData.json?rows=15&start=15>; rel="next", ' +
              '<https://api.aekos.org.au/v1/traitData.json?rows=15&start=30>; rel="last"'
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
            // no v2 attributes present
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
          speciesNames: ['species one'],
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
        body: null, // don't supply 'speciesNames'
        queryStringParameters: {
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
})

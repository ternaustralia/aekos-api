'use strict'
let objectUnderTest = require('../allSpeciesData-json')
let StubDB = require('./StubDB')

describe('/v1/allSpeciesData-json', () => {
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
          path: '/v1/allSpeciesData.json'
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
        link: '<https://api.aekos.org.au/v1/allSpeciesData.json?rows=15&start=15>; rel="next", ' +
              '<https://api.aekos.org.au/v1/allSpeciesData.json?rows=15&start=30>; rel="last"'
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
          { recordNum: 1 }, // no v2 fields should be in the response
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
        queryStringParameters: null, // no params
        headers: {
          Host: 'api.aekos.org.au',
          'X-Forwarded-Proto': 'https'
        },
        requestContext: {
          path: '/v1/allSpeciesData.json'
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
        link: '<https://api.aekos.org.au/v1/allSpeciesData.json?start=20>; rel="next", ' +
              '<https://api.aekos.org.au/v1/allSpeciesData.json?start=20>; rel="last"'
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
          { recordNum: 1 }, // no v2 fields should be in the response
          { recordNum: 2 }
        ]
      })
    })
  })
})

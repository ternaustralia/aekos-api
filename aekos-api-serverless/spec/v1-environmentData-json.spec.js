'use strict'
let StubDB = require('./StubDB')
let objectUnderTest = require('../environmentData-json')

describe('/v1/environmentData-json', () => {
  describe('.doHandle()', () => {
    let result = null
    beforeEach(done => {
      let stubDb = new StubDB()
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
        requestContext: {
          path: '/v1/environmentData.json'
        }
      }
      let callback = (_, theResult) => {
        result = theResult
        done()
      }
      objectUnderTest._testonly.doHandle(event, callback, stubDb, () => { return 42 })
    })

    it('should return a 200 response when the minimum required parameters are supplied', () => {
      expect(result.statusCode).toBe(200)
      expect(result.headers).toEqual({
        'Access-Control-Allow-Origin': '*',
        'Access-Control-Allow-Credentials': true,
        'Access-Control-Expose-Headers': 'link',
        'Content-Type': "'application/json'",
        link: '<https://api.aekos.org.au/v1/environmentData.json?start=20>; rel="next", ' +
              '<https://api.aekos.org.au/v1/environmentData.json?start=20>; rel="last"'
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
        response: [ // should be no 'locationName' or 'datasetName' fields
          {
            variables: [
              { varName: 'windSpeed', varValue: '6', varUnit: 'km/h' },
              { varName: 'ph', varValue: '7', varUnit: null }
            ],
            scientificNames: [],
            taxonRemarks: ['taxon name']
          }, {
            variables: [{ varName: 'windSpeed', varValue: '7', varUnit: 'km/h' }],
            scientificNames: ['sci name'],
            taxonRemarks: []
          }
        ]
      })
    })
  })
})

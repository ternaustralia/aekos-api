'use strict'
let StubDB = require('./StubDB')
let objectUnderTest = require('../environmentData-json')

describe('/v1/environmentData-json', () => {
  describe('.doHandle()', () => {
    let result = null
    beforeEach(done => {
      let stubDb = new StubDB()
      const recordsResult = [{
        recordNum: 1,
        locationName: 'location1',
        datasetName: 'dataset1',
        visitKey: 'location1#2017-07-07'
      }, {
        recordNum: 2,
        locationName: 'location2',
        datasetName: 'dataset2',
        visitKey: 'location2#2008-02-02'
      }]
      const countResult = [{ recordsHeld: 31 }]
      const varsResult = [{
        visitKey: 'location1#2017-07-07',
        varName: 'height',
        varValue: '123',
        varUnit: 'cm'
      }, {
        visitKey: 'location2#2008-02-02',
        varName: 'height',
        varValue: '468',
        varUnit: 'cm'
      }]
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
        varsResult,
        speciesNamesResult
      ])
      let event = {
        queryStringParameters: {
          speciesName: 'species one'
        },
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
        'Content-Type': "'application/json'",
        link: '<https://api.aekos.org.au/v1/environmentData.json?speciesName=species%20one&start=20>; rel="next", ' +
              '<https://api.aekos.org.au/v1/environmentData.json?speciesName=species%20one&start=20>; rel="last"'
      })
      expect(JSON.parse(result.body)).toEqual({
        responseHeader: {
          elapsedTime: 42,
          numFound: 31,
          pageNumber: 1,
          params: {
            rows: 20,
            start: 0,
            speciesName: 'species one',
            varName: null
          },
          totalPages: 2
        },
        response: [ // should be no 'locationName' or 'datasetName' fields
          {
            recordNum: 1,
            variables: [{ varName: 'height', varValue: '123', varUnit: 'cm' }],
            scientificNames: [],
            taxonRemarks: ['taxon name']
          }, {
            recordNum: 2,
            variables: [{ varName: 'height', varValue: '468', varUnit: 'cm' }],
            scientificNames: ['sci name'],
            taxonRemarks: []
          }
        ]
      })
    })
  })
})

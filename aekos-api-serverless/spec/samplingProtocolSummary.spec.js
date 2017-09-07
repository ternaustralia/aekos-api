'use strict'
let StubDB = require('./StubDB')
let uberRouter = require('../uberRouter')

describe('/repository-metadata', () => {
  describe('.doHandle()', () => {
    let result = null
    beforeEach(done => {
      let event = {
        requestContext: { path: '/sampling-protocol-summary' },
        path: '/sampling-protocol-summary'
      }
      let callback = (_, theResult) => {
        result = theResult
        done()
      }
      let stubDb = new StubDB()
      stubDb.setExecSelectPromiseResponses([
        [
          {
            id: 'aekos.org.au/collection/adelaide.edu.au',
            envRecordsHeld: 11
          },
          {
            id: 'aekos.org.au/collection/csiro',
            envRecordsHeld: 22
          }
        ], [
          {
            name: 'AusPlots Rangelands',
            id: 'aekos.org.au/collection/adelaide.edu.au/ausplotsrangelands',
            envRecordsHeld: 1
          },
          {
            name: null,
            id: 'aekos.org.au/collection/adelaide.edu.au/Koonamore',
            envRecordsHeld: 33
          }
        ], [
          {
            id: 'aekos.org.au/collection/adelaide.edu.au/Koonamore/CassiaCornerSennaQuadrat',
            name: 'Senna Quadrat (Cassia Corner), Koonamore Vegetation Monitoring Project'
          }
        ]
      ])
      uberRouter._testonly.doHandle(event, callback, stubDb, null)
    })

    it('should return the sampling protocol names', () => {
      expect(result.statusCode).toBe(200)
      expect(result.headers).toEqual({
        'Access-Control-Allow-Origin': '*',
        'Access-Control-Allow-Credentials': true,
        'Access-Control-Expose-Headers': 'link',
        'Content-Type': "'application/json'"
      })
      expect(JSON.parse(result.body)).toEqual({
        datasetGroups: [{
          name: 'Adelaide University',
          id: 'aekos.org.au/collection/adelaide.edu.au',
          envRecordsHeld: 11
        }, {
          name: null,
          id: 'aekos.org.au/collection/csiro',
          envRecordsHeld: 22
        }],
        samplingProtocolGroups: [{
          name: 'AusPlots Rangelands',
          id: 'aekos.org.au/collection/adelaide.edu.au/ausplotsrangelands',
          envRecordsHeld: 1
        }, {
          name: 'Koonamore Survey',
          id: 'aekos.org.au/collection/adelaide.edu.au/Koonamore',
          envRecordsHeld: 33
        }],
        surveys: [{
          name: 'Senna Quadrat (Cassia Corner), Koonamore Vegetation Monitoring Project',
          id: 'aekos.org.au/collection/adelaide.edu.au/Koonamore/CassiaCornerSennaQuadrat'
        }]
      })
    })
  })
})

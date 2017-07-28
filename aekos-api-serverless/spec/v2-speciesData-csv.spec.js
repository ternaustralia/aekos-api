'use strict'
var objectUnderTest = require('../speciesData-csv')
let StubDB = require('./StubDB')

describe('/v2/speciesData.csv', () => {
  describe('doHandle', () => {
    let result = null
    beforeEach(done => {
      let stubDb = new StubDB()
      stubDb.setExecSelectPromiseResponses([
        [
          {
            'id': 'some-id-123',
            'scientificName': 'Acacia binata Maslin',
            'taxonRemarks': null,
            'individualCount': 1,
            'eventDate': '2007-10-03',
            'month': 10,
            'year': 2007,
            'decimalLatitude': -33.59758852952833,
            'decimalLongitude': 120.15956081537496,
            'geodeticDatum': 'GDA94',
            'locationID': 'aekos.org.au/collection/wa.gov.au/ravensthorpe/R181',
            'locationName': 'R181',
            'samplingProtocol': 'aekos.org.au/collection/wa.gov.au/ravensthorpe',
            'bibliographicCitation': 'Department of Par...',
            'datasetName': 'Biological Survey of the Ravensthorpe Range (Phase 1)'
          }
        ],
        [ {recordsHeld: 31} ]
      ])
      let event = {
        body: JSON.stringify({speciesNames: ['species eleven']}),
        queryStringParameters: {
          rows: '15',
          start: '0'
        },
        headers: {
          Host: 'api.aekos.org.au',
          'X-Forwarded-Proto': 'https'
        },
        requestContext: {
          path: '/v2/speciesData.csv'
        }
      }
      let callback = (_, theResult) => {
        result = theResult
        done()
      }
      objectUnderTest._testonly.doHandle(event, callback, stubDb, () => { return 42 })
    })

    it('should return a 200 response when we do a simple request', () => {
      expect(result.statusCode).toBe(200)
      expect(result.headers).toEqual({
        'Access-Control-Allow-Origin': '*',
        'Access-Control-Allow-Credentials': true,
        'Content-Type': "'text/csv'",
        'link': '<https://api.aekos.org.au/v2/speciesData.csv?rows=15&start=15>; rel="next", ' +
                '<https://api.aekos.org.au/v2/speciesData.csv?rows=15&start=30>; rel="last"'
      })
      expect(result.body.split('\n')).toEqual([
        `"decimalLatitude","decimalLongitude","geodeticDatum","locationID","locationName","datasetName","scientificName","taxonRemarks","individualCount","eventDate","year","month","bibliographicCitation","samplingProtocol"`,
        `-33.59758852952833,120.15956081537496,"GDA94","aekos.org.au/collection/wa.gov.au/ravensthorpe/R181","R181","Biological Survey of the Ravensthorpe Range (Phase 1)","Acacia binata Maslin",,1,"2007-10-03",2007,10,"Department of Par...","aekos.org.au/collection/wa.gov.au/ravensthorpe"`
      ])
    })
  })

  describe('doHandle', () => {
    let result = null
    beforeEach(done => {
      let stubDb = new StubDB()
      stubDb.setExecSelectPromiseResponses([
        [
          {
            'id': 'some-id-123',
            'scientificName': 'Acacia binata Maslin',
            'taxonRemarks': null,
            'individualCount': 1,
            'eventDate': '2007-10-03',
            'month': 10,
            'year': 2007,
            'decimalLatitude': -33.59758852952833,
            'decimalLongitude': 120.15956081537496,
            'geodeticDatum': 'GDA94',
            'locationID': 'aekos.org.au/collection/wa.gov.au/ravensthorpe/R181',
            'locationName': 'R181',
            'samplingProtocol': 'aekos.org.au/collection/wa.gov.au/ravensthorpe',
            'bibliographicCitation': 'Department of Par...',
            'datasetName': 'Biological Survey of the Ravensthorpe Range (Phase 1)'
          }
        ],
        [ {recordsHeld: 3} ]
      ])
      let event = {
        body: JSON.stringify({speciesNames: ['species eleven']}),
        queryStringParameters: {
          rows: '15',
          start: '0',
          download: 'true' // trigger response as download
        },
        headers: {
          Host: 'api.aekos.org.au',
          'X-Forwarded-Proto': 'https'
        },
        requestContext: {
          path: '/v2/allSpeciesData.csv'
        }
      }
      let callback = (_, theResult) => {
        result = theResult
        done()
      }
      objectUnderTest._testonly.doHandle(event, callback, stubDb, () => { return 42 })
    })

    it('should respond as a download when the download param is supplied', () => {
      expect(result.statusCode).toBe(200)
      expect(result.headers).toEqual({
        'Access-Control-Allow-Origin': '*',
        'Access-Control-Allow-Credentials': true,
        'Content-Type': "'text/csv'",
        'Content-Disposition': 'attachment;filename=aekosSpeciesData.csv',
        'link': ''
      })
      expect(result.body.split('\n')).toEqual([
        `"decimalLatitude","decimalLongitude","geodeticDatum","locationID","locationName","datasetName","scientificName","taxonRemarks","individualCount","eventDate","year","month","bibliographicCitation","samplingProtocol"`,
        `-33.59758852952833,120.15956081537496,"GDA94","aekos.org.au/collection/wa.gov.au/ravensthorpe/R181","R181","Biological Survey of the Ravensthorpe Range (Phase 1)","Acacia binata Maslin",,1,"2007-10-03",2007,10,"Department of Par...","aekos.org.au/collection/wa.gov.au/ravensthorpe"`
      ])
    })
  })
})

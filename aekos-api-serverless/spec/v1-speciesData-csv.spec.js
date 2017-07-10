'use strict'
var objectUnderTest = require('../v1-speciesData-csv')
let StubDB = require('./StubDB')

describe('v1-speciesData-csv', () => {
  describe('doHandle', () => {
    it('should return a 200 response when we do a simple request', (done) => {
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
            'samplingProtocol': 'aekos.org.au/collection/wa.gov.au/ravensthorpe',
            'bibliographicCitation': 'Department of Par...'
          }
        ],
        [ {recordsHeld: 31} ]
      ])
      let event = {
        queryStringParameters: {
          speciesName: 'species one',
          rows: '15',
          start: '0'
        }
      }
      let callback = (error, result) => {
        if (error) {
          fail('Responded with error: ' + JSON.stringify(error))
        }
        expect(result.statusCode).toBe(200)
        expect(result.headers).toEqual({
          'Access-Control-Allow-Origin': '*',
          'Access-Control-Allow-Credentials': true,
          'Content-Type': "'text/csv'"
        })
        expect(result.body.split('\n')).toEqual([
          `"decimalLatitude","decimalLongitude","geodeticDatum","locationID","scientificName","taxonRemarks","individualCount","eventDate","year","month","bibliographicCitation","samplingProtocol"`,
          `-33.59758852952833,120.15956081537496,"GDA94","aekos.org.au/collection/wa.gov.au/ravensthorpe/R181","Acacia binata Maslin",,1,"2007-10-03",2007,10,"Department of Par...","aekos.org.au/collection/wa.gov.au/ravensthorpe"`
        ])
        done()
      }
      objectUnderTest._testonly.doHandle(event, callback, stubDb, () => { return 42 })
    })
  })
})
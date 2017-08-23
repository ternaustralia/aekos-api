'use strict'
let objectUnderTest = require('../allSpeciesData-csv')
let uberRouter = require('../uberRouter')
let StubDB = require('./StubDB')

describe('/v1/allSpeciesData.csv', () => {
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
            'samplingProtocol': 'aekos.org.au/collection/wa.gov.au/ravensthorpe',
            'bibliographicCitation': 'Department of Par...'
          }
        ],
        [ {recordsHeld: 31} ]
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
        requestContext: { path: '/v1/allSpeciesData.csv' },
        path: '/v1/allSpeciesData.csv'
      }
      let callback = (_, theResult) => {
        result = theResult
        done()
      }
      uberRouter._testonly.doHandle(event, callback, stubDb, () => { return 42 })
    })

    it('should return a 200 response when we do a simple request', () => {
      expect(result.statusCode).toBe(200)
      expect(result.headers).toEqual({
        'Access-Control-Allow-Origin': '*',
        'Access-Control-Allow-Credentials': true,
        'Access-Control-Expose-Headers': 'link',
        'Content-Type': "'text/csv'",
        'link': '<https://api.aekos.org.au/v1/allSpeciesData.csv?rows=15&start=15>; rel="next", ' +
                '<https://api.aekos.org.au/v1/allSpeciesData.csv?rows=15&start=30>; rel="last"'
      })
      expect(result.body.split('\n')).toEqual([
        `"decimalLatitude","decimalLongitude","geodeticDatum","locationID","scientificName","taxonRemarks","individualCount","eventDate","year","month","bibliographicCitation","samplingProtocol"`,
        `-33.59758852952833,120.15956081537496,"GDA94","aekos.org.au/collection/wa.gov.au/ravensthorpe/R181","Acacia binata Maslin",,1,"2007-10-03",2007,10,"Department of Par...","aekos.org.au/collection/wa.gov.au/ravensthorpe"`
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
        queryStringParameters: {
          rows: '15',
          start: '0',
          download: 'true' // trigger response as download
        },
        headers: {
          Host: 'api.aekos.org.au',
          'X-Forwarded-Proto': 'https'
        },
        requestContext: { path: '/v1/allSpeciesData.csv' },
        path: '/v1/allSpeciesData.csv'
      }
      let callback = (_, theResult) => {
        result = theResult
        done()
      }
      uberRouter._testonly.doHandle(event, callback, stubDb, () => { return 42 })
    })

    it('should respond as a download when the download param is supplied', () => {
      expect(result.statusCode).toBe(200)
      expect(result.headers).toEqual({
        'Access-Control-Allow-Origin': '*',
        'Access-Control-Allow-Credentials': true,
        'Access-Control-Expose-Headers': 'link',
        'Content-Type': "'text/csv'",
        'Content-Disposition': 'attachment;filename=aekosSpeciesData.csv',
        'link': ''
      })
      expect(result.body.split('\n')).toEqual([
        `"decimalLatitude","decimalLongitude","geodeticDatum","locationID","scientificName","taxonRemarks","individualCount","eventDate","year","month","bibliographicCitation","samplingProtocol"`,
        `-33.59758852952833,120.15956081537496,"GDA94","aekos.org.au/collection/wa.gov.au/ravensthorpe/R181","Acacia binata Maslin",,1,"2007-10-03",2007,10,"Department of Par...","aekos.org.au/collection/wa.gov.au/ravensthorpe"`
      ])
    })
  })

  describe('mapJsonToCsv', () => {
    it('should map the records to CSV when they are fully populated', () => {
      let records = [
        {
          scientificName: 'Acacia cincinnata',
          taxonRemarks: null,
          individualCount: 1,
          eventDate: '2014-09-11',
          month: 9,
          year: 2014,
          decimalLatitude: -17.841178,
          decimalLongitude: 145.584496,
          geodeticDatum: 'GDA94',
          locationID: 'aekos.org.au/collection/adelaide.edu.au/TAF/QDFWET0004',
          samplingProtocol: 'aekos.org.au/collection/adelaide.edu.au/TAF',
          bibliographicCitation: 'Wood SW, Bowman DMJS, Ste...'
        },
        {
          scientificName: 'Acacia cincinnata',
          taxonRemarks: null,
          individualCount: 1,
          eventDate: '1991-01-22',
          month: 1,
          year: 1991,
          decimalLatitude: -17.841178,
          decimalLongitude: 145.584496,
          geodeticDatum: 'GDA94',
          locationID: 'aekos.org.au/collection/adelaide.edu.au/TAF/QDFWET0004',
          samplingProtocol: 'aekos.org.au/collection/adelaide.edu.au/TAF',
          bibliographicCitation: 'Wood SW, Bowman DMJS, Ste...'
        }
      ]
      let result = objectUnderTest.mapJsonToCsv(records, objectUnderTest.v1CsvHeaders)
      let resultLines = result.split('\n')
      expect(resultLines.length).toBe(3)
      expect(resultLines[0]).toBe('"decimalLatitude","decimalLongitude","geodeticDatum","locationID","scientificName","taxonRemarks",' +
        '"individualCount","eventDate","year","month","bibliographicCitation","samplingProtocol"')
      expect(resultLines[1]).toBe('-17.841178,145.584496,"GDA94","aekos.org.au/collection/adelaide.edu.au/TAF/QDFWET0004","Acacia cincinnata",,' +
        '1,"2014-09-11",2014,9,"Wood SW, Bowman DMJS, Ste...","aekos.org.au/collection/adelaide.edu.au/TAF"')
      expect(resultLines[2]).toBe('-17.841178,145.584496,"GDA94","aekos.org.au/collection/adelaide.edu.au/TAF/QDFWET0004","Acacia cincinnata",,' +
        '1,"1991-01-22",1991,1,"Wood SW, Bowman DMJS, Ste...","aekos.org.au/collection/adelaide.edu.au/TAF"')
    })
  })

  describe('getCsvHeaderRow', () => {
    it('should return the expected row', () => {
      let result = objectUnderTest._testonly.getCsvHeaderRow(objectUnderTest.v1CsvHeaders)
      expect(result).toBe('"decimalLatitude","decimalLongitude","geodeticDatum","locationID","scientificName","taxonRemarks","individualCount","eventDate","year","month","bibliographicCitation","samplingProtocol"')
    })
  })

  describe('createCsvRow', () => {
    it('should map the record to CSV when all fields are populated', () => {
      let record = {
        scientificName: 'Acacia cincinnata',
        taxonRemarks: null,
        individualCount: 1,
        eventDate: '1991-01-22',
        month: 1,
        year: 1991,
        decimalLatitude: -17.841178,
        decimalLongitude: 145.584496,
        geodeticDatum: 'GDA94',
        locationID: 'aekos.org.au/collection/adelaide.edu.au/TAF/QDFWET0004',
        samplingProtocol: 'aekos.org.au/collection/adelaide.edu.au/TAF',
        bibliographicCitation: 'Wood SW, Bowman DMJS, Ste...'
      }
      let result = objectUnderTest.createCsvRow(objectUnderTest.v1CsvHeaders, record)
      expect(result).toBe('-17.841178,145.584496,"GDA94","aekos.org.au/collection/adelaide.edu.au/TAF/QDFWET0004","Acacia cincinnata",,' +
        '1,"1991-01-22",1991,1,"Wood SW, Bowman DMJS, Ste...","aekos.org.au/collection/adelaide.edu.au/TAF"')
    })
  })
})

'use strict'
let objectUnderTest = require('../allSpeciesData-csv')
let uberRouter = require('../uberRouter')
let StubDB = require('./StubDB')

describe('/v2/allSpeciesData.csv', () => {
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
        queryStringParameters: {
          rows: '15',
          start: '0'
        },
        headers: {
          Host: 'api.aekos.org.au',
          'X-Forwarded-Proto': 'https'
        },
        requestContext: { path: '/v2/allSpeciesData.csv' },
        path: '/v2/allSpeciesData.csv'
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
        'link': '<https://api.aekos.org.au/v2/allSpeciesData.csv?rows=15&start=15>; rel="next", ' +
                '<https://api.aekos.org.au/v2/allSpeciesData.csv?rows=15&start=30>; rel="last"'
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
        queryStringParameters: {
          rows: '15',
          start: '0',
          download: 'true' // trigger response as download
        },
        headers: {
          Host: 'api.aekos.org.au',
          'X-Forwarded-Proto': 'https'
        },
        requestContext: { path: '/v2/allSpeciesData.csv' },
        path: '/v2/allSpeciesData.csv'
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
        `"decimalLatitude","decimalLongitude","geodeticDatum","locationID","locationName","datasetName","scientificName","taxonRemarks","individualCount","eventDate","year","month","bibliographicCitation","samplingProtocol"`,
        `-33.59758852952833,120.15956081537496,"GDA94","aekos.org.au/collection/wa.gov.au/ravensthorpe/R181","R181","Biological Survey of the Ravensthorpe Range (Phase 1)","Acacia binata Maslin",,1,"2007-10-03",2007,10,"Department of Par...","aekos.org.au/collection/wa.gov.au/ravensthorpe"`
      ])
    })
  })

  describe('.validator()', () => {
    it('should be valid with true download param', () => {
      let queryStringObj = {
        download: 'true'
      }
      let result = objectUnderTest._testonly.validator(queryStringObj, null)
      expect(result.isValid).toBe(true)
    })

    it('should be valid with false download param', () => {
      let queryStringObj = {
        download: 'false'
      }
      let result = objectUnderTest._testonly.validator(queryStringObj, null)
      expect(result.isValid).toBe(true)
    })

    it('should be invalid when download param is not a boolean string', () => {
      let queryStringObj = {
        download: 'blah'
      }
      let result = objectUnderTest._testonly.validator(queryStringObj, null)
      expect(result.isValid).toBe(false)
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
          locationName: 'QDFWET0004',
          samplingProtocol: 'aekos.org.au/collection/adelaide.edu.au/TAF',
          bibliographicCitation: 'Wood SW, Bowman DMJS, Ste...',
          datasetName: 'TERN AusPlots Forests Monitoring Network'
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
          locationName: 'QDFWET0004',
          samplingProtocol: 'aekos.org.au/collection/adelaide.edu.au/TAF',
          bibliographicCitation: 'Wood SW, Bowman DMJS, Ste...',
          datasetName: 'TERN AusPlots Forests Monitoring Network'
        }
      ]
      let result = objectUnderTest.mapJsonToCsv(records, objectUnderTest.v2CsvHeaders)
      let resultLines = result.split('\n')
      expect(resultLines.length).toBe(3)
      expect(resultLines[0]).toBe('"decimalLatitude","decimalLongitude","geodeticDatum","locationID","locationName","datasetName","scientificName","taxonRemarks",' +
        '"individualCount","eventDate","year","month","bibliographicCitation","samplingProtocol"')
      expect(resultLines[1]).toBe('-17.841178,145.584496,"GDA94","aekos.org.au/collection/adelaide.edu.au/TAF/QDFWET0004","QDFWET0004","TERN AusPlots Forests Monitoring Network","Acacia cincinnata",,' +
        '1,"2014-09-11",2014,9,"Wood SW, Bowman DMJS, Ste...","aekos.org.au/collection/adelaide.edu.au/TAF"')
      expect(resultLines[2]).toBe('-17.841178,145.584496,"GDA94","aekos.org.au/collection/adelaide.edu.au/TAF/QDFWET0004","QDFWET0004","TERN AusPlots Forests Monitoring Network","Acacia cincinnata",,' +
        '1,"1991-01-22",1991,1,"Wood SW, Bowman DMJS, Ste...","aekos.org.au/collection/adelaide.edu.au/TAF"')
    })
  })

  describe('getCsvHeaderRow', () => {
    it('should return the expected row', () => {
      let result = objectUnderTest._testonly.getCsvHeaderRow(objectUnderTest.v2CsvHeaders)
      expect(result).toBe('"decimalLatitude","decimalLongitude","geodeticDatum","locationID","locationName","datasetName","scientificName","taxonRemarks","individualCount","eventDate","year","month","bibliographicCitation","samplingProtocol"')
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
        locationName: 'QDFWET0004',
        samplingProtocol: 'aekos.org.au/collection/adelaide.edu.au/TAF',
        bibliographicCitation: 'Wood SW, Bowman DMJS, Ste...',
        datasetName: 'TERN AusPlots Forests Monitoring Network'
      }
      let result = objectUnderTest.createCsvRow(objectUnderTest.v2CsvHeaders, record)
      expect(result).toBe('-17.841178,145.584496,"GDA94","aekos.org.au/collection/adelaide.edu.au/TAF/QDFWET0004","QDFWET0004","TERN AusPlots Forests Monitoring Network","Acacia cincinnata",,' +
        '1,"1991-01-22",1991,1,"Wood SW, Bowman DMJS, Ste...","aekos.org.au/collection/adelaide.edu.au/TAF"')
    })
  })

  describe('.getCsvDownloadFileName()', () => {
    it('should return null when no query parameters are present', () => {
      let event = {
        queryStringParameters: null
      }
      let result = objectUnderTest.getCsvDownloadFileName(event, 'Species')
      expect(result).toBeNull()
    })

    it('should return null when the "download" parameter is not present', () => {
      let event = {
        queryStringParameters: {
          someParam: 'blah'
        }
      }
      let result = objectUnderTest.getCsvDownloadFileName(event, 'Species')
      expect(result).toBeNull()
    })

    it('should return null when the "download" param is not a string', () => {
      let event = {
        queryStringParameters: {
          download: false
        }
      }
      let result = objectUnderTest.getCsvDownloadFileName(event, 'Species')
      expect(result).toBeNull()
    })

    it('should return null when the "download" param is false', () => {
      let event = {
        queryStringParameters: {
          download: 'false'
        }
      }
      let result = objectUnderTest.getCsvDownloadFileName(event, 'Species')
      expect(result).toBeNull()
    })

    it('should return the expected filename when "download" param is true', () => {
      let event = {
        queryStringParameters: {
          download: 'true'
        }
      }
      let result = objectUnderTest.getCsvDownloadFileName(event, 'Species')
      expect(result).toBe('aekosSpeciesData.csv')
    })

    it('should return the expected filename when "download" param is true but not lowercase', () => {
      let event = {
        queryStringParameters: {
          download: 'TRue'
        }
      }
      let result = objectUnderTest.getCsvDownloadFileName(event, 'Species')
      expect(result).toBe('aekosSpeciesData.csv')
    })
  })
})

'use strict'
var objectUnderTest = require('../v1-traitData-csv')
let StubDB = require('./StubDB')

describe('v1-traitData-csv', () => {
  describe('doHandle', () => {
    it('should return a 200 response when we return all traits for a species', (done) => {
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
        [ {recordsHeld: 31} ],
        [
          { traitName: 'trait1', traitValue: 'value1', traitUnit: 'unit1' },
          { traitName: 'trait2', traitValue: 'value2', traitUnit: 'unit2' }
        ]
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
          `"decimalLatitude","decimalLongitude","geodeticDatum","locationID","scientificName","taxonRemarks","individualCount","eventDate","year","month","bibliographicCitation","samplingProtocol","trait1Name","trait1Value","trait1Units","trait2Name","trait2Value","trait2Units"`,
          `-33.59758852952833,120.15956081537496,"GDA94","aekos.org.au/collection/wa.gov.au/ravensthorpe/R181","Acacia binata Maslin",,1,"2007-10-03",2007,10,"Department of Par...","aekos.org.au/collection/wa.gov.au/ravensthorpe","trait1","value1","unit1","trait2","value2","unit2"`
        ])
        done()
      }
      objectUnderTest._testonly.doHandle(event, callback, stubDb, () => { return 42 })
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
          datasetName: 'TERN AusPlots Forests Monitoring Network',
          traits: []
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
          datasetName: 'TERN AusPlots Forests Monitoring Network',
          traits: [
            {
              traitName: 'height',
              traitValue: '13.8',
              traitUnit: 'metres'
            }
          ]
        }
      ]
      let result = objectUnderTest.mapJsonToCsv(records)
      let resultLines = result.split('\n')
      expect(resultLines.length).toBe(3)
      expect(resultLines[0]).toBe('"decimalLatitude","decimalLongitude","geodeticDatum","locationID","scientificName","taxonRemarks",' +
        '"individualCount","eventDate","year","month","bibliographicCitation","samplingProtocol","trait1Name","trait1Value","trait1Units"')
      expect(resultLines[1]).toBe('-17.841178,145.584496,"GDA94","aekos.org.au/collection/adelaide.edu.au/TAF/QDFWET0004","Acacia cincinnata",,' +
        '1,"2014-09-11",2014,9,"Wood SW, Bowman DMJS, Ste...","aekos.org.au/collection/adelaide.edu.au/TAF"')
      expect(resultLines[2]).toBe('-17.841178,145.584496,"GDA94","aekos.org.au/collection/adelaide.edu.au/TAF/QDFWET0004","Acacia cincinnata",,' +
        '1,"1991-01-22",1991,1,"Wood SW, Bowman DMJS, Ste...","aekos.org.au/collection/adelaide.edu.au/TAF","height","13.8","metres"')
    })
  })

  describe('getCsvHeaderRow', () => {
    it('should return the expected row', () => {
      let maxNumOfTraits = 2
      let result = objectUnderTest.getCsvHeaderRow(maxNumOfTraits)
      expect(result).toBe('"decimalLatitude","decimalLongitude","geodeticDatum","locationID","scientificName","taxonRemarks","individualCount","eventDate","year","month","bibliographicCitation","samplingProtocol","trait1Name","trait1Value","trait1Units","trait2Name","trait2Value","trait2Units"')
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
        datasetName: 'TERN AusPlots Forests Monitoring Network',
        traits: [
          {
            traitName: 'height',
            traitValue: '13.8',
            traitUnit: 'metres'
          }
        ]
      }
      let result = objectUnderTest.createCsvRow(record)
      expect(result).toBe('-17.841178,145.584496,"GDA94","aekos.org.au/collection/adelaide.edu.au/TAF/QDFWET0004","Acacia cincinnata",,' +
        '1,"1991-01-22",1991,1,"Wood SW, Bowman DMJS, Ste...","aekos.org.au/collection/adelaide.edu.au/TAF","height","13.8","metres"')
    })

    it('should handle a null unit', () => {
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
        datasetName: 'TERN AusPlots Forests Monitoring Network',
        traits: [
          {
            traitName: 'lifeStage',
            traitValue: 'mature',
            traitUnit: null
          }
        ]
      }
      let result = objectUnderTest.createCsvRow(record)
      expect(result).toBe('-17.841178,145.584496,"GDA94","aekos.org.au/collection/adelaide.edu.au/TAF/QDFWET0004","Acacia cincinnata",,' +
        '1,"1991-01-22",1991,1,"Wood SW, Bowman DMJS, Ste...","aekos.org.au/collection/adelaide.edu.au/TAF","lifeStage","mature",')
    })
  })
})
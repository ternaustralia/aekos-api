'use strict'

var objectUnderTest = require('../v10-traitData-csv')

describe('v10-traitData-csv', () => {
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

  describe('getCsvRow', () => {
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

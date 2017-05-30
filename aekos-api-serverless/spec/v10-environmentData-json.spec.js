'use strict'
let Set = require('collections/set')
let objectUnderTest = require('../v10-environmentData-json')

describe('v10-environmentData-json', () => {
  describe('appendVars', () => {
    it('should map variables to records', () => {
      let records = [
        {
          visitKey: 'aekos.org.au/collection/adelaide.edu.au/TAF/NSFSEC0001#2014-05-09',
          eventDate: '2014-05-09',
          month: 5,
          year: 2014,
          decimalLatitude: -36.759165,
          decimalLongitude: 149.435125,
          geodeticDatum: 'GDA94',
          locationID: 'aekos.org.au/collection/adelaide.edu.au/TAF/NSFSEC0001',
          locationName: 'NSFSEC0001',
          samplingProtocol: 'aekos.org.au/collection/adelaide.edu.au/TAF',
          bibliographicCitation: 'Wood SW, Bowman DMJ...',
          datasetName: 'TERN AusPlots Forests Monitoring Network'
        }
      ]
      let varsLookup = {
        'aekos.org.au/collection/adelaide.edu.au/TAF/NSFSEC0001#2014-05-09': [
          {
            varName: 'disturbanceType',
            varValue: 'none',
            varUnit: null
          },
          {
            varName: 'aspect',
            varValue: '260',
            varUnit: 'degrees'
          },
          {
            varName: 'slope',
            varValue: '4',
            varUnit: 'degrees'
          }
        ]
      }
      objectUnderTest._testonly.appendVars(records, varsLookup)
      let result = records
      let firstRecord = result[0]
      expect(firstRecord.visitKey).toBe('aekos.org.au/collection/adelaide.edu.au/TAF/NSFSEC0001#2014-05-09')
      expect(firstRecord.variables.length).toBe(3)
      let firstVar = firstRecord.variables[0]
      expect(firstVar.varName).toBe('disturbanceType')
      expect(firstVar.varValue).toBe('none')
      expect(firstVar.varUnit).toBeNull()
      let secondVar = firstRecord.variables[1]
      expect(secondVar.varName).toBe('aspect')
      expect(secondVar.varValue).toBe('260')
      expect(secondVar.varUnit).toBe('degrees')
      let thirdVar = firstRecord.variables[2]
      expect(thirdVar.varName).toBe('slope')
      expect(thirdVar.varValue).toBe('4')
      expect(thirdVar.varUnit).toBe('degrees')
    })
  })

  describe('appendSpeciesNames', () => {
    it('should map species names to records', () => {
      let records = [
        {
          visitKey: 'aekos.org.au/collection/adelaide.edu.au/TAF/NSFSEC0001#2014-05-09',
          eventDate: '2014-05-09',
          month: 5,
          year: 2014,
          decimalLatitude: -36.759165,
          decimalLongitude: 149.435125,
          geodeticDatum: 'GDA94',
          locationID: 'aekos.org.au/collection/adelaide.edu.au/TAF/NSFSEC0001',
          locationName: 'NSFSEC0001',
          samplingProtocol: 'aekos.org.au/collection/adelaide.edu.au/TAF',
          bibliographicCitation: 'Wood SW, Bowman DMJ...',
          datasetName: 'TERN AusPlots Forests Monitoring Network'
        }
      ]
      let speciesNameLookup = {
        'aekos.org.au/collection/adelaide.edu.au/TAF/NSFSEC0001#2014-05-09': {
          scientificNames: new Set(['Acacia dealbata', 'species two']),
          taxonRemarks: new Set(['Big tree', 'Some other taxon remark', 'taxon three'])
        }
      }
      objectUnderTest._testonly.appendSpeciesNames(records, speciesNameLookup)
      let result = records
      let firstRecord = result[0]
      expect(firstRecord.visitKey).toBe('aekos.org.au/collection/adelaide.edu.au/TAF/NSFSEC0001#2014-05-09')
      expect(firstRecord.scientificNames.length).toBe(2)
      expect(firstRecord.scientificNames).toContain('Acacia dealbata', 'species two')
      expect(firstRecord.scientificNames.constructor).toBe(Array)
      expect(firstRecord.taxonRemarks.length).toBe(3)
      expect(firstRecord.taxonRemarks).toContain('Big tree', 'Some other taxon remark', 'taxon three')
      expect(firstRecord.taxonRemarks.constructor).toBe(Array)
    })
  })

  describe('stripVisitKeys', () => {
    it('should remove the visitKey from all records', () => {
      let records = [
        {
          visitKey: 'aekos.org.au/collection/test.edu.au/ONE/RECORD0001#2001-01-01',
          datasetName: 'Record 1'
        }, {
          visitKey: 'aekos.org.au/collection/test.edu.au/TWO/RECORD0002#2002-02-02',
          datasetName: 'Record 2'
        }
      ]
      objectUnderTest._testonly.stripVisitKeys(records)
      let result = records
      result.forEach(e => {
        expect(e.visitKey).toBeUndefined()
      })
    })
  })
})

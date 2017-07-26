'use strict'

let objectUnderTest = require('../environmentData-csv')

describe('/v2/environmentData-csv', function () {
  describe('createCsvRow', () => {
    it('should map the object to a CSV row', function () {
      let record = {
        'eventDate': '2014-05-09',
        'month': 5,
        'year': 2014,
        'decimalLatitude': -38.759165,
        'decimalLongitude': 143.435125,
        'geodeticDatum': 'GDA94',
        'locationID': 'aekos.org.au/collection/test.edu.au/TEST/AAAA0001',
        'locationName': 'AAAA0001',
        'samplingProtocol': 'aekos.org.au/collection/test.edu.au/TEST',
        'bibliographicCitation': 'A Person, B Person...',
        'datasetName': 'TEST dataset',
        'variables': [
          {
            'varName': 'disturbanceType',
            'varValue': 'none',
            'varUnit': null
          },
          {
            'varName': 'slope',
            'varValue': '4',
            'varUnit': 'degrees'
          },
          {
            'varName': 'aspect',
            'varValue': '260',
            'varUnit': 'degrees'
          }
        ],
        'scientificNames': [
          'Acacia dealbata',
          'Acacia aneura'
        ],
        'taxonRemarks': [
          'Grass',
          'Clover'
        ]
      }
      let result = objectUnderTest.createCsvRow(record, objectUnderTest.v2CsvHeaders)
      expect(result).toBe('-38.759165,143.435125,"GDA94","aekos.org.au/collection/test.edu.au/TEST/AAAA0001",' +
        '"AAAA0001","TEST dataset","Acacia dealbata|Acacia aneura","Grass|Clover","2014-05-09",2014,5,"A Person, B Person...",' +
        '"aekos.org.au/collection/test.edu.au/TEST","disturbanceType","none",,"slope",' +
        '"4","degrees","aspect","260","degrees"')
    })
  })
})

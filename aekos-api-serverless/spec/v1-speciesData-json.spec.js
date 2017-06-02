'use strict'
let objectUnderTest = require('../v1-speciesData-json')

describe('v1-speciesData-json', () => {
  describe('extractParams', () => {
    it('should extract the params when they are present', () => {
      let event = {
        queryStringParameters: {
          speciesName: 'species one',
          rows: 15,
          start: 0
        }
      }
      let result = objectUnderTest.extractParams(event)
      expect(result.speciesName).toBe("'species one'")
      expect(result.unescapedSpeciesName).toBe('species one')
      expect(result.rows).toBe(15)
      expect(result.start).toBe(0)
    })

    it('should extract the params when they are present', () => {
      let event = {
        queryStringParameters: {
          speciesName: 'species two'
        }
      }
      let result = objectUnderTest.extractParams(event)
      expect(result.speciesName).toBe("'species two'")
      expect(result.unescapedSpeciesName).toBe('species two')
      expect(result.rows).toBe(20)
      expect(result.start).toBe(0)
    })
  })
})

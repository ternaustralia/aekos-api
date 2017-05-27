'use strict'

var objectUnderTest = require('../v10-traitData')

describe('v10-traitData', () => {
  describe('getContentType', () => {
    it('should determine that we want CSV', () => {
      let event = {
        headers: {
          Accept: 'text/csv,application/xml;q=0.9,image/webp'
        }
      }
      let result = objectUnderTest._testonly.getContentType(event)
      expect(result).toBe(objectUnderTest._testonly.contentTypes.csv)
    })

    it('should determine that we want JSON', () => {
      let event = {
        headers: {
          Accept: 'application/json;q=0.9,image/webp'
        }
      }
      let result = objectUnderTest._testonly.getContentType(event)
      expect(result).toBe(objectUnderTest._testonly.contentTypes.json)
    })

    it('should determine that we cannot handle the requested type', () => {
      let event = {
        headers: {
          Accept: 'text/html;q=0.9,image/webp'
        }
      }
      let result = objectUnderTest._testonly.getContentType(event)
      expect(result).toBe(objectUnderTest._testonly.contentTypes.unhandled)
    })
  })
})

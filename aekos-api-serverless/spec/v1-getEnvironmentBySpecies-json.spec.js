'use strict'
let objectUnderTest = require('../v1-getEnvironmentBySpecies-json')
let StubDB = require('./StubDB')

describe('v1-getEnvironmentBySpecies-json', function () {
  describe('.responder()', () => {
    it('should return the expected result', () => {
      let stubDb = new StubDB()
      stubDb.setExecSelectPromiseResponses([
        [{ code: 'clay', recordsHeld: 123 }]
      ])
      let queryStringObj = {}
      let requestBody = {
        speciesNames: ['species one']
      }
      objectUnderTest._testonly.responder(requestBody, stubDb, queryStringObj).then(result => {
        expect(result).toEqual([{ code: 'clay', recordsHeld: 123, label: 'Clay Content' }])
      }).catch(fail)
    })
  })

  describe('validator', () => {
    it('should validate with a single species name', () => {
      let requestBody = {
        speciesNames: ['species one']
      }
      let result = objectUnderTest._testonly.validator(requestBody, {})
      expect(result.isValid).toBe(true)
    })

    it('should fail validation with no species names', () => {
      let requestBody = {
        // no 'speciesNames'
      }
      let result = objectUnderTest._testonly.validator(requestBody, {})
      expect(result.isValid).toBe(false)
    })

    it('should validate with a single species name and paging params', () => {
      let requestBody = {
        speciesNames: ['species one']
      }
      let queryString = {
        pageSize: '33',
        pageNum: '3'
      }
      let result = objectUnderTest._testonly.validator(requestBody, queryString)
      expect(result.isValid).toBe(true)
    })
  })
})

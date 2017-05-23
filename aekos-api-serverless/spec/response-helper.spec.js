'use strict'

let objectUnderTest = require('../response-helper')

describe('response-helper', function () {
  describe('ok', function () {
    it('should call the supplied callback', function () {
      let isCalled = false
      let callback = function () {
        isCalled = true
      }
      objectUnderTest.ok(callback, 'some value')
      expect(isCalled).toBeTruthy()
    })

    it('should send the supplied body to the callback', function () {
      let suppliedArg = null
      let callback = function (first, second) {
        suppliedArg = second
      }
      objectUnderTest.ok(callback, 'some value')
      expect(suppliedArg.body).toBe('"some value"')
    })
  })

  describe('isQueryStringParamPresent', function () {
    it('should be present', function () {
      let event = {
        queryStringParameters: {
          someParam: 'blah'
        }
      }
      let result = objectUnderTest.isQueryStringParamPresent(event, 'someParam')
      expect(result).toBeTruthy()
    })

    it('should NOT be present when other params are present', function () {
      let event = {
        queryStringParameters: {
          someParam: 'blah'
        }
      }
      let result = objectUnderTest.isQueryStringParamPresent(event, 'someUnsuppliedParam')
      expect(result).toBeFalsy()
    })

    it('should NOT be present when no params are present', function () {
      let event = {
        queryStringParameters: null
      }
      let result = objectUnderTest.isQueryStringParamPresent(event, 'someUnsuppliedParam')
      expect(result).toBeFalsy()
    })
  })
})

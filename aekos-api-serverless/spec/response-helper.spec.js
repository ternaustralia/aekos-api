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

  describe('assertNumber', function () {
    it('can tell when we do have a number', function () {
      let val = 1
      let result = objectUnderTest.assertNumber(val)
      expect(result).toBeTruthy()
    })

    it('can tell when we DO NOT have a number (a number string is not a number)', function () {
      let val = '1'
      let result = objectUnderTest.assertNumber(val)
      expect(result).toBeFalsy()
    })

    it('can tell when we DO NOT have a number', function () {
      let val = 'blah'
      let result = objectUnderTest.assertNumber(val)
      expect(result).toBeFalsy()
    })
  })

  describe('getOptionalValue', function () {
    it('can get the value when it is present', function () {
      let event = {
        queryStringParameters: {
          pageSize: 10
        }
      }
      let result = objectUnderTest.getOptionalParam(event, 'pageSize', 20)
      expect(result).toBe(10)
    })

    it('can get the default when the value is NOT present', function () {
      let event = {
        queryStringParameters: {
          somethingElse: 'foo'
        }
      }
      let result = objectUnderTest.getOptionalParam(event, 'pageSize', 20)
      expect(result).toBe(20)
    })

    it('can get the default when no params are present', function () {
      let event = {
        queryStringParameters: null
      }
      let result = objectUnderTest.getOptionalParam(event, 'pageSize', 20)
      expect(result).toBe(20)
    })
  })

  describe('calculateOffset', function () {
    it('returns 0 offset for the first page', function () {
      let pageNum = 1
      let pageSize = 20
      let result = objectUnderTest.calculateOffset(pageNum, pageSize)
      expect(result).toBe(0)
    })

    it('returns 0 offset for the first page with a different page size', function () {
      let pageNum = 1
      let pageSize = 100
      let result = objectUnderTest.calculateOffset(pageNum, pageSize)
      expect(result).toBe(0)
    })

    it('returns the correct offset for the second page', function () {
      let pageNum = 2
      let pageSize = 20
      let result = objectUnderTest.calculateOffset(pageNum, pageSize)
      expect(result).toBe(20)
    })
  })
})

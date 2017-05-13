'use strict'

var objectUnderTest = require('../response-helper')

describe('response-helper', function () {
  it('should call the supplied callback', function () {
    var isCalled = false
    var callback = function () {
      isCalled = true
    }
    objectUnderTest.ok(callback, 'some value')
    expect(isCalled).toBeTruthy()
  })

  it('should send the supplied body to the callback', function () {
    var suppliedArg = null
    var callback = function (first, second) {
      suppliedArg = second
    }
    objectUnderTest.ok(callback, 'some value')
    expect(suppliedArg.body).toBe('"some value"')
  })
})

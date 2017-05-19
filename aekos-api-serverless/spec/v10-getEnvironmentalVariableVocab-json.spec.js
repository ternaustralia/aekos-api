'use strict'

var objectUnderTest = require('../v10-getEnvironmentalVariableVocab-json')

describe('v10-getEnvironmentalVariableVocab-json', function () {
  // FIXME update when target function has DB stuff extracted
  it('should call the callback', function () {
    var isCallbackCalled = false
    objectUnderTest.handler(null, null, () => {
      isCallbackCalled = true
    })
    expect(isCallbackCalled).toBeTruthy()
  })
})

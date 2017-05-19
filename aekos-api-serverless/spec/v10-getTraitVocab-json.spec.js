'use strict'

var objectUnderTest = require('../v10-getTraitVocab-json')

describe('v10-getTraitVocab-json', function () {
  it('should call the callback', function () {
    var isCallbackCalled = false
    objectUnderTest.handler(null, null, () => {
        isCallbackCalled = true
    })
    expect(isCallbackCalled).toBeTruthy()
  })
})

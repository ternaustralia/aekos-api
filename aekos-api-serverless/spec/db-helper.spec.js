'use strict'

var objectUnderTest = require('../db-helper')

describe('DB Helper', function () {
  it('should quote a string', function () {
    let value = 'blah'
    let result = objectUnderTest.escape(value)
    expect(result).toBe("'blah'")
  })

  it('should escape an SQL injection attack', function () {
    let value = "'; SELECT 1;--"
    let result = objectUnderTest.escape(value)
    expect(result).toBe("'\\'; SELECT 1;--'")
  })

  it('should be able to handle a trailing % for a LIKE clause', function () {
    let value = 'aca%'
    let result = objectUnderTest.escape(value)
    expect(result).toBe("'aca%'")
  })
})
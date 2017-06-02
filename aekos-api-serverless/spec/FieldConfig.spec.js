'use strict'
let FieldConfig = require('../FieldConfig')

describe('FieldConfig', () => {
  describe('quoted', () => {
    let objectUnderTest = FieldConfig.quoted('someField')

    it('should indicate that it is quoted', () => {
      expect(objectUnderTest.isQuoted).toBeTruthy()
      expect(objectUnderTest.name).toBe('someField')
    })

    it('should get the quoted value when it is present', () => {
      let targetObj = {
        someField: 'blah'
      }
      let result = objectUnderTest.getValue(targetObj)
      expect(result).toBe('"blah"')
    })

    it('should get an empty string when it is not present (null)', () => {
      let targetObj = {
        someField: null
      }
      let result = objectUnderTest.getValue(targetObj)
      expect(result).toBe('')
    })
  })

  describe('notQuoted', () => {
    let objectUnderTest = FieldConfig.notQuoted('someField')

    it('should indicate that it is NOT quoted', () => {
      expect(objectUnderTest.isQuoted).toBeFalsy()
      expect(objectUnderTest.name).toBe('someField')
    })

    it('should get the unquoted value when it is present', () => {
      let targetObj = {
        someField: '123'
      }
      let result = objectUnderTest.getValue(targetObj)
      expect(result).toBe('123')
    })

    it('should get an empty string when it is not present (null)', () => {
      let targetObj = {
        someField: null
      }
      let result = objectUnderTest.getValue(targetObj)
      expect(result).toBe('')
    })
  })

  describe('quotedListConcat', () => {
    let objectUnderTest = FieldConfig.quotedListConcat('someField')

    it('should get the quoted and concatenated list value when it is present', () => {
      let targetObj = {
        someField: ['one', 'two', 'three with spaces']
      }
      let result = objectUnderTest.getValue(targetObj)
      expect(result).toBe('"one|two|three with spaces"')
    })

    it('should be able to handle a single element', () => {
      let targetObj = {
        someField: ['one']
      }
      let result = objectUnderTest.getValue(targetObj)
      expect(result).toBe('"one"')
    })

    it('should get an empty string when it is not present (null)', () => {
      let targetObj = {
        someField: null
      }
      let result = objectUnderTest.getValue(targetObj)
      expect(result).toBe('')
    })

    it('should throw an error when the field is present but not an Array', () => {
      let targetObj = {
        someField: 'not an array :('
      }
      expect(() => {
        objectUnderTest.getValue(targetObj)
      }).toThrow()
    })
  })
})

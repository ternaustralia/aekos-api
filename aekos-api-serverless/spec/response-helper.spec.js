'use strict'

let objectUnderTest = require('../response-helper')

describe('response-helper', function () {
  describe('json', function () {
    describe('ok', function () {
      it('should call the supplied callback', function () {
        let isCalled = false
        let callback = function () {
          isCalled = true
        }
        objectUnderTest.json.ok(callback, 'some value')
        expect(isCalled).toBeTruthy()
      })

      it('should send the supplied body to the callback', function () {
        let suppliedArg = null
        let callback = function (first, second) {
          suppliedArg = second
        }
        objectUnderTest.json.ok(callback, 'some value')
        expect(suppliedArg.body).toBe('"some value"')
      })
    })
  })

  describe('csv', function () {
    describe('ok', function () {
      it('should send the supplied body to the callback without JSON.stringify-ing it', function () {
        let suppliedArg = null
        let callback = function (first, second) {
          suppliedArg = second
        }
        objectUnderTest.csv.ok(callback, '1,"some value",123\n2,"other value",456')
        expect(suppliedArg.body).toBe('1,"some value",123\n2,"other value",456')
      })
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
      objectUnderTest.assertNumber(val)
    })

    it('can tell when we DO NOT have a number (a number string is not a number)', function () {
      let val = '1'
      expect(() => {
        objectUnderTest.assertNumber(val)
      }).toThrow()
    })

    it('can tell when we DO NOT have a number', function () {
      let val = 'blah'
      expect(() => {
        objectUnderTest.assertNumber(val)
      }).toThrow()
    })

    it('can tell when we DO NOT have a number', function () {
      let val = {}
      expect(() => {
        objectUnderTest.assertNumber(val)
      }).toThrow()
    })
  })

  describe('getOptionalStringParam', function () {
    it('can get the value when it is present', function () {
      let event = {
        queryStringParameters: {
          someParam: 'blah'
        }
      }
      let result = objectUnderTest.getOptionalStringParam(event, 'someParam', 'theDefault')
      expect(result).toBe('blah')
    })

    it('can use the default when it is NOT present', function () {
      let event = {
        queryStringParameters: {
          somethingElse: 'ignore me'
        }
      }
      let result = objectUnderTest.getOptionalStringParam(event, 'someParam', 'theDefault')
      expect(result).toBe('theDefault')
    })

    it('should throw when the value is not a string', function () {
      let event = {
        queryStringParameters: {
          someParam: 123
        }
      }
      expect(() => {
        objectUnderTest.getOptionalStringParam(event, 'someParam', 'theDefault')
      }).toThrow()
    })

    it('should throw when the value is not a string', function () {
      let event = {
        queryStringParameters: {
          someParam: {}
        }
      }
      expect(() => {
        objectUnderTest.getOptionalStringParam(event, 'someParam', 'theDefault')
      }).toThrow()
    })
  })

  describe('getOptionalNumberParam', function () {
    it('can get the value when it is present as a number', function () {
      let event = {
        queryStringParameters: {
          pageSize: 10
        }
      }
      let result = objectUnderTest.getOptionalNumberParam(event, 'pageSize', 20)
      expect(result).toBe(10)
    })

    it('can get the value when it is present as a string', function () {
      let event = {
        queryStringParameters: {
          pageSize: '42'
        }
      }
      let result = objectUnderTest.getOptionalNumberParam(event, 'pageSize', 20)
      expect(result).toBe(42)
    })

    it('can get the default when the value is NOT present', function () {
      let event = {
        queryStringParameters: {
          somethingElse: 'foo'
        }
      }
      let result = objectUnderTest.getOptionalNumberParam(event, 'pageSize', 20)
      expect(result).toBe(20)
    })

    it('can get the default when no params are present', function () {
      let event = {
        queryStringParameters: null
      }
      let result = objectUnderTest.getOptionalNumberParam(event, 'pageSize', 20)
      expect(result).toBe(20)
    })

    it('should throw when we pass a string that cannot be parsed to a number', function () {
      let event = {
        queryStringParameters: {
          pageSize: 'blah'
        }
      }
      expect(() => {
        objectUnderTest.getOptionalNumberParam(event, 'pageSize', 20)
      }).toThrow()
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

  describe('getContentType', () => {
    it('should determine that we want CSV', () => {
      let event = {
        headers: {
          Accept: 'text/csv,application/xml;q=0.9,image/webp'
        }
      }
      let result = objectUnderTest.getContentType(event)
      expect(result).toBe(objectUnderTest.contentTypes.csv)
    })

    it('should determine that we want JSON', () => {
      let event = {
        headers: {
          Accept: 'application/json;q=0.9,image/webp'
        }
      }
      let result = objectUnderTest.getContentType(event)
      expect(result).toBe(objectUnderTest.contentTypes.json)
    })

    it('should determine that we cannot handle the requested type', () => {
      let event = {
        headers: {
          Accept: 'text/html;q=0.9,image/webp'
        }
      }
      let result = objectUnderTest.getContentType(event)
      expect(result).toBe(objectUnderTest.contentTypes.unhandled)
    })
  })

  describe('resolveVocabCode', () => {
    it('should be able to look up a code that exists', () => {
      let code = 'weight'
      let result = objectUnderTest.resolveVocabCode(code)
      expect(result).toBe('Weight')
    })

    it('should be able to fallback to the code as the label for a non-existant code', () => {
      let code = 'certainlyNotInTheVocab'
      let result = objectUnderTest.resolveVocabCode(code)
      expect(result).toBe(code)
    })
  })

  describe('calculatePageNumber', () => {
    it('should be able to tell when we are at the start of the first page with multiple pages', () => {
      let start = 0
      let numFound = 66
      let totalPages = 7
      let result = objectUnderTest.calculatePageNumber(start, numFound, totalPages)
      expect(result).toBe(1)
    })

    it('should be able to tell when we are at the start of the first page with one page', () => {
      let start = 0
      let numFound = 9
      let totalPages = 1
      let result = objectUnderTest.calculatePageNumber(start, numFound, totalPages)
      expect(result).toBe(1)
    })

    it('should be able to tell when we are at the start of the last page', () => {
      let start = 90
      let numFound = 99
      let totalPages = 10
      let result = objectUnderTest.calculatePageNumber(start, numFound, totalPages)
      expect(result).toBe(10)
    })

    it('should be able to tell when we are on a middle page', () => {
      let start = 50
      let numFound = 99
      let totalPages = 10
      let result = objectUnderTest.calculatePageNumber(start, numFound, totalPages)
      expect(result).toBe(6)
    })

    it('should be able to handle small page sizes', () => {
      let start = 2
      let numFound = 6
      let totalPages = 3
      let result = objectUnderTest.calculatePageNumber(start, numFound, totalPages)
      expect(result).toBe(2)
    })

    it('should throw when we supply a non-number start', () => {
      let start = '10'
      expect(() => {
        objectUnderTest.calculatePageNumber(start, 14, 2)
      }).toThrow()
    })

    it('should throw when we supply a non-number numFound', () => {
      let numFound = '10'
      expect(() => {
        objectUnderTest.calculatePageNumber(10, numFound, 2)
      }).toThrow()
    })

    it('should throw when we supply a non-number totalPages', () => {
      let totalPages = '10'
      expect(() => {
        objectUnderTest.calculatePageNumber(10, 14, totalPages)
      }).toThrow()
    })
  })

  describe('calculateTotalPages', () => {
    it('should calculate the total page when it rounds nicely and there is more than one page', () => {
      let rows = 10
      let numFound = 100
      let result = objectUnderTest.calculateTotalPages(rows, numFound)
      expect(result).toBe(10)
    })

    it('should calculate the total page when it rounds nicely and there only one page', () => {
      let rows = 10
      let numFound = 10
      let result = objectUnderTest.calculateTotalPages(rows, numFound)
      expect(result).toBe(1)
    })

    it('should calculate the total page when it does not round nicely and there is only one page', () => {
      let rows = 10
      let numFound = 6
      let result = objectUnderTest.calculateTotalPages(rows, numFound)
      expect(result).toBe(1)
    })

    it('should calculate the total page when it does not round nicely and there is more than one page', () => {
      let rows = 10
      let numFound = 101
      let result = objectUnderTest.calculateTotalPages(rows, numFound)
      expect(result).toBe(11)
    })
  })

  describe('assertIsSupplied', () => {
    it('should do nothing when the value is supplied', () => {
      objectUnderTest.assertIsSupplied('species one')
    })

    it('should throw when the value is NOT supplied', () => {
      expect(() => {
        objectUnderTest.assertIsSupplied(null)
      }).toThrow()
    })
  })

  describe('newContentNegotiationHandler', () => {
    function TrackCallsModule () {
      let isCalled = false

      return {
        handler: () => {
          isCalled = true
        },
        hasBeenCalled: () => {
          return isCalled
        }
      }
    }
    it('should call the JSON handler when we have a JSON Accept header', () => {
      let trackerModule = new TrackCallsModule()
      let cnHandler = objectUnderTest.newContentNegotiationHandler(trackerModule, () => {})
      let event = {
        headers: {
          Accept: 'application/json'
        }
      }
      cnHandler(event, null, null)
      expect(trackerModule.hasBeenCalled()).toBeTruthy()
    })

    it('should call the CSV handler when we have a CSV Accept header', () => {
      let trackerModule = new TrackCallsModule()
      let cnHandler = objectUnderTest.newContentNegotiationHandler(() => {}, trackerModule)
      let event = {
        headers: {
          Accept: 'text/csv'
        }
      }
      cnHandler(event, null, null)
      expect(trackerModule.hasBeenCalled()).toBeTruthy()
    })

    it('should return a 400 when it cannot handle the Accept header', (done) => {
      let trackerModule = new TrackCallsModule()
      let cnHandler = objectUnderTest.newContentNegotiationHandler(() => {}, trackerModule)
      let event = {
        headers: {
          Accept: 'something/weird'
        }
      }
      cnHandler(event, null, (_, result) => {
        expect(result.statusCode).toBe(400)
        done()
      })
    })
  })
})

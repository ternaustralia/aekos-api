'use strict'

let objectUnderTest = require('../response-helper')

describe('response-helper', function () {
  describe('json', function () {
    describe('.ok()', function () {
      it('should send the supplied body to the callback', function () {
        let response = null
        let callback = function (_, result) {
          response = result
        }
        objectUnderTest.json.ok(callback, 'some value')
        expect(response.body).toBe('"some value"')
      })

      it('should return a 200 status code', function () {
        let response = null
        let callback = function (_, result) {
          response = result
        }
        objectUnderTest.json.ok(callback, 'some value')
        expect(response.statusCode).toBe(200)
      })

      it('should send the expected headers', function () {
        let response = null
        let callback = function (_, result) {
          response = result
        }
        objectUnderTest.json.ok(callback, 'some value')
        expect(response.headers).toEqual({
          'Access-Control-Allow-Origin': '*',
          'Access-Control-Allow-Credentials': true,
          'Content-Type': "'application/json'"
        })
      })

      it('should add hateoas headers when appropriate', function () {
        let response = null
        let callback = function (_, result) {
          response = result
        }
        let params = {
          rows: 15,
          start: 0
        }
        let hateoasableResponse = {
          responseHeader: {
            pageNumber: 1,
            params: params,
            totalPages: 3
          }
        }
        let event = {
          headers: {
            Host: 'api.aekos.org.au',
            'X-Forwarded-Proto': 'https'
          },
          requestContext: {
            path: '/v1/someResource.json'
          },
          queryStringParameters: params
        }
        objectUnderTest.json.ok(callback, hateoasableResponse, event)
        expect(response.headers).toEqual({
          'Access-Control-Allow-Origin': '*',
          'Access-Control-Allow-Credentials': true,
          'Content-Type': "'application/json'",
          link: '<https://api.aekos.org.au/v1/someResource.json?rows=15&start=15>; rel="next", ' +
          '<https://api.aekos.org.au/v1/someResource.json?rows=15&start=30>; rel="last"'
        })
      })
    })

    describe('.badRequest()', function () {
      it('should send the supplied body to the callback', function () {
        let response = null
        let callback = function (_, result) {
          response = result
        }
        objectUnderTest.json.badRequest(callback, 'wrong input')
        expect(response.body).toBe('"wrong input"')
      })

      it('should return a 400 status code', function () {
        let response = null
        let callback = function (_, result) {
          response = result
        }
        objectUnderTest.json.badRequest(callback, 'wrong input')
        expect(response.statusCode).toBe(400)
      })

      it('should send the expected headers', function () {
        let response = null
        let callback = function (_, result) {
          response = result
        }
        objectUnderTest.json.badRequest(callback, 'wrong input')
        expect(response.headers).toEqual({
          'Access-Control-Allow-Origin': '*',
          'Access-Control-Allow-Credentials': true,
          'Content-Type': "'application/json'"
        })
      })
    })

    describe('.internalServerError()', function () {
      it('should send the supplied body to the callback', function () {
        let response = null
        let callback = function (_, result) {
          response = result
        }
        objectUnderTest.json.internalServerError(callback, 'something failed')
        expect(response.body).toBe(JSON.stringify({ message: 'something failed' }))
      })

      it('should return a 500 status code', function () {
        let response = null
        let callback = function (_, result) {
          response = result
        }
        objectUnderTest.json.internalServerError(callback, 'something failed')
        expect(response.statusCode).toBe(500)
      })

      it('should send the expected headers', function () {
        let response = null
        let callback = function (_, result) {
          response = result
        }
        objectUnderTest.json.internalServerError(callback, 'something failed')
        expect(response.headers).toEqual({
          'Access-Control-Allow-Origin': '*',
          'Access-Control-Allow-Credentials': true,
          'Content-Type': "'application/json'"
        })
      })
    })
  })

  describe('csv', function () {
    describe('ok', function () {
      it('should send the supplied body to the callback without JSON.stringify-ing it', function () {
        let suppliedArg = null
        let callback = function (_, result) {
          suppliedArg = result
        }
        objectUnderTest.csv.ok(callback, '1,"some value",123\n2,"other value",456')
        expect(suppliedArg.body).toBe('1,"some value",123\n2,"other value",456')
      })
    })
  })

  describe('.buildHateoasLinkHeader()', function () {
    const reqHeaders = {
      Host: 'api.aekos.org.au',
      'X-Forwarded-Proto': 'https'
    }
    it('should build the HATEOAS header for the first page of a single-page response', function () {
      let event = {
        headers: reqHeaders,
        queryStringParameters: {
          rows: '20',
          download: 'false',
          start: '0'
        },
        requestContext: {
          path: '/v1/speciesData.csv'
        }
      }
      let responseHeader = {
        pageNumber: 1,
        totalPages: 1,
        params: {
          rows: 20,
          start: 0
        }
      }
      let result = objectUnderTest.buildHateoasLinkHeader(event, responseHeader)
      expect(result).toBe('')
    })

    it('should build the HATEOAS header for the first page of a multi-page response', function () {
      let event = {
        headers: reqHeaders,
        queryStringParameters: {
          rows: '20',
          download: 'false',
          start: '0'
        },
        requestContext: {
          path: '/v1/speciesData.csv'
        }
      }
      let responseHeader = {
        pageNumber: 1,
        totalPages: 10,
        params: {
          rows: 20,
          start: 0
        }
      }
      let result = objectUnderTest.buildHateoasLinkHeader(event, responseHeader)
      expect(result).toBe(
        '<https://api.aekos.org.au/v1/speciesData.csv?rows=20&download=false&start=20>; rel="next", ' +
        '<https://api.aekos.org.au/v1/speciesData.csv?rows=20&download=false&start=180>; rel="last"')
    })

    it('should build the HATEOAS header for the middle page of a multi-page (>2) response', function () {
      let event = {
        headers: reqHeaders,
        queryStringParameters: {
          rows: '20',
          download: 'false',
          start: '0'
        },
        requestContext: {
          path: '/v1/speciesData.csv'
        }
      }
      let responseHeader = {
        pageNumber: 2,
        totalPages: 10,
        params: {
          rows: 20,
          start: 20
        }
      }
      let result = objectUnderTest.buildHateoasLinkHeader(event, responseHeader)
      expect(result).toBe(
        '<https://api.aekos.org.au/v1/speciesData.csv?rows=20&download=false&start=40>; rel="next", ' +
        '<https://api.aekos.org.au/v1/speciesData.csv?rows=20&download=false&start=0>; rel="prev", ' +
        '<https://api.aekos.org.au/v1/speciesData.csv?rows=20&download=false&start=0>; rel="first", ' +
        '<https://api.aekos.org.au/v1/speciesData.csv?rows=20&download=false&start=180>; rel="last"')
    })

    it('should build the HATEOAS header for the last page of a response', function () {
      let event = {
        headers: reqHeaders,
        queryStringParameters: {
          rows: '20',
          download: 'false',
          start: '0'
        },
        requestContext: {
          path: '/v1/speciesData.csv'
        }
      }
      let responseHeader = {
        pageNumber: 3,
        totalPages: 3,
        params: {
          rows: 20,
          start: 40
        }
      }
      let result = objectUnderTest.buildHateoasLinkHeader(event, responseHeader)
      expect(result).toBe(
        '<https://api.aekos.org.au/v1/speciesData.csv?rows=20&download=false&start=20>; rel="prev", ' +
        '<https://api.aekos.org.au/v1/speciesData.csv?rows=20&download=false&start=0>; rel="first"')
    })
  })

  describe('.isHateoasable()', () => {
    it('should know when we can apply Hateoas headers because all fields are present', () => {
      let response = {
        responseHeader: {
          pageNumber: 1,
          params: {
            rows: 15,
            start: 0
          },
          totalPages: 3
        }
      }
      let result = objectUnderTest.isHateoasable(response)
      expect(result).toBe(true)
    })

    it('should know when we CANNOT apply Hateoas headers because pageNumber is missing', () => {
      let response = {
        responseHeader: {
          // no pageNumber
          params: {
            rows: 15,
            start: 0
          },
          totalPages: 3
        }
      }
      let result = objectUnderTest.isHateoasable(response)
      expect(result).toBe(false)
    })

    it('should know when we CANNOT apply Hateoas headers because rows is missing', () => {
      let response = {
        responseHeader: {
          pageNumber: 1,
          params: {
            // no rows
            start: 0
          },
          totalPages: 3
        }
      }
      let result = objectUnderTest.isHateoasable(response)
      expect(result).toBe(false)
    })

    it('should know when we CANNOT apply Hateoas headers because start is missing', () => {
      let response = {
        responseHeader: {
          pageNumber: 1,
          params: {
            rows: 15
            // no start
          },
          totalPages: 3
        }
      }
      let result = objectUnderTest.isHateoasable(response)
      expect(result).toBe(false)
    })

    it('should know when we CANNOT apply Hateoas headers because totalPages is missing', () => {
      let response = {
        responseHeader: {
          pageNumber: 1,
          params: {
            rows: 15,
            start: 0
          }
          // no totalPages
        }
      }
      let result = objectUnderTest.isHateoasable(response)
      expect(result).toBe(false)
    })

    it('should know when we CANNOT apply Hateoas headers because the response is not an object', () => {
      let response = 'not an object'
      let result = objectUnderTest.isHateoasable(response)
      expect(result).toBe(false)
    })

    it('should know when we CANNOT apply Hateoas headers because the response has a header but it is not an object', () => {
      let response = {
        responseHeader: 'not an object'
      }
      let result = objectUnderTest.isHateoasable(response)
      expect(result).toBe(false)
    })
  })

  describe('.handlePost()', () => {
    const alwaysValidValidator = (body) => { return { isValid: true } }
    const jsonAndCorsHeaders = {
      'Access-Control-Allow-Origin': '*',
      'Access-Control-Allow-Credentials': true,
      'Content-Type': "'application/json'"
    }

    it('should return a 400 when validation fails', (done) => {
      let callback = (_, result) => {
        expect(result.statusCode).toBe(400)
        expect(result.headers).toEqual(jsonAndCorsHeaders)
        done()
      }
      let validator = (body) => {
        return { isValid: false, message: 'fail town' }
      }
      objectUnderTest.handlePost({ body: '{}' }, callback, null, validator,
        function () { fail('Should not have been called') })
    })

    it('should call the responder when validation succeeds', (done) => {
      let isResponderCalled = false
      let callback = (_, result) => {
        expect(isResponderCalled).toBeTruthy()
        done()
      }
      let responder = (requestBody, db) => {
        isResponderCalled = true
        return new Promise(resolve => {
          resolve()
        })
      }
      objectUnderTest.handlePost({ body: '{}' }, callback, null, alwaysValidValidator, responder)
    })

    it('should return a 500 when the responder throws as error (outside a promise)', (done) => {
      let callback = (_, result) => {
        resetConsoleError()
        expect(result.statusCode).toBe(500)
        done()
      }
      let responder = (requestBody, db) => {
        throw new Error('ka-boom')
      }
      silenceConsoleError()
      objectUnderTest.handlePost({ body: '{}' }, callback, null, alwaysValidValidator, responder)
    })

    it('should return a 500 when the responder throws as error (inside a promise)', (done) => {
      let callback = (_, result) => {
        resetConsoleError()
        expect(result.statusCode).toBe(500)
        done()
      }
      let responder = (requestBody, db) => {
        return new Promise(() => {
          throw new Error('ka-boom')
        })
      }
      silenceConsoleError()
      objectUnderTest.handlePost({ body: '{}' }, callback, null, alwaysValidValidator, responder)
    })

    it('should return a 500 when the responder rejects the promise', (done) => {
      let callback = (_, result) => {
        resetConsoleError()
        expect(result.statusCode).toBe(500)
        expect(result.headers).toEqual(jsonAndCorsHeaders)
        done()
      }
      let responder = (requestBody, db) => {
        return new Promise((resolve, reject) => {
          reject(new Error('ka-boom'))
        })
      }
      silenceConsoleError()
      objectUnderTest.handlePost({ body: '{}' }, callback, null, alwaysValidValidator, responder)
    })

    it('should return the response body when all is successful', (done) => {
      let callback = (_, result) => {
        expect(result.statusCode).toBe(200)
        expect(result.headers).toEqual(jsonAndCorsHeaders)
        expect(result.body).toBe(JSON.stringify({ someField: 123 }))
        done()
      }
      let responder = (requestBody, db) => {
        return new Promise((resolve, reject) => {
          resolve({ someField: 123 })
        })
      }
      objectUnderTest.handlePost({ body: '{}' }, callback, null, alwaysValidValidator, responder)
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
      silenceConsoleWarn()
      let result = objectUnderTest.resolveVocabCode(code)
      resetConsoleWarn()
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
      let cnHandler = objectUnderTest.newContentNegotiationHandler(trackerModule, () => { })
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
      let cnHandler = objectUnderTest.newContentNegotiationHandler(() => { }, trackerModule)
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
      let cnHandler = objectUnderTest.newContentNegotiationHandler(() => { }, trackerModule)
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

let origConsoleError = null
function silenceConsoleError () {
  origConsoleError = console.error
  console.error = () => { }
}
function resetConsoleError () {
  console.error = origConsoleError
}
let origConsoleWarn = null
function silenceConsoleWarn () {
  origConsoleWarn = console.warn
  console.warn = () => { }
}
function resetConsoleWarn () {
  console.warn = origConsoleWarn
}

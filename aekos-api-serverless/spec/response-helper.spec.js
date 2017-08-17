'use strict'
let ConsoleSilencer = require('./ConsoleSilencer')
let consoleSilencer = new ConsoleSilencer()
let StubDB = require('./StubDB')

describe('response-helper', function () {
  let objectUnderTest = require('../response-helper')

  const alwaysValidValidator = () => { return { isValid: true } }
  const jsonAndCorsHeaders = {
    'Access-Control-Allow-Origin': '*',
    'Access-Control-Allow-Credentials': true,
    'Access-Control-Expose-Headers': 'link',
    'Content-Type': "'application/json'"
  }
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
          'Access-Control-Expose-Headers': 'link',
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
          'Access-Control-Expose-Headers': 'link',
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
        expect(JSON.parse(response.body)).toEqual({
          message: 'wrong input',
          statusCode: 400
        })
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
          'Access-Control-Expose-Headers': 'link',
          'Content-Type': "'application/json'"
        })
      })
    })

    describe('.internalServerError()', function () {
      const msgFor500 = 'Sorry about that, something has gone wrong'
      it('should send the supplied body to the callback', function () {
        let response = null
        let callback = function (_, result) {
          response = result
        }
        objectUnderTest.json.internalServerError(callback)
        expect(JSON.parse(response.body)).toEqual({
          message: msgFor500,
          statusCode: 500
        })
      })

      it('should return a 500 status code', function () {
        let response = null
        let callback = function (_, result) {
          response = result
        }
        objectUnderTest.json.internalServerError(callback)
        expect(response.statusCode).toBe(500)
      })

      it('should send the expected headers', function () {
        let response = null
        let callback = function (_, result) {
          response = result
        }
        objectUnderTest.json.internalServerError(callback)
        expect(response.headers).toEqual({
          'Access-Control-Allow-Origin': '*',
          'Access-Control-Allow-Credentials': true,
          'Access-Control-Expose-Headers': 'link',
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

      it('should respond with the content-disposition header when we supply a filename', function () {
        let suppliedArg = null
        let callback = function (_, result) {
          suppliedArg = result
        }
        let downloadFileName = 'someDownloadFile.csv'
        objectUnderTest.csv.ok(callback, '1,"some value",123\n2,"other value",456', downloadFileName)
        expect(suppliedArg.headers['Content-Disposition']).toBe('attachment;filename=someDownloadFile.csv')
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

    it('should build the HATEOAS header when no parameters were passed', function () {
      let event = {
        headers: reqHeaders,
        queryStringParameters: null,
        requestContext: {
          path: '/v1/speciesData.csv'
        }
      }
      let responseHeader = {
        pageNumber: 1,
        totalPages: 3,
        params: {
          rows: 20,
          start: 0
        }
      }
      let result = objectUnderTest.buildHateoasLinkHeader(event, responseHeader)
      expect(result).toBe(
        '<https://api.aekos.org.au/v1/speciesData.csv?start=20>; rel="next", ' +
        '<https://api.aekos.org.au/v1/speciesData.csv?start=40>; rel="last"')
    })

    it('should give an informative error when the event was not supplied', function () {
      let event // undefined!
      let responseHeader = {
        pageNumber: 1,
        totalPages: 1,
        params: {
          rows: 20,
          start: 0
        }
      }
      try {
        objectUnderTest.buildHateoasLinkHeader(event, responseHeader)
        fail()
      } catch (error) {
        expect(error.message).toBe('Programmer problem: event was not supplied')
        // success
      }
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

  describe('.handleJsonPost()', () => {
    describe('', () => {
      let result = null
      beforeEach(done => {
        let callback = (_, theResult) => {
          result = theResult
          done()
        }
        let validator = () => {
          return { isValid: false, message: 'fail town' }
        }
        objectUnderTest.handleJsonPost({ body: '{}' }, callback, null, validator,
          function () { fail('Should not have been called') })
      })

      it('should return a 400 when validation fails', () => {
        expect(result.statusCode).toBe(400)
        expect(result.headers).toEqual(jsonAndCorsHeaders)
      })
    })

    describe('', () => {
      let result = null
      beforeEach(done => {
        let callback = (_, theResult) => {
          result = theResult
          done()
        }
        let validator = () => {
          return { isValid: false, message: 'fail town' }
        }
        objectUnderTest.handleJsonPost({ /* no body */ }, callback, null, validator,
          function () { fail('Should not have been called') })
      })

      it('should return a 400 when no body is supplied. AWS provides body: null but this will help debug tests', () => {
        expect(result.statusCode).toBe(400)
        expect(result.headers).toEqual(jsonAndCorsHeaders)
      })
    })

    it('should pass the requestBody to the validator', () => {
      let passedRequestBody = null
      let validator = (_, body) => {
        passedRequestBody = body
        return { isValid: false, message: 'might as well stop here' }
      }
      objectUnderTest.handleJsonPost({ body: '{"foo":123}' }, () => {}, null, validator,
        function () { fail('Should not have been called') })
      expect(passedRequestBody).toEqual({foo: 123})
    })

    it('should pass the query string to the validator', () => {
      let passedQueryString = null
      let validator = (queryString, _) => {
        passedQueryString = queryString
        return { isValid: false, message: 'might as well stop here' }
      }
      objectUnderTest.handleJsonPost({ body: '{}', queryStringParameters: {foo: 123} }, () => {},
        null, validator, function () { fail('Should not have been called') })
      expect(passedQueryString).toEqual({foo: 123})
    })

    it('should pass the requestBody to the responder', () => {
      let passedRequestBody = null
      let responder = (body /* ignore other params */) => {
        passedRequestBody = body
        return new Promise((resolve, reject) => { resolve() })
      }
      objectUnderTest.handleJsonPost({ body: '{"foo":123}' }, () => {}, null,
        () => { return { isValid: true } }, responder)
      expect(passedRequestBody).toEqual({foo: 123})
    })

    it('should pass the query string to the responder', () => {
      let passedQueryString = null
      let responder = (body, db, queryStringObj) => {
        passedQueryString = queryStringObj
        return new Promise((resolve, reject) => { resolve() })
      }
      objectUnderTest.handleJsonPost({ body: '{}', queryStringParameters: {foo: 123} }, () => {}, null,
        () => { return { isValid: true } }, responder)
      expect(passedQueryString).toEqual({foo: 123})
    })

    describe('', () => {
      let isResponderCalled = false
      beforeEach(done => {
        let callback = (_, theResult) => {
          done()
        }
        let responder = (requestBody, db) => {
          isResponderCalled = true
          return new Promise(resolve => {
            resolve()
          })
        }
        objectUnderTest.handleJsonPost({ body: '{}' }, callback, null, alwaysValidValidator, responder)
      })

      it('should call the responder when validation succeeds', () => {
        expect(isResponderCalled).toBeTruthy()
      })
    })

    describe('', () => {
      let result = null
      beforeEach(done => {
        let callback = (_, theResult) => {
          consoleSilencer.resetConsoleError()
          result = theResult
          done()
        }
        let responder = (requestBody, db) => {
          throw new Error('ka-boom')
        }
        consoleSilencer.silenceConsoleError()
        objectUnderTest.handleJsonPost({ body: '{}' }, callback, null, alwaysValidValidator, responder)
      })

      it('should return a 500 when the responder throws an error (outside a promise)', () => {
        expect(result.statusCode).toBe(500)
      })
    })

    describe('', () => {
      let result = null
      beforeEach(done => {
        let callback = (_, theResult) => {
          consoleSilencer.resetConsoleError()
          result = theResult
          done()
        }
        let responder = (requestBody, db) => {
          return new Promise(() => {
            throw new Error('ka-boom')
          })
        }
        consoleSilencer.silenceConsoleError()
        objectUnderTest.handleJsonPost({ body: '{}' }, callback, null, alwaysValidValidator, responder)
      })

      it('should return a 500 when the responder throws an error (inside a promise)', () => {
        expect(result.statusCode).toBe(500)
      })
    })

    describe('', () => {
      let result = null
      beforeEach(done => {
        let callback = (_, theResult) => {
          consoleSilencer.resetConsoleError()
          result = theResult
          done()
        }
        let responder = (requestBody, db) => {
          return new Promise((resolve, reject) => {
            reject(new Error('ka-boom'))
          })
        }
        consoleSilencer.silenceConsoleError()
        objectUnderTest.handleJsonPost({ body: '{}' }, callback, null, alwaysValidValidator, responder)
      })

      it('should return a 500 when the responder rejects the promise', () => {
        expect(result.statusCode).toBe(500)
        expect(result.headers).toEqual(jsonAndCorsHeaders)
      })
    })

    describe('', () => {
      let result = null
      beforeEach(done => {
        let callback = (_, theResult) => {
          result = theResult
          done()
        }
        let responder = (requestBody, db) => {
          return new Promise((resolve, reject) => {
            resolve({ someField: 123 })
          })
        }
        objectUnderTest.handleJsonPost({ body: '{}' }, callback, null, alwaysValidValidator, responder)
      })

      it('should return the response body when all is successful', () => {
        expect(result.statusCode).toBe(200)
        expect(result.headers).toEqual(jsonAndCorsHeaders)
        expect(result.body).toBe(JSON.stringify({ someField: 123 }))
      })
    })
  })

  describe('.handleJsonGet()', () => {
    describe('', () => {
      let result = null
      beforeEach(done => {
        let callback = (_, theResult) => {
          result = theResult
          done()
        }
        let responder = (requestBody, db) => {
          return new Promise((resolve, reject) => {
            resolve({ someField: 123 })
          })
        }
        objectUnderTest.handleJsonGet({}, callback, null, alwaysValidValidator, responder)
      })

      it('should return the response body when all is successful', () => {
        expect(result.statusCode).toBe(200)
        expect(result.headers).toEqual(jsonAndCorsHeaders)
        expect(result.body).toBe(JSON.stringify({ someField: 123 }))
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
    it('should pass the query string object to the inner function', function () {
      let event = {
        queryStringParameters: {
          pageSize: 10
        }
      }
      let result = objectUnderTest.getOptionalNumberParam(event, 'pageSize', 20)
      expect(result).toBe(10)
    })
  })

  describe('getOptionalNumber', () => {
    it('can get the value when it is present as a number', () => {
      let containerObj = {
        pageSize: 10
      }
      let result = objectUnderTest.getOptionalNumber(containerObj, 'pageSize', 20)
      expect(result).toBe(10)
    })

    it('can get the value when it is present as a string', () => {
      let containerObj = {
        pageSize: '42'
      }
      let result = objectUnderTest.getOptionalNumber(containerObj, 'pageSize', 20)
      expect(result).toBe(42)
    })

    it('can get the default when the value is NOT present', () => {
      let containerObj = {
        somethingElse: 'foo'
      }
      let result = objectUnderTest.getOptionalNumber(containerObj, 'pageSize', 20)
      expect(result).toBe(20)
    })

    it('can get the default when no params are present', () => {
      let containerObj = null
      let result = objectUnderTest.getOptionalNumber(containerObj, 'pageSize', 20)
      expect(result).toBe(20)
    })

    it('can get the default when the default is a numeric string', () => {
      let containerObj = null
      let result = objectUnderTest.getOptionalNumber(containerObj, 'pageSize', '20')
      expect(result).toBe(20)
    })

    it('should throw when we pass a string that cannot be parsed to a number', () => {
      let containerObj = {
        pageSize: 'blah'
      }
      expect(() => {
        objectUnderTest.getOptionalNumber(containerObj, 'pageSize', 20)
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
      consoleSilencer.silenceConsoleWarn()
      let result = objectUnderTest.resolveVocabCode(code)
      consoleSilencer.resetConsoleWarn()
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

  describe('.speciesNamesValidator()', () => {
    it('should validate with one species', () => {
      let requestBody = {
        speciesNames: ['species one']
      }
      let result = objectUnderTest.speciesNamesValidator(null, requestBody)
      expect(result.isValid).toBe(true)
    })

    it('should be invalid with no species', () => {
      let requestBody = {
        speciesNames: []
      }
      let result = objectUnderTest.speciesNamesValidator(null, requestBody)
      expect(result.isValid).toBe(false)
    })
  })

  describe('.traitNamesMandatoryValidator()', () => {
    it('should validate with one trait name', () => {
      let requestBody = {
        traitNames: ['trait one']
      }
      let result = objectUnderTest.traitNamesMandatoryValidator(null, requestBody)
      expect(result.isValid).toBe(true)
    })

    it('should be invalid with no traits', () => {
      let requestBody = {
        traitNames: []
      }
      let result = objectUnderTest.traitNamesMandatoryValidator(null, requestBody)
      expect(result.isValid).toBe(false)
    })
  })

  describe('.traitNamesOptionalValidator()', () => {
    it('should validate with one trait name', () => {
      let requestBody = {
        traitNames: ['trait one']
      }
      let result = objectUnderTest.traitNamesOptionalValidator(null, requestBody)
      expect(result.isValid).toBe(true)
    })

    it('should be valid with no traits', () => {
      let requestBody = {
        traitNames: []
      }
      let result = objectUnderTest.traitNamesOptionalValidator(null, requestBody)
      expect(result.isValid).toBe(true)
    })

    it('should be valid with no body', () => {
      let requestBody = null
      let result = objectUnderTest.traitNamesOptionalValidator(null, requestBody)
      expect(result.isValid).toBe(true)
    })
  })

  describe('.envVarNamesOptionalValidator()', () => {
    it('should validate with one var name', () => {
      let requestBody = {
        varNames: ['var one']
      }
      let result = objectUnderTest.envVarNamesOptionalValidator(null, requestBody)
      expect(result.isValid).toBe(true)
    })

    it('should be valid with no vars', () => {
      let requestBody = {
        varNames: []
      }
      let result = objectUnderTest.envVarNamesOptionalValidator(null, requestBody)
      expect(result.isValid).toBe(true)
    })

    it('should be valid with no body', () => {
      let requestBody = null
      let result = objectUnderTest.envVarNamesOptionalValidator(null, requestBody)
      expect(result.isValid).toBe(true)
    })
  })

  describe('.genericOptionalNamesValidator()', () => {
    it('should validate with no body', () => {
      let requestBody = null
      let result = objectUnderTest._testonly.genericOptionalNamesValidator('targetNames', null, requestBody)
      expect(result.isValid).toBe(true)
    })

    it('should validate when the target property is not present', () => {
      let requestBody = {
        somethingElse: 'blah'
      }
      let result = objectUnderTest._testonly.genericOptionalNamesValidator('targetNames', null, requestBody)
      expect(result.isValid).toBe(true)
    })

    it('should validate when the target property is present with no items', () => {
      let requestBody = {
        targetNames: []
      }
      let result = objectUnderTest._testonly.genericOptionalNamesValidator('targetNames', null, requestBody)
      expect(result.isValid).toBe(true)
    })

    it('should validate when the target property is present and has valid items', () => {
      let requestBody = {
        targetNames: ['name one']
      }
      let result = objectUnderTest._testonly.genericOptionalNamesValidator('targetNames', null, requestBody)
      expect(result.isValid).toBe(true)
    })

    it('should catch when the target property is present and has invalid items', () => {
      let requestBody = {
        targetNames: ['name one', 2]
      }
      let result = objectUnderTest._testonly.genericOptionalNamesValidator('targetNames', null, requestBody)
      expect(result.isValid).toBe(false)
    })

    it('should catch when the target property is present and is the wrong type', () => {
      let requestBody = {
        targetNames: 'not an array'
      }
      let result = objectUnderTest._testonly.genericOptionalNamesValidator('targetNames', null, requestBody)
      expect(result.isValid).toBe(false)
    })
  })

  describe('.genericMandatoryNamesValidator()', () => {
    it('should validate with one name', () => {
      let requestBody = {
        targetNames: ['thingy one']
      }
      let result = objectUnderTest._testonly.genericMandatoryNamesValidator('targetNames', null, requestBody)
      expect(result.isValid).toBe(true)
    })

    it('should be invalid with no names', () => {
      let requestBody = {
        targetNames: []
      }
      let result = objectUnderTest._testonly.genericMandatoryNamesValidator('targetNames', null, requestBody)
      expect(result.isValid).toBe(false)
    })

    it('should be invalid when the body is not an object', () => {
      let requestBody = 'a string'
      let result = objectUnderTest._testonly.genericMandatoryNamesValidator('targetNames', null, requestBody)
      expect(result.isValid).toBe(false)
    })

    it('should be invalid when the targetNames field is not present', () => {
      let requestBody = {
        someOtherField: 123
      }
      let result = objectUnderTest._testonly.genericMandatoryNamesValidator('targetNames', null, requestBody)
      expect(result.isValid).toBe(false)
    })

    it('should be invalid when the targetNames field is the wrong type', () => {
      let requestBody = {
        targetNames: 'not an array'
      }
      let result = objectUnderTest._testonly.genericMandatoryNamesValidator('targetNames', null, requestBody)
      expect(result.isValid).toBe(false)
    })

    it('should be invalid when the targetNames field is an array containing the wrong type elements', () => {
      let requestBody = {
        targetNames: [1, 2, 3]
      }
      let result = objectUnderTest._testonly.genericMandatoryNamesValidator('targetNames', null, requestBody)
      expect(result.isValid).toBe(false)
    })

    it('should be invalid when no body is present (null)', () => {
      let requestBody = null
      let result = objectUnderTest._testonly.genericMandatoryNamesValidator('targetNames', null, requestBody)
      expect(result.isValid).toBe(false)
    })

    it('should be invalid when no body is present (undefined)', () => {
      let event = {}
      let result = objectUnderTest._testonly.genericMandatoryNamesValidator('targetNames', null, event.bodyWillBeUndefined)
      expect(result.isValid).toBe(false)
    })
  })

  describe('.getUrlSuffix()', () => {
    it('should get the JSON suffix when present', () => {
      let event = {
        path: '/some/path.json'
      }
      let result = objectUnderTest._testonly.getUrlSuffix(event)
      expect(result).toBe('json')
    })

    it('should get the CSV suffix when present', () => {
      let event = {
        path: '/some/path.csv'
      }
      let result = objectUnderTest._testonly.getUrlSuffix(event)
      expect(result).toBe('csv')
    })

    it('should return null when no suffix is present', () => {
      let event = {
        path: '/some/path'
      }
      let result = objectUnderTest._testonly.getUrlSuffix(event)
      expect(result).toBeNull()
    })
  })

  describe('.getOptionalArray()', () => {
    it('should get the array when present', () => {
      let requestBody = {
        speciesNames: ['species one']
      }
      let result = objectUnderTest.getOptionalArray(requestBody, 'speciesNames', new StubDB())
      expect(result).toEqual({
        escaped: "'species one'",
        unescaped: ['species one']
      })
    })

    it('should return the expected result when no array is present', () => {
      let requestBody = { /* empty */ }
      let result = objectUnderTest.getOptionalArray(requestBody, 'speciesNames', new StubDB())
      expect(result).toEqual({
        escaped: null,
        unescaped: null
      })
    })

    it('should throw the expected error when the value is present but not an array', () => {
      let requestBody = {
        speciesNames: 'not an array'
      }
      try {
        objectUnderTest.getOptionalArray(requestBody, 'speciesNames', new StubDB())
        fail()
      } catch (error) {
        // success!
        expect(error.message).toBe("Programmer problem: the 'speciesNames' field (value='not an " +
        "array') is not an array, this should've been caught by validation")
      }
    })
  })
})

describe('response-helper', () => {
  let responseHelper = require('../response-helper')
  const successfulValidator = () => {
    return { isValid: true }
  }
  const unsuccessfulValidator = () => {
    return { isValid: false, message: 'I always fail :(' }
  }
  describe('.compositeValidator()', () => {
    it('should be able to run with a single validator', () => {
      let objectUnderTest = responseHelper.compositeValidator([successfulValidator])
      let result = objectUnderTest(null, null)
      expect(result).toEqual({ isValid: true })
    })

    it('should validate with two successful validators', () => {
      let objectUnderTest = responseHelper.compositeValidator([successfulValidator, successfulValidator])
      let result = objectUnderTest(null, null)
      expect(result).toEqual({ isValid: true })
    })

    it('should fail when the first validator fails', () => {
      let isSecondValidatorCalled = false
      let objectUnderTest = responseHelper.compositeValidator([unsuccessfulValidator, () => { isSecondValidatorCalled = true }])
      let result = objectUnderTest(null, null)
      expect(result).toEqual({ isValid: false, message: 'I always fail :(' })
      expect(isSecondValidatorCalled).toBe(false)
    })

    it('should fail when the second validator fails', () => {
      let objectUnderTest = responseHelper.compositeValidator([successfulValidator, unsuccessfulValidator])
      let result = objectUnderTest(null, null)
      expect(result).toEqual({ isValid: false, message: 'I always fail :(' })
    })
  })

  describe('.queryStringParamIsBooleanIfPresentValidator()', () => {
    let objectUnderTest = responseHelper._testonly.queryStringParamIsBooleanIfPresentValidator('param1')
    it('should pass validation when the param is NOT present', () => {
      let queryStringObject = {}
      let result = objectUnderTest(queryStringObject, null)
      expect(result).toEqual({ isValid: true })
    })

    it('should pass validation when the param is present and true', () => {
      let queryStringObject = { param1: 'true' }
      let result = objectUnderTest(queryStringObject, null)
      expect(result).toEqual({ isValid: true })
    })

    it('should pass validation when the param is present and false', () => {
      let queryStringObject = { param1: 'false' }
      let result = objectUnderTest(queryStringObject, null)
      expect(result).toEqual({ isValid: true })
    })

    it('should pass validation when the param is present and not lowercase true', () => {
      let queryStringObject = { param1: 'TRuE' }
      let result = objectUnderTest(queryStringObject, null)
      expect(result).toEqual({ isValid: true })
    })

    it('should fail validation when the param is present and not a string', () => {
      let queryStringObject = { param1: 123 }
      let result = objectUnderTest(queryStringObject, null)
      expect(result).toEqual({
        isValid: false, message: "The 'param1' must be a stringified boolean when supplied. Supplied value = '123'" })
    })

    it('should fail validation when the param is present and not a valid value', () => {
      let queryStringObject = { param1: 'blah' }
      let result = objectUnderTest(queryStringObject, null)
      expect(result).toEqual({
        isValid: false, message: "The 'param1' must be a stringified boolean when supplied. Supplied value = 'blah'. Acceptable values are (case insensitive) 'true' or 'false'."
      })
    })
  })

  describe('.queryStringParamIsPositiveNumberIfPresentValidator()', () => {
    it('should pass validation when the param is NOT present', () => {
      let objectUnderTest = responseHelper._testonly.queryStringParamIsPositiveNumberIfPresentValidator('param1')
      let queryStringObject = {}
      let result = objectUnderTest(queryStringObject, null)
      expect(result).toEqual({ isValid: true })
    })

    it('should pass validation when no params are present (null)', () => {
      let objectUnderTest = responseHelper._testonly.queryStringParamIsPositiveNumberIfPresentValidator('param1')
      let queryStringObject = null
      let result = objectUnderTest(queryStringObject, null)
      expect(result).toEqual({ isValid: true })
    })

    it('should pass validation when no params are present (undefined)', () => {
      let objectUnderTest = responseHelper._testonly.queryStringParamIsPositiveNumberIfPresentValidator('param1')
      let event = {}
      let result = objectUnderTest(event.willBeUndefined, null)
      expect(result).toEqual({ isValid: true })
    })

    it('should pass validation when the param is present and a number', () => {
      let objectUnderTest = responseHelper._testonly.queryStringParamIsPositiveNumberIfPresentValidator('param1')
      let queryStringObject = {
        param1: '123' // API Gateway passes strings
      }
      let result = objectUnderTest(queryStringObject, null)
      expect(result).toEqual({ isValid: true })
    })

    it('should pass validation when the param is zero', () => {
      let objectUnderTest = responseHelper._testonly.queryStringParamIsPositiveNumberIfPresentValidator('param1')
      let queryStringObject = {
        param1: '0'
      }
      let result = objectUnderTest(queryStringObject, null)
      expect(result).toEqual({ isValid: true })
    })

    it('should fail validation when the param is present but is a negative number', () => {
      let objectUnderTest = responseHelper._testonly.queryStringParamIsPositiveNumberIfPresentValidator('param1')
      let queryStringObject = {
        param1: '-1'
      }
      let result = objectUnderTest(queryStringObject, null)
      expect(result.isValid).toBe(false)
      expect(result.message).toBeDefined()
    })

    it('should fail validation when the param is present and NOT a number', () => {
      let objectUnderTest = responseHelper._testonly.queryStringParamIsPositiveNumberIfPresentValidator('param1')
      let queryStringObject = {
        param1: 'asdf'
      }
      let result = objectUnderTest(queryStringObject, null)
      expect(result.isValid).toBe(false)
      expect(result.message).toBeDefined()
    })
  })

  describe('.newVersionHandler()', () => {
    it('should match a single version', () => {
      let event = {
        requestContext: {
          path: '/v1/someResource'
        }
      }
      let objectUnderTest = responseHelper.newVersionHandler({
        '/v1/': () => {
          return 'one'
        }
      })
      let result = objectUnderTest.handle(event)
      expect(result()).toBe('one')
    })

    it('should match the correct version out of many', () => {
      let event = {
        requestContext: {
          path: '/v2/someResource'
        }
      }
      let objectUnderTest = responseHelper.newVersionHandler({
        '/v1/': () => {
          return 'one'
        },
        '/v2/': () => {
          return 'two'
        }
      })
      let result = objectUnderTest.handle(event)
      expect(result()).toBe('two')
    })

    it('should strip the stage when present', () => {
      let event = {
        requestContext: {
          path: '/dev/v1/someResource'
        }
      }
      let objectUnderTest = responseHelper.newVersionHandler({
        '/v1/': () => {
          return 'one'
        }
      })
      let result = objectUnderTest.handle(event)
      expect(result()).toBe('one')
    })

    it('should be able to handle non-functions and the values in the config', () => {
      let event = {
        requestContext: {
          path: '/dev/v1/someResource'
        }
      }
      let objectUnderTest = responseHelper.newVersionHandler({
        '/v1/': 'some value'
      })
      let result = objectUnderTest.handle(event)
      expect(result).toBe('some value')
    })

    it('should throw the expected error when we pass a config that is not an object', () => {
      try {
        responseHelper.newVersionHandler('not an object')
        fail()
      } catch (error) {
        expect(error.message).toBe('Programmer problem: supplied config is not an object')
        // success
      }
    })

    it('should throw the expected error when we handle a path that is not in the config', () => {
      try {
        let event = {
          requestContext: {
            path: '/v3/someResource'
          }
        }
        let objectUnderTest = responseHelper.newVersionHandler({
          '/v1/': () => {}
        })
        objectUnderTest.handle(event)
        fail()
      } catch (error) {
        expect(error.message).toContain("Programmer problem: unhandled path prefix '/v3/' extracted")
        // success
      }
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
      let objectUnderTest = responseHelper.newContentNegotiationHandler(trackerModule, () => { })
      let event = {
        headers: {
          Accept: 'application/json'
        }
      }
      objectUnderTest(event, null, null)
      expect(trackerModule.hasBeenCalled()).toBeTruthy()
    })

    it('should call the JSON handler when we have a .json suffix on URL', () => {
      let trackerModule = new TrackCallsModule()
      let objectUnderTest = responseHelper.newContentNegotiationHandler(trackerModule, () => { })
      let event = {
        path: '/some/path.json'
      }
      objectUnderTest(event, null, null)
      expect(trackerModule.hasBeenCalled()).toBeTruthy()
    })

    it('should call the CSV handler when we have a CSV Accept header', () => {
      let trackerModule = new TrackCallsModule()
      let objectUnderTest = responseHelper.newContentNegotiationHandler(() => { }, trackerModule)
      let event = {
        headers: {
          Accept: 'text/csv'
        }
      }
      objectUnderTest(event, null, null)
      expect(trackerModule.hasBeenCalled()).toBeTruthy()
    })

    it('should call the CSV handler when we have a .csv suffix on URL (ignoring the Accept header)', () => {
      let trackerModule = new TrackCallsModule()
      let objectUnderTest = responseHelper.newContentNegotiationHandler(() => { }, trackerModule)
      let event = {
        path: '/some/path.csv',
        headers: {
          Accept: 'application/json' // needs to ignore this header
        }
      }
      objectUnderTest(event, null, null)
      expect(trackerModule.hasBeenCalled()).toBeTruthy()
    })

    it('should return a 400 when it cannot handle the Accept header', done => {
      let trackerModule = new TrackCallsModule()
      let objectUnderTest = responseHelper.newContentNegotiationHandler(() => { }, trackerModule)
      let event = {
        headers: {
          Accept: 'something/weird'
        }
      }
      objectUnderTest(event, null, (_, result) => {
        expect(result.statusCode).toBe(400)
        done()
      })
    })
  })
})

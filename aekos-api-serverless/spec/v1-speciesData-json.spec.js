'use strict'
let uberRouter = require('../uberRouter')
let StubDB = require('./StubDB')
let ConsoleSilencer = require('./ConsoleSilencer')
let consoleSilencer = new ConsoleSilencer()

describe('/v1/speciesData-json', () => {
  describe('doHandle', () => {
    let result = null
    beforeEach(done => {
      let stubDb = new StubDB()
      stubDb.setExecSelectPromiseResponses([
        [{
          recordNum: 1,
          locationName: 'location1',
          datasetName: 'dataset1'
        }, {
          recordNum: 2,
          locationName: 'location2',
          datasetName: 'dataset2'
        }],
        [ {recordsHeld: 31} ]
      ])
      let event = {
        body: JSON.stringify({speciesNames: ['species one']}),
        queryStringParameters: {
          rows: '15',
          start: '0'
        },
        headers: {
          Host: 'api.aekos.org.au',
          'X-Forwarded-Proto': 'https'
        },
        requestContext: { path: '/v1/speciesData.json' },
        path: '/v1/speciesData.json'
      }
      let callback = (_, theResult) => {
        result = theResult
        done()
      }
      uberRouter._testonly.doHandle(event, callback, stubDb, () => { return 42 })
    })

    it('should return a 200 response when all params are supplied', () => {
      expect(result.statusCode).toBe(200)
      expect(result.headers).toEqual({
        'Access-Control-Allow-Origin': '*',
        'Access-Control-Allow-Credentials': true,
        'Access-Control-Expose-Headers': 'link',
        'Content-Type': "'application/json'",
        link: '<https://api.aekos.org.au/v1/speciesData.json?rows=15&start=15>; rel="next", ' +
              '<https://api.aekos.org.au/v1/speciesData.json?rows=15&start=30>; rel="last"'
      })
      expect(JSON.parse(result.body)).toEqual({
        responseHeader: {
          elapsedTime: 42,
          numFound: 31,
          pageNumber: 1,
          params: {
            rows: 15,
            start: 0,
            speciesNames: ['species one']
          },
          totalPages: 3
        },
        response: [
          {recordNum: 1}, // no v2 fields should be in the response
          {recordNum: 2}
        ]
      })
    })
  })

  describe('doHandle', () => {
    let result = null
    beforeEach(done => {
      let stubDb = new StubDB()
      stubDb.setExecSelectPromiseResponses([
        [ {recordNum: 1} ],
        [ {recordsHeld: 31} ]
      ])
      let event = {
        body: JSON.stringify({speciesNames: ['species one']}),
        queryStringParameters: {
          start: '0'
        },
        headers: {
          Host: 'api.aekos.org.au',
          'X-Forwarded-Proto': 'https'
        },
        requestContext: { path: '/v1/speciesData.json' },
        path: '/v1/speciesData.json'
      }
      let callback = (_, theResult) => {
        result = theResult
        done()
      }
      uberRouter._testonly.doHandle(event, callback, stubDb, () => { return 0 })
    })

    it('should calculate paging information when only start is provided', () => {
      expect(result.statusCode).toBe(200)
      let responseHeader = JSON.parse(result.body).responseHeader
      expect(responseHeader.pageNumber).toBe(1)
      expect(responseHeader.totalPages).toBe(2)
    })
  })

  describe('doHandle', () => {
    let result = null
    beforeEach(done => {
      let stubDb = new StubDB()
      stubDb.setExecSelectPromiseResponses([
        [ {recordNum: 1} ],
        [ {recordsHeld: 14} ]
      ])
      let event = {
        body: JSON.stringify({speciesNames: ['species one']}),
        queryStringParameters: {
          start: '10' // start is less than the default rows value
        },
        headers: {
          Host: 'api.aekos.org.au',
          'X-Forwarded-Proto': 'https'
        },
        requestContext: { path: '/v1/speciesData.json' },
        path: '/v1/speciesData.json'
      }
      let callback = (_, theResult) => {
        result = theResult
        done()
      }
      uberRouter._testonly.doHandle(event, callback, stubDb, () => { return 0 })
    })

    it('should behave weirdly when the user supplies wierd params', () => {
      expect(result.statusCode).toBe(200)
      let responseHeader = JSON.parse(result.body).responseHeader
      expect(responseHeader.params.rows).toBe(20) // we get default rows which is greater than the start
      expect(responseHeader.pageNumber).toBe(1) // should probably be 2 but it's a weird edge case
      expect(responseHeader.totalPages).toBe(1) // are there 1,2 or 3 pages?
    })
  })

  describe('doHandle', () => {
    let result = null
    beforeEach(done => {
      let stubDb = new StubDB()
      let event = {
        body: null, // don't supply 'speciesName' (or even a body)
        queryStringParameters: {
          start: '0'
        },
        path: '/v1/speciesData.json'
      }
      let callback = (_, theResult) => {
        result = theResult
        done()
      }
      uberRouter._testonly.doHandle(event, callback, stubDb, () => { return 42 })
    })

    it('should return a 400 response when we do not supply speciesNames', () => {
      expect(result.statusCode).toBe(400)
      expect(result.headers).toEqual({
        'Access-Control-Allow-Origin': '*',
        'Access-Control-Allow-Credentials': true,
        'Access-Control-Expose-Headers': 'link',
        'Content-Type': "'application/json'"
      })
      expect(JSON.parse(result.body)).toEqual({
        message: 'No request body was supplied',
        statusCode: 400
      })
    })
  })

  describe('doHandle', () => {
    let result = null
    beforeEach(done => {
      let stubDb = new StubDB()
      spyOn(stubDb, 'execSelectPromise').and.throwError('some error')
      let event = {
        body: JSON.stringify({speciesNames: ['species one']}),
        queryStringParameters: null,
        requestContext: { path: '/v1/speciesData.json' },
        path: '/v1/speciesData.json'
      }
      let callback = (_, theResult) => {
        consoleSilencer.resetConsoleError()
        result = theResult
        done()
      }
      consoleSilencer.silenceConsoleError()
      uberRouter._testonly.doHandle(event, callback, stubDb, () => { return 42 })
    })

    it('should return a 500 response when executing a query fails', () => {
      expect(result.statusCode).toBe(500)
      expect(result.headers).toEqual({
        'Access-Control-Allow-Origin': '*',
        'Access-Control-Allow-Credentials': true,
        'Access-Control-Expose-Headers': 'link',
        'Content-Type': "'application/json'"
      })
      expect(JSON.parse(result.body)).toEqual({
        message: 'Sorry about that, something has gone wrong',
        statusCode: 500
      })
    })
  })
})

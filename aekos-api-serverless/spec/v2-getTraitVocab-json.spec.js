'use strict'
let StubDB = require('./StubDB')
let ConsoleSilencer = require('./ConsoleSilencer')
let consoleSilencer = new ConsoleSilencer()

describe('/v2/getTraitVocab-json', function () {
  let objectUnderTest = require('../traitVocab-json')
  let uberRouter = require('../uberRouter')

  describe('.doHandle()', () => {
    let expectedSql1 = `
    SELECT traitName AS code, count(*) AS recordsHeld
    FROM traits
    GROUP BY 1
    ORDER BY 1;`
    let stubDb = new StubDB()
    let result = null
    beforeEach(done => {
      let event = {
        path: '/v2/getTraitVocab.json',
        headers: {
          Host: 'api.aekos.org.au',
          'X-Forwarded-Proto': 'https'
        }
      }
      stubDb.setExecSelectPromiseResponses([
        [{code: 'height', recordsHeld: 123}]
      ])
      spyOn(stubDb, 'execSelectPromise').and.callThrough()
      let callback = (_, theResult) => {
        result = theResult
        done()
      }
      uberRouter._testonly.doHandle(event, callback, stubDb)
    })

    it('should return the query result', () => {
      expect(stubDb.execSelectPromise).toHaveBeenCalledWith(expectedSql1)
      expect(result.statusCode).toBe(200)
      expect(result.headers).toEqual({
        'Access-Control-Allow-Origin': '*',
        'Access-Control-Allow-Credentials': true,
        'Access-Control-Expose-Headers': 'link',
        'Content-Type': "'application/json'"
      })
      expect(JSON.parse(result.body)).toEqual(
        [{code: 'height', label: 'Height', recordsHeld: 123}]
      )
    })
  })

  describe('.doHandle()', () => {
    let stubDb = new StubDB()
    let result = null
    beforeEach(done => {
      let event = {
        path: '/v2/getTraitVocab.json',
        headers: {
          Host: 'api.aekos.org.au',
          'X-Forwarded-Proto': 'https'
        }
      }
      stubDb.setExecSelectPromiseResponses([
        { forEach: () => { throw new Error('bang') } }
      ])
      spyOn(stubDb, 'execSelectPromise').and.callThrough()
      let callback = (_, theResult) => {
        consoleSilencer.resetConsoleError()
        result = theResult
        done()
      }
      consoleSilencer.silenceConsoleError()
      uberRouter._testonly.doHandle(event, callback, stubDb)
    })

    it('should return the query result', () => {
      expect(result.statusCode).toBe(500)
    })
  })

  describe('.mapQueryResult()', () => {
    it('should map to the code field', function () {
      let queryResult = [
        {
          code: 'averageHeight',
          count: 123
        }
      ]
      let result = objectUnderTest.mapQueryResult(queryResult)
      expect(result.length).toBe(1)
      let first = result[0]
      expect(first.code).toBe('averageHeight')
      expect(first.label).toBe('Average Height')
      expect(first.count).toBe(123)
    })
  })
})

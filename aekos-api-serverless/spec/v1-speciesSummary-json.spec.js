'use strict'
let uberRouter = require('../uberRouter')
let StubDB = require('./StubDB')
let ConsoleSilencer = require('./ConsoleSilencer')
let consoleSilencer = new ConsoleSilencer()

describe('/v1/speciesSummary-json', () => {
  describe('.doHandle()', () => {
    let result = null
    beforeEach(done => {
      let stubDb = new StubDB()
      stubDb.setExecSelectPromiseResponses([
        [{ speciesName: 'species one', recordsHeld: 123 }]
      ])
      let event = {
        path: '/v1/speciesSummary.json',
        body: JSON.stringify({ speciesNames: ['species one'] })
      }
      let callback = (_, theResult) => {
        result = theResult
        done()
      }
      uberRouter._testonly.doHandle(event, callback, stubDb)
    })

    it('should handle a single element', () => {
      expect(result.statusCode).toBe(200)
      expect(result.headers).toEqual({
        'Access-Control-Allow-Origin': '*',
        'Access-Control-Allow-Credentials': true,
        'Access-Control-Expose-Headers': 'link',
        'Content-Type': "'application/json'"
      })
      expect(JSON.parse(result.body)).toEqual([
        { speciesName: 'species one', recordsHeld: 123, id: 'notusedanymore' }
      ])
    })
  })

  describe('.doHandle()', () => {
    let result = null
    beforeEach(done => {
      let stubDb = new StubDB()
      stubDb.setExecSelectPromiseResponses([
        { forEach: () => { throw new Error('bang!') } }
      ])
      let event = {
        path: '/v1/speciesSummary.json',
        body: JSON.stringify({ speciesNames: ['species one'] })
      }
      let callback = (_, theResult) => {
        consoleSilencer.resetConsoleError()
        result = theResult
        done()
      }
      consoleSilencer.silenceConsoleError()
      uberRouter._testonly.doHandle(event, callback, stubDb)
    })

    it('should catch an error thrown during query result processing and respond with a 500', () => {
      expect(result.statusCode).toBe(500)
    })
  })
})

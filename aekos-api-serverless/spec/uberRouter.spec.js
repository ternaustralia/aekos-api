'use strict'
let ConsoleSilencer = require('./ConsoleSilencer')
let consoleSilencer = new ConsoleSilencer()

describe('uberRouter', () => {
  let objectUnderTest = require('../uberRouter')

  describe('handler()', () => {
    let result = null
    beforeEach(done => {
      let event = {
        requestContext: {
          path: '/path/that/is/not/mapped'
        }
      }
      let callback = (_, theResult) => {
        consoleSilencer.resetConsoleError()
        result = theResult
        done()
      }
      consoleSilencer.silenceConsoleError()
      objectUnderTest._testonly.doHandle(event, callback, null)
    })

    it('should 404 when an unmapped path is supplied', () => {
      expect(result.statusCode).toBe(404)
      expect(result.body).toBe(JSON.stringify(
        { message: `The resource '/path/that/is/not/mapped' does not exist.`, statusCode: 404 }
      ))
    })
  })
})

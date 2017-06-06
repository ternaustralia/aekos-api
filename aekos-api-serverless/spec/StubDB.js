'use strict'

class StubDB {
  constructor () {
    this.escape = require('../db-helper').escape
    this._execSelectPromiseCallNum = 0
  }

  setExecSelectPromiseResponses (responses) {
    let responseObj = {}
    for (let i = 0; i < responses.length; i++) {
      let responseNum = i + 1
      responseObj[responseNum] = new Promise((resolve, reject) => {
        resolve(responses[i])
      })
    }
    this._execSelectPromiseResponses = responseObj
  }

  execSelectPromise () {
    this._execSelectPromiseCallNum++
    let result = this._execSelectPromiseResponses[this._execSelectPromiseCallNum]
    return result
  }
}

describe('StubDB', () => {
  it('should return a single configured response', (done) => {
    let objectUnderTest = new StubDB()
    objectUnderTest.setExecSelectPromiseResponses(['the only response'])
    objectUnderTest.execSelectPromise().then(theResponse => {
      expect(theResponse).toBe('the only response')
      done()
    })
  })
})

module.exports = StubDB

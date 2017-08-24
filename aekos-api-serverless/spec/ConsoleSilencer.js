'use strict'
/* Stops your test logs from being flooded with *expected* error messages
 * usage:
 *  let ConsoleSilencer = require('./ConsoleSilencer')
 *  let consoleSilencer = new ConsoleSilencer()
 *  consoleSilencer.silenceConsoleError()
 *  // do error producing thing
 *  consoleSilencer.resetConsoleError() // might need to call this in a callback
 *
 * I'm not sure if this is a perfect solution. If you silence then reset in a
 * callback, I think there's a possibility you could miss an error that you didn't
 * intend to silence from somewhere else. It depends how long the callback takes
 * to execute. It's good enough for now though.
*/

class ConsoleSilencer {
  constructor () {
    this.origConsoleError = console.error
    this.origConsoleWarn = console.warn
    this.nullConsole = () => {}
    this.nullConsole.isSilenced = true
    this.isErrorSilenced = false
    this.isWarnSilenced = false
    this.exitHandler = (c) => {
      if (c.isErrorSilenced || c.isWarnSilenced) {
        let consoleName = c.isErrorSilenced ? 'error' : 'warn'
        throw new Error(`Programmer problem: the '${consoleName}' console was silenced ` +
        'but never reset. You might be missing out on important messages. Fix this!!!')
      }
    }
    this.exitHandler = this.exitHandler.bind(null, this)
  }

  silenceConsoleError () {
    console.error = this.nullConsole
    this.isErrorSilenced = true
    process.on('exit', this.exitHandler)
  }
  resetConsoleError () {
    console.error = this.origConsoleError
    this.isErrorSilenced = false
    process.removeListener('exit', this.exitHandler)
  }
  silenceConsoleWarn () {
    console.warn = this.nullConsole
    this.isWarnSilenced = true
    process.on('exit', this.exitHandler)
  }
  resetConsoleWarn () {
    console.warn = this.origConsoleWarn
    this.isWarnSilenced = false
    process.removeListener('exit', this.exitHandler)
  }
}

module.exports = ConsoleSilencer

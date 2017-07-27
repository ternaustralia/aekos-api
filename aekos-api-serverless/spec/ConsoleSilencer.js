'use strict'
/* Stops your test logs from being flooded with *expected* error messages
 * usage:
 *  let ConsoleSilencer = require('./ConsoleSilencer')
 *  let consoleSilencer = new ConsoleSilencer()
 *  consoleSilencer.silenceConsoleError()
 *  // do error producing thing
 *  consoleSilencer.resetConsoleError()
*/

class ConsoleSilencer {
  constructor () {
    this.origConsoleError = console.error
    this.origConsoleWarn = console.warn
  }

  silenceConsoleError () {
    console.error = () => { }
  }
  resetConsoleError () {
    console.error = this.origConsoleError
  }
  silenceConsoleWarn () {
    console.warn = () => { }
  }
  resetConsoleWarn () {
    console.warn = this.origConsoleWarn
  }
}

module.exports = ConsoleSilencer

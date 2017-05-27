'use strict'
class FieldConfig {
  constructor (name, isQuoted) {
    this.name = name
    this.isQuoted = isQuoted
  }
}
function quoted (name) {
  return new FieldConfig(name, true)
}

function notQuoted (name) {
  return new FieldConfig(name, false)
}

module.exports.quoted = quoted
module.exports.notQuoted = notQuoted

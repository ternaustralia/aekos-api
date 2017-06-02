'use strict'
class FieldConfig {
  constructor (name, isQuoted, getValueImpl) {
    this.name = name
    this.isQuoted = isQuoted
    this._getValueImpl = getValueImpl
  }

  getValue (targetObj) {
    return this._getValueImpl(targetObj)
  }
}
function quoted (name) {
  return new FieldConfig(name, true, targetObj => {
    let value = targetObj[name]
    if (value === null) {
      return ''
    }
    return `"${value}"`
  })
}

function notQuoted (name) {
  return new FieldConfig(name, false, targetObj => {
    let value = targetObj[name]
    if (value === null) {
      return ''
    }
    return value
  })
}

function quotedListConcat (name) {
  return new FieldConfig(name, false, targetObj => {
    let value = targetObj[name]
    if (value === null) {
      return ''
    }
    if (value.constructor !== Array) {
      throw new Error(`Data problem: the value '${JSON.stringify(value)}' of field '${name}' on object '${JSON.stringify(targetObj)}' is not an Array, cannot handle it.`)
    }
    let concatValue = value.reduce((prev, curr) => {
      if (prev === '') {
        return curr
      }
      return prev + '|' + curr
    }, '')
    return `"${concatValue}"`
  })
}

module.exports.quoted = quoted
module.exports.notQuoted = notQuoted
module.exports.quotedListConcat = quotedListConcat

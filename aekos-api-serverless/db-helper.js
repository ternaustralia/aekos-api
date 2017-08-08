'use strict'
let mysql = require('mysql')
let promiseMysql = require('promise-mysql')

module.exports.execSelect = (selectSql, callback) => {
  var connection = mysql.createConnection({
    host: process.env.DBURL,
    port: process.env.DBPORT,
    user: process.env.DBUSER,
    password: process.env.DBPASS,
    database: process.env.DBNAME
  })
  connection.connect()
  connection.query(selectSql, function (error, results, fields) {
    if (error) {
      throw new Error(`Failed to execute query '${selectSql}' with error '${JSON.stringify(error)}'`)
    }
    callback(results)
  })
  connection.end()
}

module.exports.execSelectPromise = selectSql => {
  return promiseMysql.createConnection({
    host: process.env.DBURL,
    port: process.env.DBPORT,
    user: process.env.DBUSER,
    password: process.env.DBPASS,
    database: process.env.DBNAME
  }).then(function (conn) {
    let result = conn.query(selectSql)
    conn.end()
    return result
  })
  .catch(function (error) {
    throw new Error(`Failed to execute query '${selectSql}' with error '${JSON.stringify(error)}'`)
  })
}

module.exports.escape = (unescapedValue) => {
  if (typeof unescapedValue === 'undefined') {
    throw new Error('Data problem: supplied value cannot be undefined')
  }
  if (unescapedValue.constructor === Array) {
    return unescapedValue.map(e => {
      return mysql.escape(e)
    })
  }
  return mysql.escape(unescapedValue)
}

module.exports.toSqlList = (unescapedValues) => {
  if (unescapedValues.constructor !== Array) {
    throw new Error('parameter must be an array')
  }
  return unescapedValues.map(e => {
    return mysql.escape(e)
  }).reduce((prev, curr) => {
    if (prev) {
      return prev + ',' + curr
    }
    return prev + curr
  }, '')
}

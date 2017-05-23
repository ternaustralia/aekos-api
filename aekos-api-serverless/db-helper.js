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
    console.log('got connection, about to run query')
    return conn.query(selectSql)
  })
  // .catch(function (error) {
  //   throw new Error(`Failed to execute query '${selectSql}' with error '${JSON.stringify(error)}'`)
  // })
}

module.exports.escape = (unescapedValue) => {
  return mysql.escape(unescapedValue)
}

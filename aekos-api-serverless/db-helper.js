'use strict'
var mysql = require('mysql')

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
      throw new Error()
    }
    callback(results)
  })
  connection.end()
}

module.exports.escape = (unescapedValue) => {
  return mysql.escape(unescapedValue)
}

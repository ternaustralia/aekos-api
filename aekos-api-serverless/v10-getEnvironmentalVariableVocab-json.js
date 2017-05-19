'use strict'
var r = require('./response-helper')
var mysql = require('mysql')

module.exports.handler = (event, context, callback) => {
  var connection = mysql.createConnection({
    host: process.env.DBURL,
    port: process.env.DBPORT,
    user: process.env.DBUSER,
    password: process.env.DBPASS,
    database: process.env.DBNAME
  })
  // FIXME get service deployed into the right VPC and use a role with access
  connection.connect()
  connection.query("SELECT traitName, count(*) FROM envvars GROUP BY 1 ORDER BY 1;", function (error, results, fields) {
    if (error) {
      throw new Error()
    }
    console.log('The solution is: ', JSON.stringify(results))
    // TODO add title information
    r.ok(callback, results)
  })
  connection.end()
}

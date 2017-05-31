#!/usr/bin/env node
'use strict'
let fs = require('fs')
let stdout = process.stdout
let inFile = process.argv[2]
fs.readFile(inFile, 'utf8', function (err, data) {
  if (err) {
    return console.log(err)
  }
  handleData(data)
})

function handleData (data) {
  let parsedData = JSON.parse(data)
  let codeVarName = parsedData.head.vars[0]
  let labelVarName = parsedData.head.vars[1]
  let result = {}
  parsedData.results.bindings.forEach(function (curr) {
    let currCode = curr[codeVarName].value
    let currLabel = curr[labelVarName].value
    if (result[currCode]) {
      console.warn(`Steamrolling '${currCode}'='${result[currCode]}' with '${currCode}'='${currLabel}'`)
    }
    result[currCode] = currLabel
  })
  stdout.write(JSON.stringify(result, null, 2))
  stdout.write('\n')
}

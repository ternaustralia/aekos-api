#!/usr/bin/env node
let stdin = process.stdin,
  stdout = process.stdout,
  inputChunks = []

const stage = 'dev'
const targetRestApiName = stage + '-aekos-api-serverless'

stdin.resume()
stdin.setEncoding('utf8')

stdin.on('data', function (chunk) {
  inputChunks.push(chunk);
})

stdin.on('end', function () {
  let inputJSON = inputChunks.join()
  let parsedData = JSON.parse(inputJSON)
  parsedData.items.forEach(function (curr) {
    if (curr.name === targetRestApiName) {
      stdout.write(curr.id)
      stdout.write('\n')
    }
  })
})

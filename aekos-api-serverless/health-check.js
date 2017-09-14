#!/usr/bin/env node
/* Enlessly sends requests to the server and logs the response.
 * Built to try to determine why we get 502 responses occasionally.
 * Intended to be run like:
 *  node health-check.js | tee some-log-file.csv
 * ...and the log file will only get the CSV, not the stderr messages.
 */
const https = require('https')

const maxSleepMinutes = getMaxSleepMinutes()
console.warn(`[INFO] Using a max sleep of ${maxSleepMinutes} minutes`)
const loggingFrequency = 10
const targetHost = 'test.api.aekos.org.au'
let callCount = 0
console.log(`callTimestamp;callTimestampMs;requestID;statusCode;elapsedMs;sleepPeriodMs;sleepPeriodMinutes;bodyFragment`)
scheduleNext()

function doCall (namedRequest, sleepPeriodMs, done) {
  let start = new Date()
  let startMs = start.getTime()
  let reqConfig = namedRequest.req
  let req = https.request(reqConfig.options, (res) => {
    const { statusCode } = res

    res.setEncoding('utf8')
    let rawData = ''
    const maxRawDataLength = 90
    res.on('data', (chunk) => {
      rawData += chunk
      if (rawData.length > maxRawDataLength) {
        res.resume()
      }
    })
    res.on('end', () => {
      let bodyFragment = stripCsvNoise(rawData)
      bodyFragment = bodyFragment.length > maxRawDataLength ? bodyFragment.substr(0, maxRawDataLength) + '...' : bodyFragment
      let elapsed = new Date().getTime() - startMs
      console.log(`${format(start)};${startMs};${namedRequest.name};${statusCode};${elapsed};${sleepPeriodMs};${msToMinutes(sleepPeriodMs)};${bodyFragment}`)
      done()
    })
  })
  req.on('error', (e) => {
    console.error(new Error(`Got error: ${e.message}`))
    let elapsed = new Date().getTime() - start
    console.log(`${format(start)};${startMs};${namedRequest.name};FAIL;${elapsed};${sleepPeriodMs};${msToMinutes(sleepPeriodMs)};${e.message}`)
    done()
  })
  reqConfig.body(req)
}

function scheduleNext () {
  callCount++
  let availableReqs = getAvailableReqs()
  let reqId = Math.floor(Math.random() * availableReqs.length)
  let req = availableReqs[reqId]
  let sleepPeriodMs = Math.floor((Math.random() * maxSleepMinutes * 60 * 1000))
  let wakeTime = new Date(new Date().getTime() + sleepPeriodMs)
  console.warn(`[INFO] Sleeping for ${sleepPeriodMs}ms/${msToMinutes(sleepPeriodMs)} minutes (${format(wakeTime)}) before calling ${req.name}`)
  setTimeout(() => {
    doCall(req, sleepPeriodMs, () => {
      scheduleNext()
    })
  }, sleepPeriodMs)
  if (callCount % loggingFrequency === 0) {
    console.warn('[INFO] Number of calls so far=' + callCount)
  }
}

function stripCsvNoise (body) {
  if (body.indexOf('\n') === -1) {
    return body
  }
  let firstNewlineIndex = body.indexOf('\n')
  return body.substr(firstNewlineIndex + 1).replace('\n', ' ')
}

function format (d) {
  const days = ['Sun', 'Mon', 'Tues', 'Wed', 'Thu', 'Fri', 'Sat']
  function pad (v) {
    if (('' + v).length === 2) {
      return v
    }
    return '0' + v
  }
  return `${days[d.getDay()]} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

function msToMinutes (value) {
  return toTwoDecimalPlaces(value / 1000 / 60)
}

function toTwoDecimalPlaces (value) {
  return Math.floor(value * 100) / 100
}

// Highest occurring species
// 'Avena barbata', '44045'
// 'Stipa sp.', '41297'
// 'Atriplex vesicaria', '33988'
// 'Senna artemisioides subsp. coriacea', '30362'
// 'Atriplex stipitata', '18410'
// 'Eremophila longifolia', '17825'
// 'Austrostipa sp.', '17538'
function getAvailableReqs () {
  return [
    nr('v2/getTraitVocab.json', getJson('/v2/getTraitVocab.json')),
    nr('v2/getEnvironmentalVariableVocab.json', getJson('/v2/getEnvironmentalVariableVocab.json')),
    nr('v2/allSpeciesData-json#near-end-big-page', getJson('/v2/allSpeciesData?start=3966000&rows=1000')),
    nr('v2/speciesData-json#default-page', postJson('/v2/speciesData', {
      'speciesNames': [ 'Austrostipa sp.' ]
    })),
    nr('v2/speciesData-json#200-recs', postJson('/v2/speciesData?rows=200', {
      'speciesNames': [ 'Avena barbata', 'Stipa sp.' ]
    })),
    nr('v2/speciesData-json#1000-recs', postJson('/v2/speciesData?rows=1000', {
      'speciesNames': [ 'Avena barbata', 'Stipa sp.' ]
    })),
    nr('v2/speciesData-csv#default-page+stressful', postCsv('/v2/speciesData', {
      'speciesNames': [ 'Avena barbata', 'Stipa sp.' ]
    })),
    nr('v2/speciesData-csv#default-page+easy', postCsv('/v2/speciesData', {
      'speciesNames': [ 'Abutilon cryptopetalum (F.Muell.) Benth.', 'Geijera parviflora Lindl.', 'Eucalyptus melanophloia F.Muell.' ]
    })),
    nr('v2/speciesData-csv#300-recs', postCsv('/v2/speciesData?rows=300&start=300', {
      'speciesNames': [ 'Avena barbata', 'Stipa sp.' ]
    })),
    nr('v2/traitData-json#default-page', postJson('/v2/traitData', {
      'traitNames': [ 'averageHeight', 'cover' ],
      'speciesNames': [ 'Abutilon cryptopetalum (F.Muell.) Benth.' ]
    })),
    nr('v2/traitData-json#200-recs', postJson('/v2/traitData?rows=200', {
      'traitNames': [ ],
      'speciesNames': [ 'Avena barbata' ]
    })),
    nr('v2/traitData-json#1000-recs', postJson('/v2/traitData?rows=1000', {
      'traitNames': [ ],
      'speciesNames': [ 'Avena barbata' ]
    })),
    nr('v2/traitData-csv#100-recs', postCsv('/v2/traitData?rows=1000', {
      'traitNames': [ ],
      'speciesNames': [ 'Avena barbata' ]
    }))
  ]
}

// Creates a named request
function nr (name, req) {
  return {
    name: name,
    req: req
  }
}

function getJson (uri) {
  return {
    options: {
      protocol: 'https:',
      host: targetHost,
      method: 'GET',
      path: uri,
      headers: {
        accept: 'application/json'
      },
      timeout: 60 * 1000
    },
    body: (req) => {
      req.end()
    }
  }
}

function postJson (uri, body) {
  return postHelper(uri, body, 'application/json')
}

function postCsv (uri, body) {
  return postHelper(uri, body, 'text/csv')
}

function postHelper (uri, body, accept) {
  let bodyData = JSON.stringify(body)
  return {
    options: {
      protocol: 'https:',
      host: targetHost,
      method: 'POST',
      path: uri,
      headers: {
        accept: accept,
        'content-length': bodyData.length,
        'content-type': 'application/json'
      },
      timeout: 60 * 1000
    },
    body: (req) => {
      req.write(bodyData)
      req.end()
    }
  }
}

function getMaxSleepMinutes () {
  const defaultMaxSleepMinutes = 30
  let maxSleepParam = parseFloat(process.argv[2])
  if (!maxSleepParam) {
    maxSleepParam = defaultMaxSleepMinutes
  }
  return maxSleepParam
}

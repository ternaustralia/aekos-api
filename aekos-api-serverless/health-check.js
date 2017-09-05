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
console.log(`callTimestamp;callTimestampMs;path;statusCode;elapsedMs;sleepPeriodMs;sleepPeriodMinutes;bodyFragment`)
scheduleNext()

function doCall (reqConfig, sleepPeriodMs, done) {
  let start = new Date()
  let startMs = start.getTime()
  let req = https.request(reqConfig.options, (res) => {
    const { statusCode } = res

    res.setEncoding('utf8')
    let rawData = ''
    const maxRawDataLength = 20
    res.on('data', (chunk) => {
      rawData += chunk
      if (rawData.length > maxRawDataLength) {
        res.resume()
      }
    })
    res.on('end', () => {
      let bodyFragment = rawData.length > maxRawDataLength ? rawData.substr(0, maxRawDataLength) + '...' : rawData
      let elapsed = new Date().getTime() - startMs
      console.log(`${start};${startMs};${reqConfig.options.path};${statusCode};${elapsed};${sleepPeriodMs};${msToMinutes(sleepPeriodMs)};${bodyFragment}`)
      done()
    })
  })
  req.on('error', (e) => {
    console.error(new Error(`Got error: ${e.message}`))
    let elapsed = new Date().getTime() - start
    console.log(`${start};${startMs};${reqConfig.options.path};FAIL;${elapsed};${sleepPeriodMs};${msToMinutes(sleepPeriodMs)};${e.message}`)
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
  console.warn(`[INFO] Sleeping for ${sleepPeriodMs}ms (${msToMinutes(sleepPeriodMs)} minutes) before calling ${req.options.path}`)
  setTimeout(() => {
    doCall(req, sleepPeriodMs, () => {
      scheduleNext()
    })
  }, sleepPeriodMs)
  if (callCount % loggingFrequency === 0) {
    console.warn('[INFO] Number of calls so far=' + callCount)
  }
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
    getJson('/v2/getTraitVocab.json'),
    getJson('/v2/getEnvironmentalVariableVocab.json'),
    postJson('/v2/traitData', {
      'traitNames': [ 'averageHeight', 'cover' ],
      'speciesNames': [ 'Abutilon cryptopetalum (F.Muell.) Benth.' ]
    })
  ]
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
  let bodyData = JSON.stringify(body)
  return {
    options: {
      protocol: 'https:',
      host: targetHost,
      method: 'POST',
      path: uri,
      headers: {
        accept: 'application/json',
        'content-length': bodyData.length
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

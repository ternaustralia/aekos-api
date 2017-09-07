'use strict'
let r = require('./response-helper')

module.exports.doHandle = doHandle
function doHandle (event, callback, db, _) {
  r.handleJsonGet(event, callback, db, r.nullValidator, responder)
}

function responder (_, db, _2, _3) {
  let datasetGroupsPromise = db.execSelectPromise(getDatasetGroupsSql())
  let sampProtoGroupsPromise = db.execSelectPromise(getSamplingProtocolGroupsSql())
  let surveyPromise = db.execSelectPromise(getSurveysSql())
  return Promise.all([datasetGroupsPromise, sampProtoGroupsPromise, surveyPromise]).then(values => {
    return Promise.resolve(wrap({
      datasetGroups: mapNames(values[0]),
      samplingProtocolGroups: mapNames(values[1]),
      surveys: values[2]
    }))
  })
}

function wrap (body) {
  return { body }
}

function mapNames (values) {
  return values.reduce((previous, current) => {
    let value = current.id
    let name = current.name
    if (!name) {
      name = getMappedName(value)
    }
    previous.push({
      id: value,
      envRecordsHeld: current.envRecordsHeld,
      name: name
    })
    return previous
  }, [])
}

function getDatasetGroupsSql () {
  return `
    SELECT
      SUBSTRING_INDEX(samplingProtocol, '/', 3) AS id,
      COUNT(*) AS envRecordsHeld
    FROM citations
    GROUP BY 1
    ORDER BY 1;
  `
}

function getSamplingProtocolGroupsSql () {
  return `
    SELECT
      id,
      envRecordsHeld,
      c.datasetName AS \`name\`
    FROM (
      SELECT
        SUBSTRING_INDEX(c.samplingProtocol, '/', 4) AS id,
        COUNT(*) AS envRecordsHeld
      FROM citations AS c
      GROUP BY 1
      ) AS d
    LEFT JOIN citations AS c
    ON d.id = c.samplingProtocol
    ORDER BY 1;`
}

function getSurveysSql () {
  return `
    SELECT samplingProtocol AS id, datasetName AS name
    FROM citations
    ORDER BY 1;
  `
}

const datasetGroupMapping = {
  '/adelaide.edu.au': 'Adelaide University'
}
const samplingProtocolGroupMapping = {
  '/adelaide.edu.au/Koonamore': 'Koonamore Survey'
}
function getMappedName (key) {
  let cleanedKey = key.replace('aekos.org.au/collection', '')
  let dgResult = datasetGroupMapping[cleanedKey]
  if (dgResult) {
    return dgResult
  }
  let spgResult = samplingProtocolGroupMapping[cleanedKey]
  if (spgResult) {
    return spgResult
  }
  return null
}

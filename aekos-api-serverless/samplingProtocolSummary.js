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
    let name = getMappedName(value, current.name)
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
    CONCAT(SUBSTRING_INDEX(samplingProtocol, '/', 3), '/*') AS id,
    COUNT(*) AS envRecordsHeld
  FROM citations
  GROUP BY 1
  ORDER BY 1;`
}

function getSamplingProtocolGroupsSql () {
  return `
    SELECT
      id,
      envRecordsHeld,
      null AS 'name'
    FROM (
      SELECT
        SUBSTRING_INDEX(samplingProtocol, '/', 4) AS rawId,
        CONCAT(SUBSTRING_INDEX(samplingProtocol, '/', 4), '/*') AS id,
        COUNT(*) AS envRecordsHeld
      FROM citations
      GROUP BY 1
    ) AS d
    WHERE d.rawId NOT IN ( SELECT samplingProtocol FROM citations )
    UNION
    SELECT
      d.id,
      d.envRecordsHeld,
      c.datasetName AS 'name'
    FROM (
      SELECT
        SUBSTRING_INDEX(samplingProtocol, '/', 4) AS id,
        COUNT(*) AS envRecordsHeld
      FROM citations
      GROUP BY 1
    ) AS d
    INNER JOIN citations AS c
    ON d.id = c.samplingProtocol
    AND d.id IN ( SELECT samplingProtocol FROM citations )
    ORDER BY 1;`
}

function getSurveysSql () {
  return `
    SELECT samplingProtocol AS id, datasetName AS name
    FROM citations
    ORDER BY 1;`
}

const datasetGroupMapping = {
  '/adelaide.edu.au/*': 'Adelaide University',
  '/csiro/*': 'TERN Australian Transect Network-CSIRO & NATT',
  '/gov.au/*': 'ABARES Ground Cover Reference Sites DB',
  '/nsw.gov.au/*': 'NSW Office of Environment & Heritage Vegetation Surveys',
  '/qld.gov.au/*': 'QLD Department of Science, Information Technology and Innovation CORVEG DB',
  '/sa.gov.au/*': 'SA Department of Environment, Water and Natural Resources Vegegation Surveys',
  '/sydney.edu.au/*': 'The University of Sydney Desert Ecology Research Group',
  '/tas.gov.au/*': 'TAS Department of Primary Industries, Parks, Water and Environment Platypus Surveys',
  '/uq/*': 'Joint Remote Sensing Research Program AusCover and Supersites SLATS Transects',
  '/wa.gov.au/*': 'TERN Australian Transect Network & WA Biodiversity Conservation & Attractions South West Australian Transitional Transect'
}
const samplingProtocolGroupMapping = {
  '/adelaide.edu.au/ausplotsrangelands': 'TERN AusPlots Rangelands',
  '/adelaide.edu.au/Koonamore/*': 'UofA Koonamore Survey',
  '/adelaide.edu.au/trend': 'TERN Transects for Environmental Monitoring and Decision Making (TREND)',
  '/csiro/natt/*': 'TERN Australian Transect Network-CSIRO & NATT',
  '/gov.au/abares/*': 'ABARES Ground Cover Reference Sites DB by States/Territories',
  '/nsw.gov.au/nsw_atlas/*': 'NSW Office of Environment & Heritage Vegetation Surveys',
  '/sa.gov.au/bdbsa_veg/*': 'Biological Database of SA Vegetation',
  '/sa.gov.au/DEWNR_ROADSIDEVEG/*': 'Roadside Vegetation'
}
function getMappedName (key, fallbackName) {
  let cleanedKey = key.replace('aekos.org.au/collection', '')
  let dgResult = datasetGroupMapping[cleanedKey]
  if (dgResult) {
    return dgResult
  }
  let spgResult = samplingProtocolGroupMapping[cleanedKey]
  if (spgResult) {
    return spgResult
  }
  return fallbackName
}

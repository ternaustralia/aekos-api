'use strict'
let accepts = require('accepts')
let codeToLabelMapping = require('./ontology/code-to-label.json')
const jsonContentType = "'application/json'"
const csvContentType = "'text/csv'"
function getHeaders (contentType) {
  return {
    'Access-Control-Allow-Origin': '*', // Required for CORS support to work
    'Access-Control-Allow-Credentials': true,
    'Content-Type': contentType // Required for cookies, authorization headers with HTTPS
  }
}

function doResponse (theCallback, theBody, statusCode, contentType) {
  let body = contentType === csvContentType ? theBody : JSON.stringify(theBody)
  const response = {
    statusCode: statusCode,
    headers: getHeaders(contentType),
    body: body
  }
  theCallback(null, response)
}

function isQueryStringParamPresent (event, paramName) {
  let queryStringParams = event.queryStringParameters
  if (!queryStringParams || typeof (queryStringParams[paramName]) === 'undefined') {
    return false
  }
  return true
}

function getOptionalParam (event, paramName, defaultValue) {
  if (!isQueryStringParamPresent(event, paramName)) {
    return defaultValue
  }
  return event.queryStringParameters[paramName]
}

function calculateOffset (pageNum, pageSize) {
  return (pageNum - 1) * pageSize
}

function assertNumber (val) {
  if (typeof (val) === 'number') {
    return true
  }
  return false
}

const contentTypes = {
  json: 'json',
  csv: 'csv',
  unhandled: 'UNHANDLED'
}

function getContentType (event) {
  let accept = accepts(castEventToFakeExpressReq(event))
  switch (accept.type(['application/json', 'text/csv'])) {
    case 'application/json':
      return contentTypes.json
    case 'text/csv':
      return contentTypes.csv
    default:
      return contentTypes.unhandled
  }
}

function castEventToFakeExpressReq (event) {
  return {
    headers: {
      accept: event.headers.Accept
    }
  }
}

function resolveVocabCode (code) {
  let result = codeToLabelMapping[code]
  if (typeof result === 'undefined') {
    console.warn(`Data problem: no label defined for the code '${code}', reverting to the code`)
    result = code
  }
  return result
}

module.exports = {
  json: {
    ok: (theCallback, theBody) => {
      doResponse(theCallback, theBody, 200, jsonContentType)
    },
    badRequest: (theCallback, theMessage) => {
      doResponse(theCallback, theMessage, 400, jsonContentType)
    },
    internalServerError: (theCallback, theMessage) => {
      doResponse(theCallback, theMessage, 500, jsonContentType)
    }
  },
  csv: {
    ok: (theCallback, theBody) => {
      doResponse(theCallback, theBody, 200, csvContentType)
    }
  },
  getContentType: getContentType,
  isQueryStringParamPresent: isQueryStringParamPresent,
  assertNumber: assertNumber,
  getOptionalParam: getOptionalParam,
  calculateOffset: calculateOffset,
  resolveVocabCode: resolveVocabCode,
  now: () => {
    return new Date().getTime()
  },
  contentTypes: contentTypes
}

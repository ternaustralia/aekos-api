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

function getOptionalStringParam (event, paramName, defaultValue) {
  if (!isQueryStringParamPresent(event, paramName)) {
    return defaultValue
  }
  let rawValue = event.queryStringParameters[paramName]
  let valueType = typeof (rawValue)
  if (valueType === 'string') {
    return rawValue
  }
  throw new Error(`Data problem: supplied value '${rawValue}' of type '${valueType}' is not a string.`)
}

function getOptionalNumberParam (event, paramName, defaultValue) {
  if (!isQueryStringParamPresent(event, paramName)) {
    return defaultValue
  }
  let rawValue = event.queryStringParameters[paramName]
  let valueType = typeof (rawValue)
  if (valueType === 'number') {
    return rawValue
  }
  let parsedValue = parseInt(rawValue)
  if (isNaN(parsedValue)) {
    throw new Error(`Data problem: supplied value '${rawValue}' of type '${valueType}' is not a number.`)
  }
  return parsedValue
}

function calculateOffset (pageNum, pageSize) {
  return (pageNum - 1) * pageSize
}

function assertNumber (val) {
  let valType = typeof (val)
  if (valType === 'number') {
    return
  }
  throw new Error(`Data problem: expected value '${val}' of type '${valType}' to be a number`)
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

function calculatePageNumber (start, numFound, totalPages) {
  assertNumber(start)
  assertNumber(numFound)
  assertNumber(totalPages)
  if (numFound === 0) {
    return 0
  }
  if (start === 0) {
    return 1
  }
  let decimalProgress = (start + 1) / numFound
  let decimalPageNumber = decimalProgress * totalPages
  return Math.ceil(decimalPageNumber)
}

function calculateTotalPages (rows, numFound) {
  return Math.ceil(numFound / rows)
}

function assertIsSupplied (escapedSpeciesName) {
  if (!escapedSpeciesName) {
    throw new Error(`Programmer problem: no escaped species name was supplied='${escapedSpeciesName}'.`)
  }
}

function now () {
  return new Date().getTime()
}

function calculateElapsedTime (startMs) {
  return now() - startMs
}

function newContentNegotiationHandler (jsonHandler, csvHandler) {
  return function (event, context, callback) {
    let contentTypeIndicator = getContentType(event)
    switch (contentTypeIndicator) {
      case contentTypes.json:
        jsonHandler.handler(event, context, callback)
        break
      case contentTypes.csv:
        csvHandler.handler(event, context, callback)
        break
      case contentTypes.unhandled:
        jsonResponseHelpers.badRequest(callback, `Cannot handle the specified Accept header '${event.headers.Accept}`)
        break
      default:
        throw new Error(`Programmer error: unknown content type indicator returned '${contentTypeIndicator}'`)
    }
  }
}

const jsonResponseHelpers = {
  ok: (theCallback, theBody) => {
    doResponse(theCallback, theBody, 200, jsonContentType)
  },
  badRequest: (theCallback, theMessage) => {
    doResponse(theCallback, theMessage, 400, jsonContentType)
  },
  internalServerError: (theCallback, theMessage) => {
    doResponse(theCallback, { message: theMessage }, 500, jsonContentType)
  }
}

module.exports = {
  json: jsonResponseHelpers,
  csv: {
    ok: (theCallback, theBody) => {
      doResponse(theCallback, theBody, 200, csvContentType)
    }
  },
  getContentType: getContentType,
  isQueryStringParamPresent: isQueryStringParamPresent,
  assertNumber: assertNumber,
  assertIsSupplied: assertIsSupplied,
  getOptionalStringParam: getOptionalStringParam,
  getOptionalNumberParam: getOptionalNumberParam,
  calculateOffset: calculateOffset,
  resolveVocabCode: resolveVocabCode,
  now: now,
  calculateElapsedTime: calculateElapsedTime,
  contentTypes: contentTypes,
  calculatePageNumber: calculatePageNumber,
  calculateTotalPages: calculateTotalPages,
  newContentNegotiationHandler: newContentNegotiationHandler
}

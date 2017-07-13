'use strict'
let accepts = require('accepts')
let codeToLabelMapping = require('./ontology/code-to-label.json')
let querystring = require('querystring')
const jsonContentType = "'application/json'"
const csvContentType = "'text/csv'"
function getHeaders (contentType) {
  return {
    'Access-Control-Allow-Origin': '*', // Required for CORS support to work
    'Access-Control-Allow-Credentials': true,
    'Content-Type': contentType // Required for cookies, authorization headers with HTTPS
  }
}

function doResponse (theCallback, theBody, statusCode, contentType, extraHeadersCallback) {
  let body = contentType === csvContentType ? theBody : JSON.stringify(theBody)
  let headers = getHeaders(contentType)
  if (extraHeadersCallback && isHateoasable(theBody)) {
    extraHeadersCallback(headers)
  }
  const response = {
    statusCode: statusCode,
    headers: headers,
    body: body
  }
  theCallback(null, response)
}

function handlePost (event, callback, db,
    validator/* (requestBody):{isValid:boolean, message:string} */,
    responder/* (requestBody, databaseHelper):Promise<{}> */) {
  let requestBody = JSON.parse(event.body)
  let validationResult = validator(requestBody)
  if (!validationResult.isValid) {
    jsonResponseHelpers.badRequest(callback, validationResult.message)
    return
  }
  let errorHandler = error => {
    console.error('Failed to execute post handler', error)
    jsonResponseHelpers.internalServerError(callback, 'Sorry, something went wrong')
  }
  try {
    responder(requestBody, db).then(responseBody => {
      jsonResponseHelpers.ok(callback, responseBody)
    }).catch(errorHandler)
  } catch (error) {
    errorHandler(error)
  }
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

function buildHateoasLinkHeader (event, responseHeader) {
  function appendCommaIfNecessary (linkHeader) {
    if (linkHeader.length > 0) {
      return linkHeader + ', '
    }
    return linkHeader
  }

  function createLinkHeader (uri, rel) {
    return '<' + uri + '>; rel="' + rel + '"'
  }

  function queryStringWithStart (newStart) {
    let params = event.queryStringParameters
    params.start = newStart
    return querystring.stringify(params)
  }
  // FIXME should handle if expected values aren't available
  let start = responseHeader.params.start
  let rows = responseHeader.params.rows
  let pageNumber = responseHeader.pageNumber
  let totalPages = responseHeader.totalPages
  let scheme = event.headers['X-Forwarded-Proto']
  let host = event.headers.Host
  let path = event.requestContext.path
  let fromPath = `${scheme}://${host}${path}`
  let result = ''
  let hasNextPage = pageNumber < totalPages
  if (hasNextPage) {
    let startForNextPage = start + rows
    let qs = queryStringWithStart(startForNextPage)
    let uriForNextPage = `${fromPath}?${qs}`
    result += createLinkHeader(uriForNextPage, 'next')
  }
  let hasPrevPage = pageNumber > 1
  if (hasPrevPage) {
    let startForPrevPage = start - rows
    let qs = queryStringWithStart(startForPrevPage)
    let uriForPrevPage = `${fromPath}?${qs}`
    result = appendCommaIfNecessary(result)
    result += createLinkHeader(uriForPrevPage, 'prev')
  }
  let hasFirstPage = pageNumber > 1
  if (hasFirstPage) {
    let qs = queryStringWithStart(0)
    let uriForFirstPage = `${fromPath}?${qs}`
    result = appendCommaIfNecessary(result)
    result += createLinkHeader(uriForFirstPage, 'first')
  }
  let hasLastPage = pageNumber < totalPages
  if (hasLastPage) {
    let startForLastPage = (totalPages - 1) * rows
    let qs = queryStringWithStart(startForLastPage)
    let uriForLastPage = `${fromPath}?${qs}`
    result = appendCommaIfNecessary(result)
    result += createLinkHeader(uriForLastPage, 'last')
  }
  return result
}

function isHateoasable (response) {
  if (typeof response !== 'object' || typeof response.responseHeader !== 'object') {
    return false
  }
  let responseHeader = response.responseHeader
  if (typeof responseHeader.pageNumber === 'number' &&
      responseHeader.params &&
      typeof responseHeader.params.rows === 'number' &&
      typeof responseHeader.params.start === 'number' &&
      typeof responseHeader.totalPages === 'number') {
    return true
  }
  return false
}

const jsonResponseHelpers = {
  ok: (theCallback, theBody, event) => {
    let extraHeadersCallback = function () {}
    if (isHateoasable(theBody)) {
      extraHeadersCallback = headers => {
        headers.link = buildHateoasLinkHeader(event, theBody.responseHeader)
      }
    }
    doResponse(theCallback, theBody, 200, jsonContentType, extraHeadersCallback)
  },
  badRequest: (theCallback, theMessage) => {
    doResponse(theCallback, { message: theMessage }, 400, jsonContentType)
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
  newContentNegotiationHandler: newContentNegotiationHandler,
  handlePost: handlePost,
  buildHateoasLinkHeader: buildHateoasLinkHeader,
  isHateoasable: isHateoasable
}

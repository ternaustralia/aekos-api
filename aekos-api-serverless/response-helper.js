'use strict'
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
  isQueryStringParamPresent: isQueryStringParamPresent,
  assertNumber: assertNumber,
  getOptionalParam: getOptionalParam,
  calculateOffset: calculateOffset,
  now: () => {
    return new Date().getTime()
  }
}

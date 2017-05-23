'use strict'

const corsHeaders = {
  'Access-Control-Allow-Origin': '*', // Required for CORS support to work
  'Access-Control-Allow-Credentials' : true // Required for cookies, authorization headers with HTTPS
}

function doResponse (theCallback, theBody, statusCode) {
  const response = {
    statusCode: statusCode,
    headers: corsHeaders,
    body: JSON.stringify(theBody)
  }
  theCallback(null, response)
}

function do200 (theCallback, theBody) {
  doResponse(theCallback, theBody, 200)
}

function do400 (theCallback, theMessage) {
  doResponse(theCallback, theMessage, 400)
}

function isQueryStringParamPresent (event, paramName) {
  let queryStringParams = event.queryStringParameters
  if (!queryStringParams || typeof(queryStringParams[paramName]) === 'undefined') {
    return false
  }
  return true
}

module.exports = {
  ok: do200,
  badRequest: do400,
  isQueryStringParamPresent: isQueryStringParamPresent
}
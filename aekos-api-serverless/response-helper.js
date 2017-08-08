'use strict'
let accepts = require('accepts')
let yaml = require('yamljs')
const speciesNamesParam = yaml.load('./constants.yml').paramNames.speciesName.multiple
const traitNamesParam = yaml.load('./constants.yml').paramNames.traitName.multiple
const pageSizeParam = yaml.load('./constants.yml').paramNames.PAGE_SIZE
const pageNumParam = yaml.load('./constants.yml').paramNames.PAGE_NUM
const startParam = yaml.load('./constants.yml').paramNames.START
const rowsParam = yaml.load('./constants.yml').paramNames.ROWS
const msg500 = yaml.load('./constants.yml').messages.public.internalServerError
let codeToLabelMapping = require('./ontology/code-to-label.json')
let querystring = require('querystring')
const jsonContentType = "'application/json'"
const csvContentType = "'text/csv'"
function getHeaders (contentType) {
  return {
    'Access-Control-Allow-Origin': '*', // Required for CORS support to work
    'Access-Control-Allow-Credentials': true,
    'Access-Control-Expose-Headers': 'link',
    'Content-Type': contentType // Required for cookies, authorization headers with HTTPS
  }
}

function doResponse (theCallback, theBody, statusCode, contentType, extraHeadersCallback) {
  let body = contentType === csvContentType ? theBody : JSON.stringify(theBody)
  let headers = getHeaders(contentType)
  if (extraHeadersCallback) {
    extraHeadersCallback(headers)
  }
  const response = {
    statusCode: statusCode,
    headers: headers,
    body: body
  }
  theCallback(null, response)
}

function handleJsonPost (event, callback, db,
    validator/* (queryStringParameters, requestBody):{isValid:boolean, message:string} */,
    responder/* (requestBody, databaseHelper, queryStringParameters):Promise<{}> */,
    extrasProvider) {
  if (typeof event.body === 'undefined') {
    jsonResponseHelpers.badRequest(callback, 'Programmer problem: request body is undefined. Most likely you ' +
      "haven't defined the test input or wired things up correctly. Or maybe AWS has changed the event object")
    return
  }
  let requestBody = JSON.parse(event.body)
  let queryStringObj = event.queryStringParameters
  let validationResult = validator(queryStringObj, requestBody)
  if (!validationResult.isValid) {
    jsonResponseHelpers.badRequest(callback, validationResult.message)
    return
  }
  let errorHandler = error => {
    console.error('Failed to execute POST handler', error)
    jsonResponseHelpers.internalServerError(callback)
  }
  try {
    responder(requestBody, db, queryStringObj, extrasProvider).then(responseBody => {
      jsonResponseHelpers.ok(callback, responseBody, event)
    }).catch(errorHandler)
  } catch (error) {
    errorHandler(error)
  }
}

function handleCsvPost (event, callback, db,
    validator/* (queryStringParameters, requestBody):{isValid:boolean, message:string} */,
    responder/* (requestBody, databaseHelper, queryStringParameters):Promise<{}> */,
    extrasProvider) {
  let requestBody = JSON.parse(event.body)
  let queryStringObj = event.queryStringParameters
  let validationResult = validator(queryStringObj, requestBody)
  if (!validationResult.isValid) {
    jsonResponseHelpers.badRequest(callback, validationResult.message)
    return
  }
  let errorHandler = error => {
    console.error('Failed to execute POST handler', error)
    jsonResponseHelpers.internalServerError(callback)
  }
  try {
    responder(requestBody, db, queryStringObj, extrasProvider).then(responseWrapper => {
      csvResponseHelpers.ok(callback, responseWrapper.body, responseWrapper.downloadFileName, event, responseWrapper.jsonResponse)
    }).catch(errorHandler)
  } catch (error) {
    errorHandler(error)
  }
}

function handleJsonGet (event, callback, db,
    validator/* (queryStringParameters):{isValid:boolean, message:string} */,
    responder/* (databaseHelper, queryStringParameters):Promise<{}> */,
    extrasProvider) {
  let queryStringObj = event.queryStringParameters
  let validationResult = validator(queryStringObj)
  if (!validationResult.isValid) {
    jsonResponseHelpers.badRequest(callback, validationResult.message)
    return
  }
  let errorHandler = error => {
    console.error('Failed to execute GET handler', error)
    jsonResponseHelpers.internalServerError(callback)
  }
  try {
    responder(db, queryStringObj, extrasProvider).then(responseBody => {
      jsonResponseHelpers.ok(callback, responseBody, event)
    }).catch(errorHandler)
  } catch (error) {
    errorHandler(error)
  }
}

function handleCsvGet (event, callback, db,
    validator/* (queryStringParameters):{isValid:boolean, message:string} */,
    responder/* (databaseHelper, queryStringParameters):Promise<{}> */,
    extrasProvider) {
  let queryStringObj = event.queryStringParameters
  let validationResult = validator(queryStringObj)
  if (!validationResult.isValid) {
    jsonResponseHelpers.badRequest(callback, validationResult.message)
    return
  }
  let errorHandler = error => {
    console.error('Failed to execute GET handler', error)
    jsonResponseHelpers.internalServerError(callback)
  }
  try {
    responder(db, queryStringObj, extrasProvider).then(responseWrapper => {
      csvResponseHelpers.ok(callback, responseWrapper.body, responseWrapper.downloadFileName, event, responseWrapper.jsonResponse)
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
  return getOptionalNumber(event.queryStringParameters, paramName, defaultValue)
}

function getOptionalNumber (containerObj, paramName, defaultValue) {
  let isValueNotPresent = !containerObj || typeof containerObj[paramName] === 'undefined'
  if (isValueNotPresent) {
    return parseInt(defaultValue)
  }
  let rawValue = containerObj[paramName]
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

function getOptionalArray (containerObj, key, db) {
  let unescapedResult = containerObj[key]
  if (unescapedResult && unescapedResult.constructor !== Array) {
    throw new Error(`Programmer problem: the '${key}' field (value='${unescapedResult}') is not an array, this should've been caught by validation`)
  }
  if (!unescapedResult) {
    unescapedResult = null
  }
  let escapedResult = null
  if (unescapedResult) {
    escapedResult = db.toSqlList(unescapedResult)
  }
  return {
    escaped: escapedResult,
    unescaped: unescapedResult
  }
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

function assertIsSupplied (escapedSpeciesNames) {
  if (!escapedSpeciesNames) {
    throw new Error(`Programmer problem: no escaped species names were supplied='${escapedSpeciesNames}'.`)
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
    let urlSuffix = getUrlSuffix(event)
    let contentTypeIndicator
    switch (urlSuffix) {
      case 'json':
        contentTypeIndicator = contentTypes.json
        break
      case 'csv':
        contentTypeIndicator = contentTypes.csv
        break
      default:
        contentTypeIndicator = getContentType(event)
        break
    }
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

function getUrlSuffix (event) {
  if (!event || !event.path) {
    return null
  }
  let path = event.path
  let regex = /\.\w+$/
  let match = regex.exec(path)
  if (!match) {
    return null
  }
  return match[0].replace('.', '')
}

function newVersionHandler (config) {
  if (typeof config !== 'object') {
    throw new Error('Programmer problem: supplied config is not an object')
  }
  return new VersionHandler(config)
}

class VersionHandler {
  constructor (config) {
    this._versionPrefixLength = 4
    this._config = config
  }

  handle (event) {
    let path = event.requestContext.path
    let isStageInPath = /^\/\w+\/v\d\//.test(path)
    let pathWithoutStage = path
    if (isStageInPath) {
      pathWithoutStage = path.replace(/\/\w+/, '')
    }
    let versionPrefix = pathWithoutStage.substr(0, this._versionPrefixLength)
    let result = this._config[versionPrefix]
    if (result) {
      return result
    }
    throw new Error(`Programmer problem: unhandled path prefix '${versionPrefix}' extracted from path '${path}'`)
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
    let params = event.queryStringParameters || {}
    params.start = newStart
    return querystring.stringify(params)
  }
  if (typeof event === 'undefined') {
    throw new Error('Programmer problem: event was not supplied')
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
  return typeof responseHeader.pageNumber === 'number' &&
    responseHeader.params &&
    typeof responseHeader.params.rows === 'number' &&
    typeof responseHeader.params.start === 'number' &&
    typeof responseHeader.totalPages === 'number'
}

const validatorIsValid = { isValid: true }
const validatorNotValid = message => {
  return { isValid: false, message: message }
}

function speciesNamesValidator (_, requestBody) {
  return genericMandatoryNamesValidator(speciesNamesParam, _, requestBody)
}

function traitNamesMandatoryValidator (_, requestBody) {
  return genericMandatoryNamesValidator(traitNamesParam, _, requestBody)
}

// Validates that the target property MUST be present, is the right type, has items and
// the items are the right type
function genericMandatoryNamesValidator (namesParam, _, requestBody) {
  let isRequestBodyNotSupplied = requestBody === null || typeof requestBody === 'undefined'
  if (isRequestBodyNotSupplied) {
    return validatorNotValid('No request body was supplied')
  }
  let names = requestBody[namesParam]
  let isFieldNotSupplied = typeof names === 'undefined'
  if (isFieldNotSupplied) {
    return validatorNotValid(`The '${namesParam}' field was not supplied`)
  }
  let isFieldNotArray = names.constructor !== Array
  if (isFieldNotArray) {
    return validatorNotValid(`The '${namesParam}' field must be an array (of strings)`)
  }
  let isArrayEmpty = names.length < 1
  if (isArrayEmpty) {
    return validatorNotValid(`The '${namesParam}' field is mandatory and was not supplied`)
  }
  let isAnyElementNotStrings = names.some(element => { return typeof element !== 'string' })
  if (isAnyElementNotStrings) {
    return validatorNotValid(`The '${namesParam}' field must be an array of strings. You supplied a non-string element.`)
  }
  return validatorIsValid
}

function traitNamesOptionalValidator (_, requestBody) {
  return genericOptionalNamesValidator(traitNamesParam, _, requestBody)
}

// Validates that IF the target property is present then it is the right type and
// if it has items that the items are the right type
function genericOptionalNamesValidator (namesParam, _, requestBody) {
  let isRequestBodyNotSupplied = requestBody === null || typeof requestBody === 'undefined'
  if (isRequestBodyNotSupplied) {
    return validatorIsValid
  }
  let names = requestBody[namesParam]
  let isFieldNotSupplied = typeof names === 'undefined'
  if (isFieldNotSupplied) {
    return validatorIsValid
  }
  let isFieldNotArray = names.constructor !== Array
  if (isFieldNotArray) {
    return validatorNotValid(`The '${namesParam}' field must be an array (of strings)`)
  }
  let isArrayEmpty = names.length < 1
  if (isArrayEmpty) {
    return validatorIsValid
  }
  let isAnyElementNotStrings = names.some(element => { return typeof element !== 'string' })
  if (isAnyElementNotStrings) {
    return validatorNotValid(`The '${namesParam}' field must be an array of strings. You supplied a non-string element.`)
  }
  return validatorIsValid
}

function compositeValidator (validatorArray) {
  return (queryStringObj, requestBody) => {
    for (let i = 0; i < validatorArray.length; i++) {
      let currValidator = validatorArray[i]
      let currResult = currValidator(queryStringObj, requestBody)
      if (!currResult.isValid) {
        return currResult
      }
    }
    return validatorIsValid
  }
}

function queryStringParamIsPositiveNumberIfPresentValidator (paramName) {
  return (queryStringObj, _) => {
    if (queryStringObj === null || typeof queryStringObj === 'undefined') {
      return validatorIsValid
    }
    let value = queryStringObj[paramName]
    if (typeof value === 'undefined') {
      return validatorIsValid
    }
    let parsedValue = parseInt(value)
    if (isNaN(parsedValue)) {
      return validatorNotValid(`The '${paramName}' must be a number when supplied. Supplied value = '${value}'`)
    }
    if (parsedValue < 0) {
      return validatorNotValid(`The '${paramName}' must be a number >= 0. Supplied value = '${value}'`)
    }
    return validatorIsValid
  }
}

const jsonResponseHelpers = {
  ok: (theCallback, theBody, event) => {
    let extraHeadersCallback = function () { }
    if (isHateoasable(theBody)) {
      extraHeadersCallback = headers => {
        headers.link = buildHateoasLinkHeader(event, theBody.responseHeader)
      }
    }
    doResponse(theCallback, theBody, 200, jsonContentType, extraHeadersCallback)
  },
  badRequest: (theCallback, theMessage) => {
    let statusCode = 400
    let body = {
      message: theMessage,
      statusCode: statusCode
    }
    doResponse(theCallback, body, statusCode, jsonContentType)
  },
  internalServerError: (theCallback) => {
    let statusCode = 500
    let body = {
      message: msg500,
      statusCode: statusCode
    }
    doResponse(theCallback, body, statusCode, jsonContentType)
  }
}

const csvResponseHelpers = {
  ok: (theCallback, theBody, downloadFileName, event, dataForHateoas) => {
    let extraHeadersCallback = headers => {
      if (typeof downloadFileName !== 'undefined' && downloadFileName !== null) {
        headers['Content-Disposition'] = `attachment;filename=${downloadFileName}`
      }
      if (isHateoasable(dataForHateoas)) {
        headers.link = buildHateoasLinkHeader(event, dataForHateoas.responseHeader)
      }
    }
    doResponse(theCallback, theBody, 200, csvContentType, extraHeadersCallback)
  }
}

module.exports = {
  json: jsonResponseHelpers,
  csv: csvResponseHelpers,
  getContentType: getContentType,
  isQueryStringParamPresent: isQueryStringParamPresent,
  assertNumber: assertNumber,
  assertIsSupplied: assertIsSupplied,
  getOptionalStringParam: getOptionalStringParam,
  getOptionalNumberParam: getOptionalNumberParam,
  getOptionalNumber: getOptionalNumber,
  getOptionalArray: getOptionalArray,
  calculateOffset: calculateOffset,
  resolveVocabCode: resolveVocabCode,
  now: now,
  calculateElapsedTime: calculateElapsedTime,
  contentTypes: contentTypes,
  calculatePageNumber: calculatePageNumber,
  calculateTotalPages: calculateTotalPages,
  newContentNegotiationHandler: newContentNegotiationHandler,
  handleJsonPost: handleJsonPost,
  handleCsvPost: handleCsvPost,
  handleJsonGet: handleJsonGet,
  handleCsvGet: handleCsvGet,
  buildHateoasLinkHeader: buildHateoasLinkHeader,
  isHateoasable: isHateoasable,
  speciesNamesValidator: speciesNamesValidator,
  traitNamesMandatoryValidator: traitNamesMandatoryValidator,
  traitNamesOptionalValidator: traitNamesOptionalValidator,
  compositeValidator: compositeValidator,
  queryStringParamIsPositiveNumberIfPresentValidator: queryStringParamIsPositiveNumberIfPresentValidator,
  pageSizeValidator: queryStringParamIsPositiveNumberIfPresentValidator(pageSizeParam),
  pageNumValidator: queryStringParamIsPositiveNumberIfPresentValidator(pageNumParam),
  startValidator: queryStringParamIsPositiveNumberIfPresentValidator(startParam),
  rowsValidator: queryStringParamIsPositiveNumberIfPresentValidator(rowsParam),
  newVersionHandler: newVersionHandler,
  _testonly: {
    genericMandatoryNamesValidator: genericMandatoryNamesValidator,
    genericOptionalNamesValidator: genericOptionalNamesValidator,
    getUrlSuffix: getUrlSuffix
  }
}

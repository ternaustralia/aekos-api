'use strict'
let r = require('./response-helper')

module.exports.doHandle = doHandle
function doHandle (event, callback, _, _2) {
  let scheme = event.headers['X-Forwarded-Proto']
  let host = event.headers.Host
  let fromPath = `${scheme}://${host}`
  let linksArray = buildLinkObject(fromPath)
  let linkHeader = buildLinkHeader(linksArray)
  let response = {
    statusCode: 200,
    headers: {
      'Access-Control-Allow-Origin': '*',
      'Access-Control-Allow-Credentials': true,
      'Access-Control-Expose-Headers': 'link',
      'Content-Type': "'application/json'",
      link: linkHeader
    },
    body: JSON.stringify(
      {links: linksArray}
    )
  }
  callback(null, response)
}

const thingsToLink = {
  'x-trait-vocab': '/v2/getTraitVocab.json',
  'x-env-vocab': '/v2/getEnvironmentalVariableVocab.json'
}

function buildLinkHeader (linkParts) {
  return r.buildRfc5988Link(linkParts)
}

function buildLinkObject (pathPrefix) {
  return Object.keys(thingsToLink).reduce((previous, current) => {
    let rel = current
    let hrefFragment = thingsToLink[rel]
    previous.push(r.rfc5988LinkPart(pathPrefix + hrefFragment, rel))
    return previous
  }, [])
}

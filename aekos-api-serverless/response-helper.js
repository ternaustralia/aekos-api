'use strict'

function do200(theCallback, theBody) {
  const response = {
    statusCode: 200,
    headers: {
      'Access-Control-Allow-Origin': '*', // Required for CORS support to work
    },
    body: JSON.stringify(theBody)
  }
  theCallback(null, response);
}

module.exports = {
  ok: do200
}
'use strict'

module.exports.handler = (event, context, callback) => {
  let documentationDomainName = 'http://www.' + process.env.DOMAIN_NAME
  callback(null, {
    statusCode: '301',
    headers: {
      Location: documentationDomainName
    },
    body: JSON.stringify(`
      <html><body>
        <p>Redirecting to documentation: <a href="${documentationDomainName}">${documentationDomainName}</a>
      </body></html>`)
  })
}

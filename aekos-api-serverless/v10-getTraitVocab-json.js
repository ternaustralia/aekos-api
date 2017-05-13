'use strict';

module.exports.handler = (event, context, callback) => {
  const response = {
    statusCode: 200,
    headers: {
      'Access-Control-Allow-Origin': '*', // Required for CORS support to work
    },
    body: JSON.stringify([
      {
        "code": "averageHeight",
        "label": "Average Height"
      },
      {
        "code": "basalArea",
        "label": "Basal Area"
      },
      {
        "code": "basalAreaCount",
        "label": "Basal Area Count"
      }
    ])
  };

  callback(null, response);
};

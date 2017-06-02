let r = require('./response-helper')
module.exports.handler = (event, context, callback) => {
    // r.ok(callback, JSON.parse(event.body))
    r.ok(callback, event.body)
}

// ## Model
// -
//         name: "TestPostRequest"
//         contentType: "application/json"
//         schema:
//           type: array
//           items:
//             type: string

// test-post:
//     handler: test-post.handler
//     events:
//       - http:
//           path: v1/test-post
//           method: post
//           cors: true
//           documentation:
//             summary: "POST test"
//             description: |
//               Testing if we can post stuff easier
//             requestModels:
//               "application/json": "TestPostRequest"
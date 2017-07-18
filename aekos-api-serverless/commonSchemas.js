module.exports.vocabSchema = () => {
  return {
    '$schema': 'http://json-schema.org/draft-04/schema#',
    'additionalItems': false,
    'items': {
      'additionalProperties': false,
      'properties': {
        'code': {
          'type': 'string'
        },
        'label': {
          'type': 'string'
        },
        'recordsHeld': {
          'type': 'integer'
        }
      },
      'required': [
        'recordsHeld',
        'code',
        'label'
      ],
      'type': 'object'
    },
    'type': 'array'
  }
}

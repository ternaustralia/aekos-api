'use strict'
let objectUnderTest = require('../v1-traitData-json')

describe('v1-traitData-json', () => {
  describe('extractParams', () => {
    it('should extract the params when they are present', () => {
      let event = {
        queryStringParameters: {
          speciesName: 'species one',
          traitName: 'trait one',
          rows: '15',
          start: '0'
        }
      }
      let result = objectUnderTest.extractParams(event)
      expect(result.speciesName).toBe("'species one'")
      expect(result.unescapedSpeciesName).toBe('species one')
      expect(result.traitName).toBe("'trait one'")
      expect(result.unescapedTraitName).toBe('trait one')
      expect(result.rows).toBe(15)
      expect(result.start).toBe(0)
    })

    it('should default the paging info', () => {
      let event = {
        queryStringParameters: {
          speciesName: 'species two'
        }
      }
      let result = objectUnderTest.extractParams(event)
      expect(result.speciesName).toBe("'species two'")
      expect(result.unescapedSpeciesName).toBe('species two')
      expect(result.traitName).toBeNull()
      expect(result.unescapedTraitName).toBeNull()
      expect(result.rows).toBe(20)
      expect(result.start).toBe(0)
    })
  })

  let expectedTraitSql1 = `
    SELECT
    traitName,
    traitValue,
    traitUnit
    FROM traits
    WHERE parentId = 'some-parent-id123'
    AND traitName in ('trait one');`
  let expectedTraitSql2 = `
    SELECT
    traitName,
    traitValue,
    traitUnit
    FROM traits
    WHERE parentId = 'some-parent-id123'
    ;`
  describe('getTraitSql', () => {
    it('should be able to handle a single trait', () => {
      let result = objectUnderTest._testonly.getTraitSql('some-parent-id123', "'trait one'")
      expect(result).toBe(expectedTraitSql1)
    })

    it('should throw an error when we do not supply a parent ID', () => {
      let undefinedParentId
      expect(() => {
        objectUnderTest._testonly.getTraitSql(undefinedParentId, "'trait one'")
      }).toThrow()
    })

    it('should be able to handle when no trait filter is applied', () => {
      let noTraits = null
      let result = objectUnderTest._testonly.getTraitSql('some-parent-id123', noTraits)
      expect(result).toBe(expectedTraitSql2)
    })
  })
})

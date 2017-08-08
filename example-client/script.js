var app = angular.module('aekosapiexample', [])

app.controller('TheController', function ($http, $scope) {
  $scope.baseUrl = 'https://k2jqcign2l.execute-api.us-west-1.amazonaws.com/test'
  var speciesPageSize = 100
  $scope.traitDataAccept = 'application/json'

  $scope.isLoadVocabClicked = false
  $scope.loadVocabs = function () {
    $scope.traits = []
    resetStep2()
    resetStep3()
    $scope.isLoadVocabClicked = true
    $scope.isLoadingTraits = true
    $http.get($scope.baseUrl + '/v1/getTraitVocab.json').then(function (response) {
      $scope.isLoadingTraits = false
      $scope.traits = response.data
    }, errorHandler)
  }

  function resetStep2() {
    $scope.species = []
    $scope.isLoadSpeciesClicked = false
  }

  $scope.findSpecies = function () {
    $scope.speciesPaging = {
      pageNum: 1,
      pageSize: speciesPageSize
    }
    speciesSearchHelper()
  }
  $scope.speciesNextPage = function () {
    var nextPageNum = $scope.speciesPaging.pageNum + 1
    $scope.speciesPaging = {
      pageNum: nextPageNum,
      pageSize: speciesPageSize
    }
    speciesSearchHelper()
  }

  function speciesSearchHelper () {
    resetStep2()
    resetStep3()
    var traitNames = getTraits($scope.traits, 'code')
    if (traitNames.length === 0) {
      alert('[ERROR] you need to select at least one trait from step 1 first')
      return
    }
    $scope.isLoadSpeciesClicked = true
    $scope.isLoadingSpecies = true
    var data = {
      traitNames: traitNames
    }
    var config = {
      params: {
        pageNum: $scope.speciesPaging.pageNum,
        pageSize: $scope.speciesPaging.pageSize
      }
    }
    $http.post($scope.baseUrl + '/v1/getSpeciesByTrait.json', data, config).then(function (response) {
      $scope.isLoadingSpecies = false
      $scope.species = response.data
    }, errorHandler)
  }

  function resetStep3() {
    $scope.traitRecords = []
    $scope.traitRecordsHeader = {}
    $scope.isLoadTraitDataClicked = false
  }

  $scope.getTraitData = function () {
    traitDataHelper($scope.baseUrl + '/v2/traitData')
  }

  $scope.getTraitData = function () {
    traitDataHelper($scope.baseUrl + '/v2/traitData')
  }

  $scope.traitDataChangePage = function (url) {
    traitDataHelper(url)
  }

  $scope.prettyTraitDataJson = function () {
    return JSON.stringify($scope.traitRecords, null, 2)
  }

  function traitDataHelper (url) {
    resetStep3()
    var speciesNames = getSpeciesNames($scope.species)
    if (speciesNames.length === 0) {
      alert('[ERROR] you need to select at least one species name from step 2 first')
      return
    }
    $scope.isLoadTraitDataClicked = true
    $scope.isLoadingTraitData = true
    var traitNames = []
    if ($scope.isApplyTraitFilter) {
      traitNames = getTraits($scope.traits, 'code')
    }
    var data = {
      speciesNames: speciesNames,
      traitNames: traitNames
    }
    var config = {
      headers: {
        Accept: $scope.traitDataAccept
      }
    }
    $http.post(url, data, config).then(function (response) {
      $scope.isLoadingTraitData = false
      var acceptHeader = $scope.traitDataAccept
      switch (acceptHeader) {
        case 'application/json':
          $scope.traitRecords = response.data.response
          $scope.traitRecordsHeader = response.data.responseHeader
          $scope.traitDataResultWindow = 'json'
          break
        case 'text/csv':
          $scope.traitRecords = response.data
          $scope.traitDataResultWindow = 'csv'
          break
        default:
          throw new Error('Programmer problem: unhandled accept header=' + acceptHeader)
      }
      // uses wombleton/link-headers so we don't hardcode paging URLs
      var rawLinkHeader = response.headers('link')
      var parsedLinkHeader = $.linkheaders(rawLinkHeader)
      $scope.traitDataFirstPageUrl = getLink(parsedLinkHeader, 'first')
      $scope.traitDataPrevPageUrl = getLink(parsedLinkHeader, 'prev')
      $scope.traitDataNextPageUrl = getLink(parsedLinkHeader, 'next')
      $scope.traitDataLastPageUrl = getLink(parsedLinkHeader, 'last')
    }, errorHandler)
  }
})

app.filter('filterSelected', function () {
  return function (items) {
    var items = items || []
    var result = []
    items.forEach(function (e) {
      if (e.isChecked) {
        result.push(e)
      }
    })
    return result
  }
})

function getLink (parsedLinkHeader, relName) {
  var value = parsedLinkHeader.find(relName)
  if (value === null) {
    return null
  }
  return value.resolve()
}

function getTraits(traits, fieldToReturn) {
  var result = []
  traits.forEach(function (curr) {
    if (!curr.isChecked) {
      return
    }
    result.push(curr[fieldToReturn])
  })
  return result
}

function getSpeciesNames(species) {
  var result = []
  species.forEach(function (curr) {
    if (!curr.isChecked) {
      return
    }
    result.push(curr.name)
  })
  return result
}

function errorHandler(error) {
  var msg = 'Something went wrong:'
  alert(msg + error.message)
  throw new Error(msg, error)
}
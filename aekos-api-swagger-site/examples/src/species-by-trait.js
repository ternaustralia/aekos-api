var app = angular.module('aekosapiexample')

app.config(function ($urlRouterProvider, $stateProvider) {
  $stateProvider.state({
    name: 'exampleParent.speciesByTrait',
    url: '/species-by-trait',
    component: 'speciesByTrait'
  })
})

app.component('speciesByTrait', {
  templateUrl: './src/species-by-trait.html',
  controller: 'SpeciesByTraitController'
})

app.controller('SpeciesByTraitController', function ($http, $scope, baseUrlService, commonHelpersService) {
  var speciesPageSize = 100
  $scope.traitDataAccept = 'application/json'

  $scope.isLoadVocabClicked = false
  $scope.loadVocabs = function () {
    $scope.traits = []
    resetStep2()
    resetStep3()
    $scope.isLoadVocabClicked = true
    $scope.isLoadingTraits = true
    $http.get(baseUrlService.getBaseUrl() + '/v2/getTraitVocab.json').then(function (response) {
      $scope.isLoadingTraits = false
      $scope.traits = response.data
    }, errorHandler)
  }

  function resetStep2 () {
    $scope.species = []
    $scope.isLoadSpeciesClicked = false
  }

  $scope.findSpecies = function () {
    speciesSearchHelper(baseUrlService.getBaseUrl() + '/v2/getSpeciesByTrait.json')
  }

  $scope.speciesSearchChangePage = function (url) {
    speciesSearchHelper(url)
  }

  function speciesSearchHelper (url) {
    resetStep2()
    resetStep3()
    var traitNames = commonHelpersService.getChecked($scope.traits, 'code')
    if (traitNames.length === 0) {
      alert('[ERROR] you need to select at least one trait from step 1 first')
      return
    }
    $scope.isLoadSpeciesClicked = true
    $scope.isLoadingSpecies = true
    var data = {
      traitNames: traitNames
    }
    $http.post(url, data).then(function (response) {
      $scope.isLoadingSpecies = false
      $scope.species = response.data
      // uses wombleton/link-headers to pull apart the 'link' header so we don't hardcode paging URLs
      var rawLinkHeader = response.headers('link')
      var parsedLinkHeader = $.linkheaders(rawLinkHeader)
      $scope.speciesSearchFirstPageUrl = commonHelpersService.getLink(parsedLinkHeader, 'first')
      $scope.speciesSearchPrevPageUrl = commonHelpersService.getLink(parsedLinkHeader, 'prev')
      $scope.speciesSearchNextPageUrl = commonHelpersService.getLink(parsedLinkHeader, 'next')
      $scope.speciesSearchLastPageUrl = commonHelpersService.getLink(parsedLinkHeader, 'last')
    }, errorHandler)
  }

  $scope.isApplyTraitFilter = true
  function resetStep3 () {
    $scope.traitRecords = []
    $scope.traitRecordsHeader = {}
    $scope.isLoadTraitDataClicked = false
  }

  $scope.getTraitData = function () {
    traitDataHelper(baseUrlService.getBaseUrl() + '/v2/traitData')
  }

  $scope.traitDataChangePage = function (url) {
    traitDataHelper(url)
  }

  $scope.prettyTraitDataJson = function () {
    return JSON.stringify($scope.traitRecords, null, 2)
  }

  function traitDataHelper (url) {
    resetStep3()
    var speciesNames = commonHelpersService.getChecked($scope.species, 'name')
    if (speciesNames.length === 0) {
      alert('[ERROR] you need to select at least one species name from step 2 first')
      return
    }
    $scope.isLoadTraitDataClicked = true
    $scope.isLoadingTraitData = true
    var traitNames = []
    if ($scope.isApplyTraitFilter) {
      traitNames = commonHelpersService.getChecked($scope.traits, 'code')
    }
    var data = {
      speciesNames: speciesNames,
      traitNames: traitNames
    }
    var config = {
      headers: {
        Accept: $scope.traitDataAccept // be sure to specify the MIME type you want in the response
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
      // uses wombleton/link-headers to pull apart the 'link' header so we don't hardcode paging URLs
      var rawLinkHeader = response.headers('link')
      var parsedLinkHeader = $.linkheaders(rawLinkHeader)
      $scope.traitDataFirstPageUrl = commonHelpersService.getLink(parsedLinkHeader, 'first')
      $scope.traitDataPrevPageUrl = commonHelpersService.getLink(parsedLinkHeader, 'prev')
      $scope.traitDataNextPageUrl = commonHelpersService.getLink(parsedLinkHeader, 'next')
      $scope.traitDataLastPageUrl = commonHelpersService.getLink(parsedLinkHeader, 'last')
    }, errorHandler)
  }
})

function errorHandler (error) {
  var msg = 'Something went wrong:'
  alert(msg + error.message)
  throw new Error(msg, error)
}

var app = angular.module('aekosapiexample')

app.config(function ($urlRouterProvider, $stateProvider) {
  $stateProvider.state({
    name: 'exampleParent.allSpecies',
    url: '/all-species',
    component: 'allSpecies'
  })
})

app.component('allSpecies', {
  templateUrl: './src/all-species.html',
  controller: 'AllSpeciesController'
})

app.controller('AllSpeciesController', function ($http, $scope, baseUrlService, commonHelpersService) {
  $scope.speciesDataAccept = 'application/json'

  function resetStep3 () {
    $scope.speciesRecords = []
    $scope.speciesRecordsHeader = {}
    $scope.isLoadSpeciesDataClicked = false
  }

  $scope.getSpeciesData = function () {
    speciesDataHelper(baseUrlService.getBaseUrl() + '/v2/allSpeciesData')
  }

  $scope.speciesDataChangePage = function (url) {
    speciesDataHelper(url)
  }

  $scope.prettyspeciesDataJson = function () {
    return JSON.stringify($scope.speciesRecords, null, 2)
  }

  function speciesDataHelper (url) {
    resetStep3()
    $scope.isLoadspeciesDataClicked = true
    $scope.isLoadingSpeciesData = true
    var config = {
      headers: {
        Accept: $scope.speciesDataAccept
      }
    }
    $http.get(url, config).then(function (response) {
      $scope.isLoadingSpeciesData = false
      var acceptHeader = $scope.speciesDataAccept
      switch (acceptHeader) {
        case 'application/json':
          $scope.speciesRecords = response.data.response
          $scope.speciesRecordsHeader = response.data.responseHeader
          $scope.speciesDataResultWindow = 'json'
          break
        case 'text/csv':
          $scope.speciesRecords = response.data
          $scope.speciesDataResultWindow = 'csv'
          break
        default:
          throw new Error('Programmer problem: unhandled accept header=' + acceptHeader)
      }
      // uses wombleton/link-headers so we don't hardcode paging URLs
      var rawLinkHeader = response.headers('link')
      var parsedLinkHeader = $.linkheaders(rawLinkHeader)
      $scope.speciesDataFirstPageUrl = commonHelpersService.getLink(parsedLinkHeader, 'first')
      $scope.speciesDataPrevPageUrl = commonHelpersService.getLink(parsedLinkHeader, 'prev')
      $scope.speciesDataNextPageUrl = commonHelpersService.getLink(parsedLinkHeader, 'next')
      $scope.speciesDataLastPageUrl = commonHelpersService.getLink(parsedLinkHeader, 'last')
    }, errorHandler)
  }
})

function errorHandler (error) {
  var msg = 'Something went wrong:'
  alert(msg + error.message)
  throw new Error(msg, error)
}

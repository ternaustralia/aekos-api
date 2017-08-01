var app = angular.module('aekosapiexample', [])

app.controller('TheController', function ($http, $scope) {
  $scope.baseUrl = 'https://k2jqcign2l.execute-api.us-west-1.amazonaws.com/test'
  $scope.isLoadVocabsClicked = false
  $scope.isLoading = function (array) {
    return $scope.isLoadVocabsClicked && !array
  }
  $scope.loadVocabs = function () {
    $scope.isLoadVocabsClicked = true
    $http.get($scope.baseUrl + '/v1/getTraitVocab.json').then(function (response) {
      $scope.traits = response.data
    }, errorHandler)
  }
  $scope.findSpecies = function () {
    var traitNames = getTraitNames($scope.traits)
    var data= {
      traitNames: traitNames
    }
    $http.post($scope.baseUrl + '/v1/getSpeciesByTrait.json', data).then(function (response) {
      $scope.species = response.data
    }, errorHandler)
  }
  $scope.getTraitData = function () {
    var speciesNames = getSpeciesNames($scope.species)
    var data= {
      speciesNames: speciesNames
    }
    var config = {
      headers: {
        Accept: 'application/json'
      }
    }
    $http.post($scope.baseUrl + '/v2/traitData', data, config).then(function (response) {
      $scope.traitRecords = response.data.response
      $scope.traitRecordsHeader = response.data.responseHeader
    }, errorHandler)
  }

  function getTraitNames (traits) {
    var result = []
    traits.forEach(function (curr) {
      if (!curr.isChecked) {
        return
      }
      result.push(curr.code)
    })
    return result
  }

  function getSpeciesNames (species) {
    var result = []
    species.forEach(function (curr) {
      if (!curr.isChecked) {
        return
      }
      result.push(curr.name)
    })
    return result
  }
})

function errorHandler(error) {
  var msg = 'Something went wrong:'
  alert(msg + error.message)
  throw new Error(msg, error)
}

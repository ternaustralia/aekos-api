var app = angular.module('aekosapiexample', ['ui.router'])

app.config(function ($urlRouterProvider, $stateProvider) {
  $urlRouterProvider.otherwise('/home')
  $stateProvider.state({
    name: 'home',
    url: '/home',
    component: 'home'
  })
  $stateProvider.state({
    name: 'exampleParent',
    component: 'exampleParent'
  })
})

app.factory('baseUrlService', function () {
  var baseUrl = 'https://k2jqcign2l.execute-api.us-west-1.amazonaws.com/test'
  return {
    getBaseUrl: function () {
      return baseUrl
    },
    setBaseUrl: function (newUrl) {
      baseUrl = newUrl
    }
  }
})

app.component('exampleParent', {
  templateUrl: './src/exampleParent.html',
  controller: function ($scope, baseUrlService) {
    $scope.baseUrl = baseUrlService.getBaseUrl()
    $scope.updateBaseUrl = function (newUrl) {
      baseUrlService.setBaseUrl(newUrl)
    }
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

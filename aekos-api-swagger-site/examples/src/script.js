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

app.constant('devBaseUrl', 'https://dev.api.aekos.org.au')

app.constant('httpErrorHandler', function (error) {
  var url = error.config.url
  var method = error.config.method
  var msg = 'Something went wrong: failed to ' + method + ' the url ' + url +
    '. Check the developer console for more details.'
  throw new Error(msg, error)
})

app.constant('alertingErrorHandler', function (error) {
  console.error(error.message)
  alert(error.message)
})

app.factory('baseUrlService', function (devBaseUrl) {
  var baseUrl = devBaseUrl
  return {
    getBaseUrl: function () {
      return baseUrl
    },
    setBaseUrl: function (newUrl) {
      baseUrl = newUrl
    }
  }
})

app.factory('commonHelpersService', function () {
  return {
    getLink: function (parsedLinkHeader, relName) {
      var value = parsedLinkHeader.find(relName)
      if (value === null) {
        return null
      }
      return value.resolve()
    },
    getChecked: function (vocabs, fieldToReturn) {
      var result = []
      vocabs.forEach(function (curr) {
        if (!curr.isChecked) {
          return
        }
        result.push(curr[fieldToReturn])
      })
      return result
    }
  }
})

app.component('exampleParent', {
  templateUrl: './src/exampleParent.html',
  controller: function ($scope, baseUrlService, devBaseUrl) {
    $scope.baseUrl = baseUrlService.getBaseUrl()
    $scope.devBaseUrl = devBaseUrl
    $scope.updateBaseUrl = function (newUrl) {
      baseUrlService.setBaseUrl(newUrl)
    }
    $scope.setBaseUrl = function (newUrl) {
      $scope.baseUrl = newUrl
      $scope.updateBaseUrl(newUrl)
    }
  }
})

app.filter('filterSelected', function () {
  return function (potentiallyUndefinedItems) {
    var items = potentiallyUndefinedItems || []
    var result = []
    items.forEach(function (e) {
      if (e.isChecked) {
        result.push(e)
      }
    })
    return result
  }
})

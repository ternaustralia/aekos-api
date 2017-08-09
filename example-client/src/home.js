var app = angular.module('aekosapiexample')

app.component('home', {
  templateUrl: './src/home.html',
  controller: 'HomeController'
})

app.controller('HomeController', function ($scope) {
  $scope.examples = [
    {
      title: 'Species by Trait',
      description: 'Start with a vocabulary of traits, find species that have those traits then retrieve the species and trait data.',
      sref: 'exampleParent.speciesByTrait'
    }
  ]
})

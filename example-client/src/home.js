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
    }, {
      title: 'Environment by Species',
      description: 'Start with a vocabulary of environmental variables, find ' +
        'species that occur at sites that have data for those variables then ' +
        'retrieve the data for the visits to those sites.',
      sref: 'exampleParent.envBySpecies'
    }
  ]
})

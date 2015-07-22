;(function (angular) {
    'use strict';

    var app = angular.module('MainRouter', ['ngRoute']);

    app.config(['$routeProvider',
        function($routeProvider) {
            $routeProvider.
                when('/', {
                    controller: 'packetController'
                }).
                otherwise({
                    redirectTo: '/'
                });
        }
    ]);
})(angular);

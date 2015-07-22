;(function (angular) {
    'use strict';

    var API_ENDPOINT = '/api/v1';

    var app = angular.module('services.packet', []);

    app.service('packetService', ['$http',

        function ($http) {

            this.globalStats = function (success) {
                 $http.get(API_ENDPOINT + "/global").success(success).error(function(err) {

                 });
            };

            this.topIps = function (success) {
                $http.get(API_ENDPOINT + "/topips").success(success).error(function(err) {

                });
            }

            this.topPorts = function (success) {
                $http.get(API_ENDPOINT + "/toports").success(success).error(function(err) {

                });
            }

            this.topProtos = function (success) {
                $http.get(API_ENDPOINT + "/toprotos").success(success).error(function(err) {

                });
            }
        }
    ]);


})(angular);

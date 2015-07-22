;(function (angular) {
    'use strict';

    var app = angular.module('controllers.packet', []);

    app.controller('packetController', ['$scope', '$rootScope', 'packetService',

        function ($scope, $rootScope, packetService) {

            var socket = io.connect('http://localhost:8080');

            packetService.globalStats(function(data) {
                $scope.all = data[0].all;
                $scope.tcp = data[1].tcp;
                $scope.udp = data[2].udp;
                socket.on('packets', function(stats) {
                    $rootScope.$apply(function() {
                        $scope.all = stats.totalPackets;
                        $scope.tcp = stats.tcpPackets;
                        $scope.udp = stats.udpPackets;
                    });
                });

            });

            packetService.topIps(function(data) {
                $scope.topIps = data;
                socket.on('ips', function(stats) {
                    $rootScope.$apply(function() {
                        $scope.topIps = stats;
                    });
                });
            });

            packetService.topPorts(function(data) {
                $scope.topPorts = data;
                socket.on('ports', function(stats) {
                    $rootScope.$apply(function() {
                        $scope.topPorts = stats;
                    });
                });
            });

            packetService.topProtos(function(data) {
                $scope.topProtos = data;
                socket.on('protos', function(stats) {
                    $rootScope.$apply(function() {
                        $scope.topProtos = stats;
                    });
                });
            });
        }
    ]);

})(angular);

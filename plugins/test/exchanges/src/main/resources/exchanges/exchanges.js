/*
 * Copyright 2015 Kantega AS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

angular
    .module("respiro.exchanges", ["respiro"])
    .config(function(respiroMenuProvider) {

        respiroMenuProvider
            .add({
                href: "/exchanges",
                label: "Exchanges"
            })
    })
    .config(function ($routeProvider) {

        $routeProvider
            .when('/exchanges', {
                controller:'ExchangesController as exchangesList',
                templateUrl:'partials/exchanges.html',
            })
            .when('/exchanges/details/:exchangeUuid', {
                controller:'ExchangeDetailsController as details',
                templateUrl:'partials/exchanges-details.html',
            })
    })
    .controller("ExchangesController", function($http, $scope) {
        $http.get("exchanges/api/exchanges").then(function(result) {

            $scope.exchanges = result.data;

        }, function() {
            alert("Failed getting exchanges from server");
        })
    })
    .controller("ExchangeDetailsController", function($http, $scope, $routeParams) {
        $http.get("exchanges/api/exchanges/" + $routeParams.exchangeUuid).then(function(result) {
            $scope.ex= result.data;
        }, function() {
            alert("Failed getting exchange details from server");
        })
    });

respiro.module("respiro.exchanges");
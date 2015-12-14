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
    .module("respiro.registry", ["respiro"])
    .config(function(respiroMenuProvider) {
        respiroMenuProvider
            .add({
                href: "/registry",
                label: "Registry"
            });
    })
    .config(function ($routeProvider) {

        $routeProvider
            .when('/registry', {
                controller:'RegistryController as registry',
                templateUrl:'partials/registry.html',
            })
    })
    .controller("RegistryController", function($http, $scope) {
        $http.get("registry/soap").then(function(result) {

            $scope.soap = result.data;

        }, function() {
            alert("Failed getting SOAP endpoints from server");
        })

        $http.get("registry/rest").then(function(result) {

            $scope.rest= result.data;

        }, function() {
            alert("Failed getting REST resources from server");
        })
    });

respiro.module("respiro.registry");
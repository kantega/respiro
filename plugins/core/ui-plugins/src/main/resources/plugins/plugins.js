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
    .module("respiro.plugins", ["respiro"])
    .config(function(respiroMenuProvider) {
        respiroMenuProvider
            .add({
                href: "/plugins",
                label: "Plugins"
            })
    })
    .config(function ($routeProvider) {

        $routeProvider
            .when('/plugins', {
                controller: 'PluginsController as plugins',
                templateUrl: 'partials/plugins.html',
            })
            .otherwise({
                redirectTo: '/plugins'
            });

    })
    .controller("PluginsController", function ($http, $scope) {

        function groupPlugins(data) {
            var respiro = [];
            var reststop = [];
            var other = [];
            var groups = [
                {name: "Your plugins", plugins:other},
                {name: "Respiro plugins", plugins:respiro},
                {name: "Reststop plugins", plugins:reststop},
            ];
            for(var i = 0; i < data.length; i++) {
                var plugin = data[i];
                if(plugin.groupId === "org.kantega.reststop") {
                    reststop.push(plugin);
                } else if(plugin.groupId === "org.kantega.respiro") {
                    respiro.push(plugin);
                } else {
                    other.push(plugin)
                }
            }
            return groups;
        }


        $http.get("plugins")
            .then(function (data) {
                    $scope.plugins = groupPlugins(data.data);
                },
                function () {
                    console.log("Error fetching plugins")
                });


    });
respiro.module("respiro.plugins");
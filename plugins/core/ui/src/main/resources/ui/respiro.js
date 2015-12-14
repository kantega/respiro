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
    .module("respiro", ["ngRoute"])
    .provider("respiroMenu", function () {

        function RespiroMenu(items) {

            this.items = function() {return items};
        }

        var items = [];

        this.add = function(item) {
            items.push(item);
            return this;
        }

        this.$get = function() {
            return new RespiroMenu(items)
        }
    })
    .controller("NavController", function ($http, $scope, $location, respiroMenu) {
        $scope.menuitems = respiroMenu.items()

        $scope.active = function(item) {
            return $location.path().indexOf(item.href) == 0;
        }
    })
    .controller("ProfileController", function ($http, $scope) {

        $http.get("userprofile")
            .then(function (data) {
                    $scope.userprofile = data.data
                },
                function () {
                    console.log("Error fetching user profile")
                });

    });



(function() {

    window.respiro = {}

    var items = ["respiro"];

    window.respiro.module = function(name) {
        items.push(name);
    }

    var xhr = new XMLHttpRequest();
    xhr.open("GET", "modules", true);


    xhr.onreadystatechange = function() {

        if(xhr.readyState == 4 && xhr.status == 200) {

            var modules = angular.fromJson(xhr.responseText);

            var remaining = modules.length;

            for(var i = 0; i < modules.length; i++) {
                var module = modules[i];
                var script = document.createElement("script");
                script.setAttribute("src", module.src);
                script.addEventListener("load", function() {
                    remaining--;
                    if(remaining == 0) {
                        angular.bootstrap(document, items);
                    }
                });
                document.head.appendChild(script);
            }
        }
    };

    xhr.send();
})();
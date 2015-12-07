angular
    .module("respiro", ["ngRoute"])
    .config(function ($routeProvider) {

        $routeProvider
            .when('/plugins', {
                controller: 'PluginsController as plugins',
                templateUrl: 'partials/plugins.html',
            })
            .when('/metrics', {
                controller: 'MetricsController as metrics',
                templateUrl: 'partials/metrics.html',
            })
            .otherwise({
                redirectTo: '/plugins'
            });


    })
    .controller("NavController", function ($http, $scope, $location) {
        $scope.menuitems = [
            {
                href: "/plugins",
                label: "Plugins"
            },
            {
                href: "/metrics",
                label: "Metrics"
            }
        ];

        $scope.more = [
            {
                href: "/systeminfo",
                label: "System info"
            },
            {
                href: "/sql",
                label: "SQL console"
            }
        ];

        $scope.active = function(item) {
            return $location.path() === item.href;
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


    })
    .controller("MetricsController", function ($http, $scope) {
        $http.get("../metrics/")
            .then(function (data) {
                var resources = new Array();
                var endpoints = new Array();

                var timers = data.data.timers;
                    for (var name in timers) {
                        if(name.indexOf("REST.") == 0) {
                            var timer = timers[name];
                            var rest = name.substr("REST.".length);
                            var method = rest.substr(0, rest.indexOf("."));
                            var path = rest.substr(rest.indexOf(".") + 1);
                            resources.push({name: name, method: method, path: path, timer: timer});
                        } else if(name.indexOf("SOAP.") == 0) {
                            var timer = timers[name];
                            var rest = name.substr("SOAP.".length);
                            var serviceStart = 0
                            var operationStart = rest.indexOf(".", serviceStart+1);
                            var pathStart = rest.indexOf(".", operationStart+1);

                            endpoints.push({
                                name: name,
                                service: rest.substr(serviceStart, operationStart-serviceStart),
                                operation: rest.substr(operationStart +1, pathStart-operationStart-1),
                                path: rest.substr(pathStart),
                                timer: timer});
                        }
                    }

                    $scope.resources = resources;
                    $scope.endpoints = endpoints;
                },
                function () {
                    console.log("Error fetching metrics")
                });
    });

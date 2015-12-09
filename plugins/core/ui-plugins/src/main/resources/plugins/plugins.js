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
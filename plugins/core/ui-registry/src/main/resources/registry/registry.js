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
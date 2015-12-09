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
                templateUrl:'../exchanges/list.html',
            })
            .when('/exchanges/details/:exchangeUuid', {
                controller:'ExchangeDetailsController as details',
                templateUrl:'../exchanges/details.html',
            })
            .otherwise({
                redirectTo: '/plugins'
            });


    })
    .controller("ExchangesController", function($http, $scope) {
        $http.get("../exchanges/api/exchanges").then(function(result) {

            $scope.exchanges = result.data;

        }, function() {
            alert("Failed getting exchanges from server");
        })
    })
    .controller("ExchangeDetailsController", function($http, $scope, $routeParams) {
        $http.get("../exchanges/api/exchanges/" + $routeParams.exchangeUuid).then(function(result) {
            $scope.ex= result.data;
        }, function() {
            alert("Failed getting exchange details from server");
        })
    });

respiro.module("respiro.exchanges");
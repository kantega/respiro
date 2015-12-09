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
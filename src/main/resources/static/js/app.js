var myApp = angular.module('healthApp',["ui.bootstrap"]);

myApp.controller('SearchController',["$scope","Search","Totals", function($scope,Search,Totals) {
    
    $scope.searched=false;
    $scope.searching=false;
    $scope.searchedData={};
    
    $scope.doSearch=function(){
        //validate search params
        $scope.searchErrors=Search.validateSearchData();
        if($scope.searchErrors.length===0){
            var searchedData={};
            $scope.searching=true;
            Search.doSearch().success(function(response){
                $scope.searchResults=response;
                angular.copy(Search.searchData,searchedData);
                $scope.searched=true;
                $scope.searchedData=searchedData;
                $scope.searching=false;
                $scope.searchedData.demographics=Totals.buildDemographics(Search.searchData);
            }).error(function(error){
                $scope.searching=false;
                $scope.searchErrors=["There seems to be an issue with our data feed. Please try again later"]
            });       
        }
        
    };
    $scope.doReset=function(){
        if(confirm("Reset all search fields?")){
            //$rootScope.$broadcast('cancelSearch');
            $scope.searchErrors="";
            $scope.searched=false;
            $scope.searchedData={};
        }
   
    };
    
    $scope.showSearchDetails=function(whichOne){
        $scope.showSearchDetails[whichOne]=!$scope.showSearchDetails[whichOne];
    }
    
    $scope.downloadCSV=function(fileType){
        Search.export(fileType)
        return false
    }
}]);

myApp.controller('SearchResults',["$scope","Search","Totals",function($scope,Search,Totals){
    $scope.searched=false;
    $scope.searchResults={};
    var searchedData={};
    $scope.$on('search', function(event,eventData) {
        Search.doSearch().success(function(response){
            $scope.searchResults=response.data;   
        }).error(function(error){
            $scope.searchErrors="There seems to be an issue with our data feed. Please try again later"
        });       
        angular.copy(eventData,searchedData);
        $scope.searched=true;
        $scope.searchedData=searchedData;
        $scope.searchedData.demographics=Totals.buildDemographics(eventData);        
    });
    $scope.$on("cancelSearch",function(){
        $scope.searched=false;
    });
}])

myApp.directive("closeAllSearchDialogs",["$rootScope",function($rootScope){
        return {
            link:function(){
                $(document).bind('click', function(event){
                    var isClickedElementChildOfPopup = $(".searchDropdown")
                        .find(event.target)
                        .length > 0 || $(event.target).attr("class")=="searchDropdown";

                    if (isClickedElementChildOfPopup)
                        return;

                    $rootScope.$apply(function(){
                        $rootScope.$broadcast("openDialog");
                    });
                });
            }
        }
}])

myApp.directive("regionSelect",["$http","$rootScope","Search",function($http,$rootScope,Search){
    return {
        templateUrl: "/directives/regionSelect.tmpl.html",
        link:function(scope,element){
            scope.$on("openDialog",function(event,dialogName){
                if(dialogName!="region"){
                    scope.hideRegionOptions();
                }
            })
            $http.get("/api/regions").then(function(response){
                scope.regionsList=response.data
                Search.searchData.regions=scope.regionsList;
            })
            scope.showRegionOptions=function(){
                scope.showRegions=!scope.showRegions;
                scope.$broadcast("openDialog","region")
            }
            scope.hideRegionOptions=function(){
                scope.showRegions=false;
            }
            scope.cancelRegionOptions=function(){
                scope.hideRegionOptions();
                scope.deselectAllRegions();
            }

            scope.selectAllRegions=function(){
                angular.forEach(scope.regionsList, function (item) {
                    item.selected = true;
                });
            }
            scope.deselectAllRegions=function(){
                angular.forEach(scope.regionsList, function (item) {
                    item.selected = false;
                });
            }
            
            scope.$watch("regionsList", function(newlySelectedRegions, oldSelection){
                var regions=newlySelectedRegions;
                var selectedItems = 0;
                Search.searchData.regions=[];
                //unset counties
                
                angular.forEach(scope.countiesList, function (singleCounty) {
                            angular.forEach(singleCounty,function(county){
                                county.selected=false;  
                            })
                        })

                        
                //This is nested pretty deep, but its because of the breaking of counties into 4 sections for layout
                angular.forEach(regions, function(region){
                    if(region.selected){
                        scope.regionSelected=true;
                        selectedItems += 1;
                        Search.searchData.regions.push(region)
                        angular.forEach(scope.countiesList, function (singleCounty) {
                            angular.forEach(singleCounty,function(county){
                                if(region.id==county.region.id){
                                    county.selected=true;
                                }
                            });
                        });
                    }
                    
                })
                scope.selectedRegions = selectedItems;
            }, true);
            
            $rootScope.$on("cancelSearch",scope.cancelRegionOptions)
        }
    }
}])

myApp.directive("countySelect",["$http","$rootScope","Search",function($http,$rootScope,Search){
    return {
        templateUrl: "/directives/countySelect.tmpl.html",
        link:function(scope,element){
            
            $http.get("/api/counties?max=100").then(function(response){
                scope.countiesList=chunk(response.data,10);
            })
            scope.$on("openDialog",function(event,dialogName){
                if(dialogName!="county"){
                    scope.hideCountyOptions();
                }
            })
            scope.showCountyOptions=function(){
                scope.showCounties=!scope.showCounties;
                scope.$broadcast("openDialog","county")
            }
            scope.hideCountyOptions=function(){
                scope.showCounties=false;
            }
            scope.cancelCountyOptions=function(){
                scope.hideCountyOptions();
                scope.deselectAllCounties();
            }

            scope.selectAllCounties=function(){
                angular.forEach(scope.countiesList, function (item) {
                    angular.forEach(item,function(county){
                        county.selected=true
                    });
                });
            }
            scope.deselectAllCounties=function(){
                angular.forEach(scope.countiesList, function (item) {
                    angular.forEach(item,function(county){
                        county.selected=false
                    });
                });
            }

            scope.$watch("countiesList", function(items){
                var counties = [].concat.apply([], items);
                var selectedItems = 0;
                Search.searchData.counties=[]
                angular.forEach(counties, function(item){
                    if(item.selected){
                        selectedItems += 1;
                        Search.searchData.counties.push(item)
                    }
                })
                scope.selectedCounties = selectedItems;
            }, true);
            
            $rootScope.$on("cancelSearch",scope.cancelCountyOptions)
        }
    }
}])
myApp.directive("diseaseSelect",["$http","$rootScope","Search",function($http,$rootScope,Search){
    return {
        templateUrl: "/directives/diseaseSelect.tmpl.html",
        link:function(scope,element){
            $http.get("/api/diseases?max=100").then(function(response){
                scope.diseasesList=chunk(response.data,5);
            })
            
            scope.$on("openDialog",function(event,dialogName){
                if(dialogName!="disease"){
                    scope.hideDiseaseOptions();
                }
            })
            
            scope.showDiseaseOptions=function(){
                scope.showDiseases=!scope.showDiseases;
                scope.$broadcast("openDialog","disease")
            }
            scope.hideDiseaseOptions=function(){
                scope.showDiseases=false;
            }
            scope.cancelDiseaseOptions=function(){
                scope.hideDiseaseOptions();
                scope.deselectAllDiseases();
            }

            scope.selectAllDiseases=function(){
                angular.forEach(scope.diseasesList, function (item) {
                    angular.forEach(item,function(disease){
                        disease.selected=true
                    });
                });
            }
            scope.deselectAllDiseases=function(){
                angular.forEach(scope.diseasesList, function (item) {
                    angular.forEach(item,function(disease){
                        disease.selected=false
                    });
                });
            }

            scope.$watch("diseasesList", function(items){
                var diseases = [].concat.apply([], items);
                var selectedItems = 0;
                Search.searchData.diseases=[]
                angular.forEach(diseases, function(item){
                    if(item.selected) {
                        selectedItems +=1;
                        Search.searchData.diseases.push(item)
                    }
                })
                scope.selectedDiseases = selectedItems;
            }, true);
            
            $rootScope.$on("cancelSearch",scope.cancelDiseaseOptions)
        }
    }
}])
myApp.directive("ethnicitySelect",["$http","$rootScope","Search",function($http,$rootScope,Search){
    return {
        templateUrl: "/directives/ethnicitySelect.tmpl.html",
        link:function(scope,element){
            $http.get("/api/ethnicity?max=100").then(function(response){
                scope.ethnicitiesList=chunk(response.data,3);
            })
            
            scope.$on("openDialog",function(event,dialogName){
                if(dialogName!="ethnicity"){
                    scope.hideEthnicityOptions();
                }
            })
            
            scope.showEthnicityOptions=function(){
                scope.showEthnicities=!scope.showEthnicities;
                scope.$broadcast("openDialog","ethnicity")
            }
            scope.hideEthnicityOptions=function(){
                scope.showEthnicities=false;
            }
            scope.cancelEthnicityOptions=function(){
                scope.hideEthnicityOptions();
                scope.deselectAllEthnicities();
            }

            scope.selectAllEthnicities=function(){
                angular.forEach(scope.ethnicitiesList, function (item) {
                    angular.forEach(item,function(ethnicity){
                        ethnicity.selected=true
                    });
                });
            }
            scope.deselectAllEthnicities=function(){
                angular.forEach(scope.ethnicitiesList, function (item) {
                    angular.forEach(item,function(ethnicity){
                        ethnicity.selected=false
                    });
                });
            }

            scope.$watch("ethnicitiesList", function(items){
                var ethnicities = [].concat.apply([], items);
                var selectedItems = 0;
                Search.searchData.ethnicities=[];
                angular.forEach(ethnicities, function(item){
                    if(item.selected){
                        selectedItems += 1;
                        Search.searchData.ethnicities.push(item)
                    }
                })
                scope.selectedEthnicities = selectedItems;
            }, true);
            
            $rootScope.$on("cancelSearch",scope.cancelEthnicityOptions)
        }
    }
}])

myApp.directive("ageGroupSelect",["$http","$rootScope","Search",function($http,$rootScope,Search){
    return {
        templateUrl: "/directives/ageGroupSelect.tmpl.html",
        link:function(scope,element){
            $http.get("/api/agegroup?max=100").then(function(response){
                scope.ageGroupList=chunk(response.data,3);
            })
            
            scope.$on("openDialog",function(event,dialogName){
                if(dialogName!="ageGroup"){
                    scope.hideAgeGroupOptions();
                }
            })
            
            scope.showAgeGroupOptions=function(){
                scope.showAgeGroups=!scope.showAgeGroups;
                scope.$broadcast("openDialog","ageGroup")
            }
            scope.hideAgeGroupOptions=function(){
                scope.showAgeGroups=false;
            }
            scope.cancelAgeGroupOptions=function(){
                scope.hideAgeGroupOptions();
                scope.deselectAllAgeGroups();
            }

            scope.selectAllAgeGroups=function(){
                angular.forEach(scope.ageGroupList, function (item) {
                    angular.forEach(item,function(ageGroup){
                        ageGroup.selected=true
                    });
                });
            }
            scope.deselectAllAgeGroups=function(){
                angular.forEach(scope.ageGroupList, function (item) {
                    angular.forEach(item,function(ageGroup){
                        ageGroup.selected=false
                    });
                });
            }

            scope.$watch("ageGroupList", function(items){
                var ageGroups = [].concat.apply([], items);
                var selectedItems = 0;
                Search.searchData.ageGroups=[];
                angular.forEach(ageGroups, function(item){
                    if(item.selected){
                        selectedItems += 1;
                        Search.searchData.ageGroups.push(item)
                    }
                })
                scope.selectedAgeGroups = selectedItems;
            }, true);
            
            $rootScope.$on("cancelSearch",scope.cancelAgeGroupOptions)
        }
    }
}])

myApp.directive("searchResultsAggregator",[function(){
    return {
        templateUrl: "/directives/searchResultsAggregator.tmpl.html",
        scope:{
            //data they searched for
            searchData:"=",
            //Sometimes only one disease is shown (per tab) or all diseases (totals tab)
            //directive declaration will dicate what is passed. See HTML
            disease:"="
        },
        link:function(scope,element,attr){           
        }
    }
}])

myApp.directive("searchResultsTotalsTable",[function(){
    return {
        templateUrl: "/directives/searchResultsTable.tmpl.html",
        scope:{
            someTotal:"="
        },
        link:function(scope,element,attr){
            scope.title=attr.title;
            scope.description=attr.description;
            //the totals are fed from server, need to watch this, otherwise you can just
            //use the "=" in scope declaration.
            scope.$watch("someTotal",function(v){
                scope.totals=v;
            })
        }
    }
}]);

myApp.directive("searchResultsDetailsTable",[function(){
    return {
        templateUrl: "/directives/searchResultsTableDetails.tmpl.html",
        scope:{
            details:"=",
        },
        link:function(scope,element,attr){
            scope.title=attr.title;
            scope.description=attr.description;
            //the totals are fed from server, need to watch this, otherwise you can just
            //use the "=" in scope declaration.
            scope.$watch("details",function(v){
                scope.details=v;
            })
        }
    }
}]);

myApp.directive("sexOptions",["$rootScope","Search",function($rootScope,Search){
        return{
            templateUrl:"/directives/sexOptions.tmpl.html",
            link:function(scope){
                
                scope.sex={
                    male:false,
                    female:false
                }
                
                Search.searchData.sex=scope.sex;
                
                
                $rootScope.$on("cancelSearch",function(){
                    scope.sex.male=false;
                    scope.sex.female=false;
                })
            }
        }
}])

function chunk(arr, size) {
  var newArr = [];
  for (var i=0; i<arr.length; i+=size) {
    newArr.push(arr.slice(i, i+size));
  }
  return newArr;
}

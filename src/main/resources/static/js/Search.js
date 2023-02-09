angular.module("healthApp").service("Search",["$http",function($http){
        
        var searchData={}
        
        var prepareSearchData=function(){
            var parsedSearchData={
                disease:[],
                region:[],
                county:[],
                ethnicity:[],
                ageGroup:[],
                sex:[]
            };
            angular.forEach(searchData.diseases,function(obj){
                parsedSearchData.disease.push(obj.id)
            });
            angular.forEach(searchData.regions,function(obj){
                parsedSearchData.region.push(obj.id)
            });
            angular.forEach(searchData.counties,function(obj){
                parsedSearchData.county.push(obj.id);
            });
            angular.forEach(searchData.ethnicities,function(obj){
                parsedSearchData.ethnicity.push(obj.id);
            });
            angular.forEach(searchData.ageGroups,function(obj){
                parsedSearchData.ageGroup.push(obj.id);
            });
            if(searchData.sex.male){
                parsedSearchData.sex.push("Male")
            }
            if(searchData.sex.female){
                parsedSearchData.sex.push("Female")
            }
            return parsedSearchData;
        }
        
        return {
            searchData:searchData,
            validateSearchData:function(){
                var searchErrors=[];
                if(searchData.diseases.length==0){
                    searchErrors.push("Please select a Disease");
                }
                if(searchData.regions.length==0 && searchData.counties.length==0){
                    searchErrors.push("Please select a Region or a County")
                }
                if(searchData.ethnicities.length==0){
                    searchErrors.push("Please select an Ethnicity")
                }
                if(searchData.ageGroups.length==0){
                    searchErrors.push("Please select an Age Group")
                }
                if(searchData.sex.length==0){
                    searchErrors.push("Please select an Age Group")
                }

                if(!searchData.sex.male && !searchData.sex.female){
                    searchErrors.push("Please select Sex")
                } 
                
                return searchErrors
            },
            doSearch:function(){
                var data=prepareSearchData();
                return $http({
                    url:"/search",
                    params:data
                });
            },
            export:function(exportType){
                switch(exportType){
                    //export all search params as CSV
                    case "total":
                        var searchData=prepareSearchData()
                        //this little doohicky converts the search data object to url params, &param=value, etc.
                        var params=Object.keys(searchData).map(function(k) {                            
                            var output="";
                            angular.forEach(searchData[k],function(value){
                                output+="&"+(encodeURIComponent(k) + "=" + encodeURIComponent(value));
                            })
                            return output
                        }).join('')
                        window.location="/export?" + params
                        break;
                }
            }
            
        }
        
        
        
        }]);

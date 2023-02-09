angular.module("healthApp").service("Totals",[function(){
        
        return {
            buildDemographics:function(searchData){
                var demographics=[];
                angular.forEach(searchData.ethnicities,function(ethnicity){
                    //sex -> age group -> ethnicity
                    if(searchData.sex.male){
                        angular.forEach(searchData.ageGroups,function(ageGroup){
                            demographics.push(ethnicity.ethnicityName + " Male " + ageGroup.groupName)
                        })
                    }
                    if(searchData.sex.female){
                        angular.forEach(searchData.ageGroups,function(ageGroup){
                            demographics.push(ethnicity.ethnicityName + " Female " + ageGroup.groupName)
                        })
                    }

                })
                return demographics;
            }
        };
                
}]);
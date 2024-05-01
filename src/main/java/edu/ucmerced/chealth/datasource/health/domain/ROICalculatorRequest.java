package edu.ucmerced.chealth.datasource.health.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class ROICalculatorRequest {
	
	/*
	*  "county": "1",
    	"region": "1",
		"disease": "1",
		"sex": "Female",
		"ethnicity": "1",
		"sizeOfGroup": 500,
    	"anticipatedEffectivenessOfProgram": "1",
		"anticipatedTimeForEffectivenessOfProgram": 2.0,
		"numberOfYearsForROI" : "10",
    	"initialProgramCost" : 10000,
		"ongoingProgramCost" : 10000,
		"operationalPeriodOfProgram" : 10,
		"discountRate": 2.0,
		"ageLimit": "11-12",
		"valueOfQaly": 50000.0
	
	*/
	
	
	private  String county; 
	private  String region; 
	private  String disease; 
	private  String sex; 
	private  String ethnicity; 
	private  String ageLimit;  //25
	private long sizeOfGroup;
	@JsonProperty(value = "valueOfQaly")
	private  double valueOfQaly; 
	@JsonProperty(value = "anticipatedEffectivenessOfProgram")
	private  float anticipatedEffectivenessOfProgram; 
	
	@JsonProperty(value = "anticipatedTimeForEffectivenessOfProgram")
	private  int anticipatedTimeForEffectivenessOfProgram; 
	
	@JsonProperty(value = "numberOfYearsForROI")
	private  int numberOfFollowUpYears; 
	@JsonProperty(value = "initialProgramCost")
	private long initialProgramCost;
	
	@JsonProperty(value = "ongoingProgramCost")
	private long ongoingProgramCost;
	
	@JsonProperty(value = "operationalPeriodOfProgram")
	private int operationalPeriodOfProgram;
	
	@JsonProperty(value = "discountRate")
	private  float discountRate;
	

	
	

}

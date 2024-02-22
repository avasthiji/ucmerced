package edu.ucmerced.chealth.datasource.health.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class ROICalculatorRequest {
	
	private  String countyName; 
	private  String regionName; 
	private  String diseaseName; 
	private  String sex; 
	private  String ethnicity; 
	private  String ageLimit;  //25
	
	@JsonProperty(value = "InvestmentPerPerson")
	private  double InvestmentPerPerson; 
	private  boolean unDouscounted; 
	private  int numberOfFollowUpYears; 
	private  long SizeOfGroup; 
	
	@JsonProperty(value = "ReductionInRateWithProgram")
	private  float ReductionInRateWithProgram; 
	
	@JsonProperty(value = "ReductionInRateAfterYearsWithProgram")
	private  int ReductionInRateAfterYearsWithProgram; 
	private  float discountedfactor;
	
	

}
package edu.ucmerced.chealth.datasource.health.domain;

import lombok.Data;

@Data
public class ROICalculatorRequest {
	
	private  String countyName; 
	private  String regionName; 
	private  String diseaseName; 
	private  String sex; 
	private  String ethnicity; 
	private  int startAge;  //25
	private  double InvestmentPerPerson; 
	private  boolean unDouscounted; 
	private  int numberOfFollowUpYears; 
	private  long SizeOfGroup; 
	private  float ReductionInRateWithProgram; 
	private  int ReductionInRateAfterYearsWithProgram; 
	private  float discountedfactor;
	
	

}

package edu.ucmerced.chealth.datasource.health.domain;

import lombok.Data;

@Data
public class ROIHealthModelPerYear {

	private  int age; 
	private  long totalCases; 
	private  long numberOfPeopleWithProgram; 
	private  long numberOfPeopleWithOutProgram;
	private  double investmentPerPerson;
	private  double difference; 
	private  String costSavingPerYear; 
	private  String discountedCostSavingsPerYear; 
	private  String UtilityCost; 
	private  String totalCost;

	

}

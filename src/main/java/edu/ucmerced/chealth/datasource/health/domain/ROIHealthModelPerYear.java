package edu.ucmerced.chealth.datasource.health.domain;

import lombok.Data;

@Data
public class ROIHealthModelPerYear {

	private  int age; 
	private  long population; 
	private  long numberOfPeopleWithProgram; 
	private  long numberOfPeopleWithOutProgram;
	private  double investmentPerPerson;
	private  double difference; 
	private  String costSavingPerYear; 
	private  String discountedCostSavingsPerYear; 
	private  String countyName; 
	private  String regionName; 
	private  String diseaseName; 
	private  String sex; 
	private  String ethnicity; 
	private  String UtilityCost; 
	private  String totalCost;

	

}

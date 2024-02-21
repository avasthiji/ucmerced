package edu.ucmerced.chealth.datasource.health.domain;

import lombok.Data;

@Data
public class ROIHealthModelPerYear {

	private  int age; 
	private  double population; 
	private  double numberOfPeopleWithProgram; 
	private  double numberOfPeopleWithOutProgram;
	private  double investmentPerPerson;
	private  double difference; 
	private  double costSavingPerYear; 
	private  double discountedCostSavingsPerYear; 
	private  String countyName; 
	private  String regionName; 
	private  String diseaseName; 
	private  String sex; 
	private  String ethnicity; 
	private  double UtilityCost; 
	private  double totalCost;

	

}

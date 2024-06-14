package edu.ucmerced.chealth.datasource.health.domain;

import lombok.Data;

@Data
public class ROIHealthModelPerYear {

	
	private String conditions; 
	private String region;
	private String county;
	private String ethnicity;
	private String gender;
	private  int age; 
	private int year;
	private double discount;
	private double prevRateInitial;
	private double prevRateAfter;
	private double utilityDiffAfterWithDiscount;
	private double utilityDiffInitialWithoutDiscount;
	
	private double population;
	private double casesBeforeProgram; 
	private double casesAfterProgram;
	
	private double utilityLossDiscountedInitial; 
	private double utilityLossDiscountedAfter; 
	private double utilityLossDiscountedDiff; 
	
	private double utilityLossInitial; 
	private double utilityLossAfter; 
	private double utilityLossDiff; 
	
	private double utilityCostInitial; 
	private double utilityCostAfter; 
	private double utilityCostDiff; 
	
	private double costPerCase;
	
	private double healthcareCostInitial;
	private double healthcareCostAfter;
	private double healthcareCostDiff;
	
	private double totalCostInitial;
	private double totalCostAfter;
	private double totalCostDiff;
	private double totalCostDiffDiscounted;
	private double investment;
	private double discountedInvestment;


	

}

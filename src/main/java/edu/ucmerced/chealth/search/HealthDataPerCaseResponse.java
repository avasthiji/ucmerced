package edu.ucmerced.chealth.search;

import lombok.Data;

@Data
public class HealthDataPerCaseResponse {
	
	private String conditions; 
	private String county; 
	private String costPerCase; 
	private String utilityLossPerCase;
	private String rates; 
	private long cases; 
	private String healthCareCost; 
	private String utilityLoss; 
	private String costOfUtility;
	private String totalCost;

}

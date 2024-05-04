package edu.ucmerced.chealth.datasource.health.domain;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class CumulativeROIHealthModel {
	
	//Table 1
	private String county;
	
	private double costPerCaseInitial;
	private double costPerCaseAfterProgram;
	private double costPerCaseDiff;
	
	private double utilityLossPerCaseInitial;
	private double utilityLossPerCaseAfterProgram;
	private double utilityLossPerCaseDiff;
	
	private double ratesInitial;
	private double ratesAfterProgram;
	private double ratesDiff;
	
	private long populationInitial;
	private long populationAfterProgram;
	private long populationDiff;
	
	private long casesInitial;
	private long casesAfterProgram;
	private long casesDiff;
	
	private double utilityLossInitial;
	private double utilityLossAfterProgram;
	private double utilityLossDiff;
	
	private double healthCareCostInitial;
	private double healthCareCostAfterProgram;
	private double healthCareCostDiff;
	
	private String totalCostInitial;
	private String totalCostAfterProgram;
	private double totalCostDiff;
	
	//Table 2
	private long totalCasesWithProgram; 
	private long totalCasesWithoutProgram;
	private long totalCasesDiff;
	
	private double totalCostWithoutQalyWithProgram;
	private double totalCostWithoutQalyWithoutProgram;
	private double totalCostWithoutQalyDiff;
	
	private String totalCostWithQalyWithProgram;
	private String totalCostWithQalyWithoutProgram;
	private double totalCostWithQalyDiff;
	
	private double totalQalyWithProgram;
	private double totalQaLYWithoutProgram;
	private double totalQalyDiff;
	
	private double investmentWithProgram;
	private double investmentWithoutProgram;
	private double investmentDiff;
	
	private double ROIWithoutQaly;
	private double ROIWithQaly;
	private double ROIDiscounted;
	
	private List<ROIHealthModelPerYear> ageRange = new ArrayList<ROIHealthModelPerYear>();
	
}

package edu.ucmerced.chealth.datasource.health.domain;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@Data
public class CumulativeROIHealthModel {
	
	//Table 1
	private String county;
	
	private long costPerCaseInitial;
	private long costPerCaseAfterProgram;
	private long costPerCaseDiff;
	
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
	
	private long utilityCostInitial;
	private long utilityCostAfterProgram;
	private long utilityCostDiff;
	
	private long healthCareCostInitial;
	private long healthCareCostAfterProgram;
	private long healthCareCostDiff;
	
	private long totalCostInitial;
	private long totalCostAfterProgram;
	private long totalCostDiff;
	
	//Table 2
	private long totalCasesWithProgram; 
	private long totalCasesWithoutProgram;
	private long totalCasesDiff;
	
	private long totalCostWithoutQalyWithProgram;
	private long totalCostWithoutQalyWithoutProgram;
	private long totalCostWithoutQalyDiff;
	
	private long totalCostWithQalyWithProgram;
	private long totalCostWithQalyWithoutProgram;
	private long totalCostWithQalyDiff;
	
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

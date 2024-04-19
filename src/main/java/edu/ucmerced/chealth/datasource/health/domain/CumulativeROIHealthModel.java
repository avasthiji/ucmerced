package edu.ucmerced.chealth.datasource.health.domain;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class CumulativeROIHealthModel {
	
	private long totalCasesWithProgram; 
	private long totalCasesWithoutProgram;
	private long totalCasesDiff;
	
	private double totalCostWithoutQalyWithProgram;
	private double totalCostWithoutQalyWithoutProgram;
	private double totalCostWithoutQalyDiff;
	
	private double totalCostWithQalyWithProgram;
	private double totalCostWithQalyWithoutProgram;
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

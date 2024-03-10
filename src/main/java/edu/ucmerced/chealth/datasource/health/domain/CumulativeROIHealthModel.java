package edu.ucmerced.chealth.datasource.health.domain;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class CumulativeROIHealthModel {
	
	private  String totalCostSaving;
	private  String discountedCostSaving;
	private  String rOIDiscounted;
	private  String rOINonDiscounted;
	private  String totalUtilityCost;
	private  String  totalcost;
	private List<ROIHealthModelPerYear> ageRange = new ArrayList<ROIHealthModelPerYear>();
	
}

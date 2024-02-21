package edu.ucmerced.chealth.datasource.health.domain;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class CumulativeROIHealthModel {
	
	private  double totalCostSaving;
	private  double discountedCostSaving;
	private  double rOIDiscounted;
	private  double rOINonDiscounted;
	private  double totalUtilityCost;
	private  double  totalcost;
	private  Map<Integer, List<ROIHealthModelPerYear>> agemap = new HashMap<Integer, List<ROIHealthModelPerYear>>();
	
	
}

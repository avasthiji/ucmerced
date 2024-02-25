package edu.ucmerced.chealth.datasource.health.domain;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class CumulativeROIHealthModel {
	
	private  String totalCostSaving;
	private  String discountedCostSaving;
	private  String rOIDiscounted;
	private  String rOINonDiscounted;
	private  String totalUtilityCost;
	private  String  totalcost;
	private  Map<Integer, List<ROIHealthModelPerYear>> agemap = new HashMap<Integer, List<ROIHealthModelPerYear>>();
	
	
}

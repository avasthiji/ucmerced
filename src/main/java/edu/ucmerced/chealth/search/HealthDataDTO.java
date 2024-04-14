package edu.ucmerced.chealth.search;

import java.text.DecimalFormat;

import edu.ucmerced.chealth.datasource.health.domain.HealthTotalData;
import lombok.Setter;

@Setter
public class HealthDataDTO {
	private final HealthTotalData totals;

	public HealthDataDTO (HealthTotalData totals) {
		this.totals = totals;
	}

	public String getDiseaseName() {
		return totals.getDisease();
	}


	public Integer getGroupName() {
		return totals.getAge();
	}

	public long getCases() {
		return totals.getCases();
	}

	public String getEthnicity() {
		return totals.getEthnicity();
	}

	public String getRegionName() {
		return totals.getRegion();
	}

	public String getSex() {
		return totals.getSex();
	}

	public Float getPrevalenceRate() {
		return totals.getPrevalenceRate();
	}

	public String getCountyName() {
		return totals.getCounty();
	}

	public Long getPopulation() {
		return totals.getPopulation();
	}

	public Double getCostPerCase() {
		return totals.getCostPerCase();
	}
	
	public String gethealthcareCost() {
		DecimalFormat df = new DecimalFormat("#");
		df.setMaximumFractionDigits(2);
		return df.format(totals.getTotalHCCost());
	}
	
	public String gettotalCost() {
		DecimalFormat df = new DecimalFormat("#");
		df.setMaximumFractionDigits(2);
		return df.format(totals.getTotalTotalCost());
	}

	

	public String getAverageUtility() {
		DecimalFormat df = new DecimalFormat("#");
		df.setMaximumFractionDigits(2);
		return df.format(totals.getAverageUtility());
	}

	public String getAverageHealthyUtility() {
		DecimalFormat df = new DecimalFormat("#");
		df.setMaximumFractionDigits(2);
		return df.format(totals.getAverageHealthyUtility());
	}
	
	public String getutilityLoss() {
		DecimalFormat df = new DecimalFormat("#");
		df.setMaximumFractionDigits(2);
		return df.format(totals.getUtilityLoss());
	}
	
	public String getTotalUtilityLoss() {
		DecimalFormat df = new DecimalFormat("#");
		df.setMaximumFractionDigits(2);
		return df.format(totals.getTotalUtilityLoss());
	}

	public String getTotalCostOfUtility() {
		DecimalFormat df = new DecimalFormat("#");
		df.setMaximumFractionDigits(2);
		return df.format(totals.getCostOfUtility());
	}


}

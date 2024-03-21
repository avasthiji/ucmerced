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

    public String getCosts() {
    	DecimalFormat df = new DecimalFormat("#");
		df.setMaximumFractionDigits(2);
        return df.format(totals.getTotalHCCost());
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
}

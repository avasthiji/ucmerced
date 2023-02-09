package edu.ucmerced.chealth.search;

import edu.ucmerced.chealth.datasource.health.domain.Totals;

public class TotalsDTO {
    private final Totals totals;
    public TotalsDTO (Totals totals) {
        this.totals = totals;
    }

    public String getDiseaseName() {
        return totals.getDisease().getDiseaseName();
    }

    public Double getCosts() {
        return totals.getCosts();
    }

    public String getGroupName() {
        return totals.getAgeGroup().getGroupName();
    }

    public Double getCases() {
        return totals.getCases();
    }

    public String getEthnicity() {
        return totals.getEthnicity().getEthnicityName();
    }

    public String getRegionName() {
        return totals.getRegion().getRegionName();
    }

    public String getSex() {
        return totals.getSex();
    }

    public Float getPrevalenceRate() {
        return totals.getPrevalenceRate();
    }

    public String getCountyName() {
        return totals.getCounty().getCountyName();
    }

    public Long getPopulation() {
        return totals.getPopulation();
    }
}

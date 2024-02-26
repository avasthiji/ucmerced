package edu.ucmerced.chealth.search;

import edu.ucmerced.chealth.datasource.health.domain.Totals;
import lombok.Setter;

@Setter
public class TotalsDTO {
    private final Totals totals;
    public TotalsDTO (Totals totals) {
        this.totals = totals;
    }

    public String getDiseaseName() {
        return totals.getDisease().getDisease();
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
        return totals.getEthnicity().getEthnicity();
    }

    public String getRegionName() {
        return totals.getRegion().getRegion();
    }

    public String getSex() {
        return totals.getSex();
    }

    public Float getPrevalenceRate() {
        return totals.getPrevalenceRate();
    }

    public String getCountyName() {
        return totals.getCounty().getCounty();
    }

    public Long getPopulation() {
        return totals.getPopulation();
    }
}

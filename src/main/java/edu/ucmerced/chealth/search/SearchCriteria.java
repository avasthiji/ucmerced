package edu.ucmerced.chealth.search;

import java.util.List;

import static edu.ucmerced.chealth.search.SearchUtils.notNullList;

/*
 * Criteria for searching health data. Its members are always non-null.
 */

public class SearchCriteria {
    public List<Long> regions;
    public List<Long> counties;
    public List<Long> diseases;
    public List<Long> ethnicities;
    public List<Long> ageGroups;
    public List<String> sexes;

    public SearchCriteria(
        List<Long> regions,
        List<Long> counties,
        List<Long> diseases,
        List<Long> ethnicities,
        List<Long> ageGroups,
        List<String> sexes) {
        this.regions = notNullList(regions);
        this.counties = notNullList(counties);
        this.diseases = notNullList(diseases);
        this.ethnicities = notNullList(ethnicities);
        this.ageGroups = notNullList(ageGroups);
        this.sexes = notNullList(sexes);
    }
}

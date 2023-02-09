package edu.ucmerced.chealth.service;

import com.querydsl.core.BooleanBuilder;
import edu.ucmerced.chealth.datasource.health.domain.QTotals;
import edu.ucmerced.chealth.datasource.health.domain.Totals;
import edu.ucmerced.chealth.datasource.health.repository.TotalsRepository;
import edu.ucmerced.chealth.search.SearchCriteria;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@CommonsLog
public class SearchService {
    private static final QTotals totals = QTotals.totals;

    @Autowired
    private TotalsRepository totalsRepository;

    /**
     * Search for chronic disease data.
     */
    public List<Totals> search(SearchCriteria criteria) {
        BooleanBuilder searchTotals = new BooleanBuilder();
        if (criteria.regions.size() == 1) {
            searchTotals.and(totals.region.id.eq(criteria.regions.get(0)));
        } else if (!criteria.regions.isEmpty()) {
            searchTotals.and(totals.region.id.in(criteria.regions));
        }
        if (criteria.counties.size() == 1) {
            searchTotals.and(totals.county.id.eq(criteria.counties.get(0)));
        } else if (!criteria.counties.isEmpty()) {
            searchTotals.and(totals.county.id.in(criteria.counties));
        }
        if (criteria.diseases.size() == 1) {
            searchTotals.and(totals.disease.id.eq(criteria.diseases.get(0)));
        } else if (!criteria.diseases.isEmpty()) {
            searchTotals.and(totals.disease.id.in(criteria.diseases));
        }
        if (criteria.ethnicities.size() == 1) {
            searchTotals.and(totals.ethnicity.id.eq(criteria.ethnicities.get(0)));
        } else if (!criteria.ethnicities.isEmpty()) {
            searchTotals.and(totals.ethnicity.id.in(criteria.ethnicities));
        }
        if (criteria.ageGroups.size() == 1) {
            searchTotals.and(totals.ageGroup.id.eq(criteria.ageGroups.get(0)));
        } else if (!criteria.ageGroups.isEmpty()) {
            searchTotals.and(totals.ageGroup.id.in(criteria.ageGroups));
        }
        if (criteria.sexes.size() == 1) {
            searchTotals.and(totals.sex.eq(criteria.sexes.get(0)));
        } else if (!criteria.sexes.isEmpty()) {
            searchTotals.and(totals.sex.in(criteria.sexes));
        }
        List<Totals> found = new ArrayList<>();
        totalsRepository.findAll(searchTotals).forEach(found::add);
        return found;
    }
}

package edu.ucmerced.chealth.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.ucmerced.chealth.datasource.health.domain.Totals;
import edu.ucmerced.chealth.search.TotalsDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/*
 * Format health data into the format needed by our front end.
 */

@Service
public class SearchFormatter {
    @Autowired
    private ObjectMapper mapper;

    /**
     * Given some health data, put it into a "search result" structure for use by
     * the front end. The structure is:
     *
     * {
     *     Totals: {
     *         totals: [],
     *         regions: [],
     *         counties: []
     *     },
     *     diseases: [
     *         {
     *             name: "DiseaseName",
     *             totals: [],
     *             regions: [],
     *             counties: []
     *         }
     *     ]
     * }
     */
    public ObjectNode createSearchResult(List<Totals> results, List<Long> regionIds) {
        return mapper.createObjectNode()
                .putPOJO("Totals", createBreakdown(results, regionIds))
                .putPOJO("diseases", getDiseaseResults(results, regionIds));
    }

    private ObjectNode createBreakdown(List<Totals> results, List<Long> regionIds) {
        return addBreakdown(mapper.createObjectNode(), results, regionIds);
    }

    private ObjectNode createNamedBreakdown(String name, List<Totals> results, List<Long> regionIds) {
        return addBreakdown(mapper.createObjectNode().putPOJO("name", name), results, regionIds);
    }

    private ObjectNode addBreakdown(ObjectNode node, List<Totals> results, List<Long> regionIds) {
        return node.putPOJO("totals", createTotals(results))
                .putPOJO("regions", getRegionResults(regionIds, results))
                .putPOJO("counties", createCountyResults(results));
    }

    /**
     * Provide the sums of costs and cases. Oddly, they are always in a list.
     */
    private List<ObjectNode> createTotals(List<Totals> results) {
        double costs = 0;
        double cases = 0;
        for (Totals totals: results) {
            costs += totals.getCosts();
            cases += totals.getCases();
        }
        return Collections.singletonList(mapper.createObjectNode()
                .put("costs", costs).put("cases", cases));
    }

    private List<ObjectNode> createCountyResults(List<Totals> results) {
        return results.stream()
                .collect(Collectors.groupingBy(Totals::getCountyName, Collectors.toUnmodifiableList()))
                .entrySet().stream().sorted(Map.Entry.comparingByKey())
                .map(entry -> createCountyResult(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    /**
     * Create a county result with the following structure:
     *   {"Marin": {"data": [], "totals": [] }}
     */
    private ObjectNode createCountyResult(String name, List<Totals> results) {
        return mapper.createObjectNode().putPOJO(name,
                mapper.createObjectNode()
                        .putPOJO("data", getTotalsDtos(results))
                        .putPOJO("totals", createTotals(results))
        );
    }

    private List<TotalsDTO> getTotalsDtos(List<Totals> totals) {
        return totals.stream().map(TotalsDTO::new).collect(Collectors.toList());
    }

    /**
     * Provide the disease results for a batch of data. Like the county results, we need
     * to gather up the Totals applicable to each disease, then produce a DiseaseResult
     * for the batch.
     */
    private List<ObjectNode> getDiseaseResults(List<Totals> results, List<Long> regionIds) {
        return results.stream()
                .collect(Collectors.groupingBy(Totals::getDiseaseName, Collectors.toUnmodifiableList()))
                .entrySet().stream().sorted(Map.Entry.comparingByKey())
                .map(entry -> createNamedBreakdown(entry.getKey(), entry.getValue(), regionIds))
                .collect(Collectors.toList());
    }

    /**
     * Create a list of "region results", consisting of the region name and the amount of
     * cases and costs per region: { "region name": { cases: #, costs #}}
     */
    private List<ObjectNode> getRegionResults(List<Long> regionIds, List<Totals> results) {
        return results.stream()
                .filter(t -> regionIds.contains(t.getRegionId()))
                .collect(Collectors.groupingBy(Totals::getRegionName, Collectors.toUnmodifiableList()))
                .entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> mapper.createObjectNode()
                        .putPOJO(entry.getKey(), createTotals(entry.getValue())))
                .collect(Collectors.toList());
    }
}

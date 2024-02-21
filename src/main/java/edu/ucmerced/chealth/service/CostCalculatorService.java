package edu.ucmerced.chealth.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.ucmerced.chealth.datasource.health.domain.HealthTotalData;
import edu.ucmerced.chealth.datasource.health.repository.HealthTotalRepository;
import edu.ucmerced.chealth.search.HealthDataDTO;

/*
 * Format health data into the format needed by our front end.
 */

@Service
public class CostCalculatorService {
	@Autowired
	private ObjectMapper mapper;

	@Autowired
	private HealthTotalRepository healthTotalRepository;

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

	public ObjectNode getHealthData(String county , String ethnicity, String disease, String sex, String region) {

		List<String> countyList = new ArrayList<String>(Arrays.asList(county.split(",")));
		List<String> ethnicityList = new ArrayList<String>(Arrays.asList(ethnicity.split(","))); 
		List<String> diseaseList = new ArrayList<String>(Arrays.asList(county.split(","))); 
		List<String> genderList = new ArrayList<String>(Arrays.asList(sex.split(","))); 
		List<String> regionList = new ArrayList<String>(Arrays.asList(region.split(","))); 

		List<HealthTotalData> healthDataList =  healthTotalRepository.retrieveHealthData(0, 5, countyList, ethnicityList, diseaseList, genderList, regionList);

		return createSearchResult(healthDataList, regionList);


	}

	public ObjectNode createSearchResult(List<HealthTotalData> results, List<String> regionList) {
		return mapper.createObjectNode()
				.putPOJO("Totals", createBreakdown(results, regionList))
				.putPOJO("diseases", getDiseaseResults(results, regionList));
	}

	private ObjectNode createBreakdown(List<HealthTotalData> results, List<String> regionIds) {
		return addBreakdown(mapper.createObjectNode(), results, regionIds);
	}

	private ObjectNode addBreakdown(ObjectNode node, List<HealthTotalData> results, List<String> regionIds) {
		return node.putPOJO("totals", createTotals(results))
				.putPOJO("regions", getRegionResults(regionIds, results))
				.putPOJO("counties", createCountyResults(results));
	}

	private ObjectNode createNamedBreakdown(String name, List<HealthTotalData> results, List<String> regionIds) {
		return addBreakdown(mapper.createObjectNode().putPOJO("name", name), results, regionIds);
	}

	private List<ObjectNode> createCountyResults(List<HealthTotalData> results) {
		return results.stream()
				.collect(Collectors.groupingBy(HealthTotalData::getCounty, Collectors.toUnmodifiableList()))
				.entrySet().stream().sorted(Map.Entry.comparingByKey())
				.map(entry -> createCountyResult(entry.getKey(), entry.getValue()))
				.collect(Collectors.toList());
	}

	/**
	 * Create a county result with the following structure:
	 *   {"Marin": {"data": [], "totals": [] }}
	 */
	private ObjectNode createCountyResult(String name, List<HealthTotalData> results) {
		return mapper.createObjectNode().putPOJO(name,
				mapper.createObjectNode()
				.putPOJO("data", getTotalsDtos(results))
				.putPOJO("totals", createTotals(results))
				);
	}

	private List<HealthDataDTO> getTotalsDtos(List<HealthTotalData> totals) {
		return totals.stream().map(HealthDataDTO::new).collect(Collectors.toList());
	}

	/**
	 * Provide the disease results for a batch of data. Like the county results, we need
	 * to gather up the Totals applicable to each disease, then produce a DiseaseResult
	 * for the batch.
	 */
	private List<ObjectNode> getDiseaseResults(List<HealthTotalData> results, List<String> regionIds) {
		return results.stream()
				.collect(Collectors.groupingBy(HealthTotalData::getDisease, Collectors.toUnmodifiableList()))
				.entrySet().stream().sorted(Map.Entry.comparingByKey())
				.map(entry -> createNamedBreakdown(entry.getKey(), entry.getValue(), regionIds))
				.collect(Collectors.toList());
	}

	/**
	 * Create a list of "region results", consisting of the region name and the amount of
	 * cases and costs per region: { "region name": { cases: #, costs #}}
	 */
	private List<ObjectNode> getRegionResults(List<String> regionIds, List<HealthTotalData> results) {
		return results.stream()
				.filter(t -> regionIds.contains(t.getRegion()))
				.collect(Collectors.groupingBy(HealthTotalData::getRegion, Collectors.toUnmodifiableList()))
				.entrySet().stream()
				.sorted(Map.Entry.comparingByKey())
				.map(entry -> mapper.createObjectNode()
						.putPOJO(entry.getKey(), createTotals(entry.getValue())))
				.collect(Collectors.toList());
	}

	/**
	 * Provide the sums of costs and cases. Oddly, they are always in a list.
	 */
	private List<ObjectNode> createTotals(List<HealthTotalData> results) {
		double costs = 0;
		double cases = 0;
		for (HealthTotalData totals: results) {
			costs += totals.getTotalHCCost();
			cases += totals.getCases();
		}
		return Collections.singletonList(mapper.createObjectNode()
				.put("costs", costs).put("cases", cases));
	}
}

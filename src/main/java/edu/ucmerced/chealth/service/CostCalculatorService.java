package edu.ucmerced.chealth.service;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.ucmerced.chealth.datasource.health.domain.HealthTotalData;
import edu.ucmerced.chealth.datasource.health.repository.CountyRepository;
import edu.ucmerced.chealth.datasource.health.repository.DiseaseRepository;
import edu.ucmerced.chealth.datasource.health.repository.EthnicityRepository;
import edu.ucmerced.chealth.datasource.health.repository.HealthTotalRepository;
import edu.ucmerced.chealth.datasource.health.repository.RegionRepository;
import edu.ucmerced.chealth.search.HealthDataPerCaseResponse;

/*
 * Format health data into the format needed by our front end.
 */

@Service
public class CostCalculatorService {
	@Autowired
	private ObjectMapper mapper;

	@Autowired
	private HealthTotalRepository healthTotalRepository;

	@Autowired
	private DiseaseRepository diseaseRepository;

	@Autowired
	private CountyRepository countyRepository;

	@Autowired
	private EthnicityRepository ethnicityRepository;

	@Autowired
	private RegionRepository regionRepository;

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
	public ObjectNode getHealthData(String county, String disease, String ethnicity, String ageGroups, String sexes, String region) {

		List<String> countyList =countyRepository.findByIdIn(Stream.of(county.split(","))
				.map(Long::parseLong)
				.collect(Collectors.toList()));
		List<String> ethnicityList =ethnicityRepository.findByIdIn(Stream.of(ethnicity.split(","))
				.map(Long::parseLong)
				.collect(Collectors.toList()));
		List<String> diseaseList =diseaseRepository.findByIdIn(Stream.of(disease.split(","))
				.map(Long::parseLong)
				.collect(Collectors.toList()));
		List<String> genderList = new
				 ArrayList<String>(Arrays.asList(sexes.split(",")));
		List<String> regionList =regionRepository.findByIdIn(Stream.of(region.split(","))
				.map(Long::parseLong)
				.collect(Collectors.toList()));
		List<Integer> ageList = Stream.of(ageGroups.split("-"))
				.map(Integer::parseInt)
				.collect(Collectors.toList());
		
		List<HealthTotalData> countiesHealthDataList =  healthTotalRepository.retrieveHealthData(ageList.get(0), ageList.get(1), countyList, ethnicityList, diseaseList, genderList, regionList);
		
		List<HealthTotalData> regionHealthDataList =  healthTotalRepository.retrieveHealthData(ageList.get(0), ageList.get(1), ethnicityList, diseaseList, genderList, regionList);

		return createSearchResult(countiesHealthDataList,regionHealthDataList, diseaseList, countyList, regionList);


	}

	public ObjectNode createSearchResult(List<HealthTotalData> countiesHealthDataList,List<HealthTotalData> regionHealthDataList, List<String> diseaseList, 
			 List<String> countyList,  List<String> regionList) {
		return mapper.createObjectNode()
				.putPOJO("Counties", createPerCountyRessults(countiesHealthDataList, diseaseList, countyList))
				.putPOJO("Totals", createRegionResults(regionHealthDataList, diseaseList, regionList.get(0) ));
	}
	
	private HealthDataPerCaseResponse createPerCountyRessults(List<HealthTotalData> countiesHealthDataList, List<String> conditions, List<String> counties) {
		DecimalFormat df = new DecimalFormat("#");
		df.setMaximumFractionDigits(2);
		HealthDataPerCaseResponse healthDataPerCaseResponse = new HealthDataPerCaseResponse();
		healthDataPerCaseResponse.setConditions(String.join(",", conditions));
		healthDataPerCaseResponse.setCounty(String.join(",", counties));
		healthDataPerCaseResponse.setCostPerCase(df.format(countiesHealthDataList.stream().mapToDouble(o->o.getCostPerCase()).average().orElse(0.0)));
		healthDataPerCaseResponse.setUtilityCostPerCase(df.format(countiesHealthDataList.stream().mapToDouble(o->o.getUtilityLoss()).average().orElse(0.0)));
		healthDataPerCaseResponse.setRates(df.format(countiesHealthDataList.stream().mapToDouble(o -> o.getAverageHealthyUtility() - o.getAverageUtility()).average().orElse(0.0)));
		healthDataPerCaseResponse.setCases(df.format(countiesHealthDataList.stream().mapToDouble(o->o.getCases()).sum()));
		healthDataPerCaseResponse.setHealthCareCost(df.format(countiesHealthDataList.stream().mapToDouble(o->o.getTotalHCCost()).sum()));
		healthDataPerCaseResponse.setUtilityLoss(df.format(countiesHealthDataList.stream().mapToDouble(o->o.getUtilityLoss()).sum()));
		healthDataPerCaseResponse.setTotalCost(df.format(countiesHealthDataList.stream().mapToDouble(o->o.getTotalTotalCost()).sum()));

		return healthDataPerCaseResponse;
		
	}
	
	private HealthDataPerCaseResponse createRegionResults(List<HealthTotalData> regionHealthDataList, List<String> conditions, String region) {
		DecimalFormat df = new DecimalFormat("#");
		df.setMaximumFractionDigits(2);
		HealthDataPerCaseResponse healthDataPerCaseResponse = new HealthDataPerCaseResponse();
		healthDataPerCaseResponse.setConditions(String.join(",", conditions));
		healthDataPerCaseResponse.setCounty("All Counties of " + region);
		healthDataPerCaseResponse.setCostPerCase(df.format(regionHealthDataList.stream().mapToDouble(o->o.getCostPerCase()).average().orElse(0.0)));
		healthDataPerCaseResponse.setUtilityCostPerCase(df.format(regionHealthDataList.stream().mapToDouble(o->o.getUtilityLoss()).average().orElse(0.0)));
		healthDataPerCaseResponse.setRates(df.format(regionHealthDataList.stream().mapToDouble(o -> o.getAverageHealthyUtility() - o.getAverageUtility()).average().orElse(0.0)));
		healthDataPerCaseResponse.setCases(df.format(regionHealthDataList.stream().mapToDouble(o->o.getCases()).sum()));
		healthDataPerCaseResponse.setHealthCareCost(df.format(regionHealthDataList.stream().mapToDouble(o->o.getTotalHCCost()).sum()));
		healthDataPerCaseResponse.setUtilityLoss(df.format(regionHealthDataList.stream().mapToDouble(o->o.getUtilityLoss()).sum()));
		healthDataPerCaseResponse.setTotalCost(df.format(regionHealthDataList.stream().mapToDouble(o->o.getTotalTotalCost()).sum()));

		return healthDataPerCaseResponse;
	}

	
	
	
}

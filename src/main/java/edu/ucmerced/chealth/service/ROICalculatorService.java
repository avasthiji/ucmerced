package edu.ucmerced.chealth.service;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;

import edu.ucmerced.chealth.datasource.health.domain.CumulativeROIHealthModel;
import edu.ucmerced.chealth.datasource.health.domain.HealthTotalData;
import edu.ucmerced.chealth.datasource.health.domain.ROICalculatorRequest;
import edu.ucmerced.chealth.datasource.health.domain.ROIHealthModelPerYear;
import edu.ucmerced.chealth.datasource.health.repository.CountyRepository;
import edu.ucmerced.chealth.datasource.health.repository.DiseaseRepository;
import edu.ucmerced.chealth.datasource.health.repository.EthnicityRepository;
import edu.ucmerced.chealth.datasource.health.repository.HealthTotalRepository;
import edu.ucmerced.chealth.datasource.health.repository.RegionRepository;

@Service
public class ROICalculatorService {

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

	@Value("${utilityLossRate}")
	private double utilityLossRate;

	public Map<String, Object> getROIData(ROICalculatorRequest request) {

		List<String> countyList =countyRepository.findByIdIn(Stream.of(request.getCountyName().split(","))
				.map(Long::parseLong)
				.collect(Collectors.toList()));
		List<String> ethnicityList =ethnicityRepository.findByIdIn(Stream.of(request.getEthnicity().split(","))
				.map(Long::parseLong)
				.collect(Collectors.toList()));
		List<String> diseaseList =diseaseRepository.findByIdIn(Stream.of(request.getDiseaseName().split(","))
				.map(Long::parseLong)
				.collect(Collectors.toList()));
		List<String> genderList = new
				ArrayList<String>(Arrays.asList(request.getSex().split(",")));
		List<String> regionList =regionRepository.findByIdIn(Stream.of(request.getRegionName().split(","))
				.map(Long::parseLong)
				.collect(Collectors.toList()));
		List<Integer> ageList =  Stream.of(request.getAgeLimit().split("-"))
				.map(Integer::parseInt)
				.collect(Collectors.toList());
		/*
		 * 1. Disease will be one at a time 
		 * 2. As with the cost calculator, we have to be able to do this by gender, ethnicity, and region -> response will set grouping based on this three(not counties)
		 * 3. for each grouping, initial population will be (total number of cases for that particular age for all gender, ethnicity, and region ) 
		 * */

		Map<String, Object> responseMap = new HashMap<String, Object>();

		Map<String, CumulativeROIHealthModel> responseTotalAgemap = new HashMap<String, CumulativeROIHealthModel>();
		Map<String, Map<String, CumulativeROIHealthModel>> responseRegionAgemap = new HashMap<String, Map<String,CumulativeROIHealthModel>>();
		Map<String, Map<String, CumulativeROIHealthModel>> responsegenderAgemap = new HashMap<String, Map<String,CumulativeROIHealthModel>>();
		Map<String, Map<String, CumulativeROIHealthModel>> responseEthnicityAgemap = new HashMap<String, Map<String,CumulativeROIHealthModel>>();

		Map<String, CumulativeROIHealthModel> genderMapResp = new HashMap<String, CumulativeROIHealthModel>();
		Map<String, CumulativeROIHealthModel> regionMapResp = new HashMap<String, CumulativeROIHealthModel>();
		Map<String, CumulativeROIHealthModel> ethnicityMapResp = new HashMap<String, CumulativeROIHealthModel>();
		int startAge = ageList.get(0);
		int endAge = 0;
		for(int i = startAge ; i<= ageList.get(1) ; i++) {

			endAge = i + (request.getNumberOfFollowUpYears() - 1);
			String key = i + "-" + endAge;
			List<HealthTotalData> healthTotalDataList =  healthTotalRepository.retrieveHealthData(i, endAge, countyList, ethnicityList, diseaseList, genderList, regionList);
			Map<String, List<HealthTotalData>> genderMap = getGenderMap(healthTotalDataList);
			Map<String, List<HealthTotalData>> regionMap= getRegionMap(healthTotalDataList);
			Map<String, List<HealthTotalData>> ethnicityMap = getEthnicityMap(healthTotalDataList);

			genderMapResp = genderMap
					.entrySet()
					.stream()
					.collect(Collectors.toMap(
							Map.Entry::getKey,
							entry -> getTotalsDtos(entry.getValue(), request, startAge, null, entry.getKey(), null, null)
							));

			regionMapResp = regionMap 
					.entrySet()
					.stream() 
					.collect(Collectors.toMap( 
							Map.Entry::getKey, entry -> getTotalsDtos(entry.getValue(), request, startAge, null,null,entry.getKey(),null) ));

			ethnicityMapResp = ethnicityMap
					.entrySet() 
					.stream() 
					.collect(Collectors.toMap(
							Map.Entry::getKey, entry -> getTotalsDtos(entry.getValue(), request, startAge, null, null, null, entry.getKey())));

			CumulativeROIHealthModel response = getTotalsDtos(healthTotalDataList, request, startAge, "total", null, null, null);
			responseTotalAgemap.put(key,response);

			for (Entry<String, CumulativeROIHealthModel> entry : genderMapResp.entrySet()) {
				Map<String, CumulativeROIHealthModel> tempMap = new HashMap<String, CumulativeROIHealthModel>();
				if(!responsegenderAgemap.containsKey(entry.getKey())) {
					tempMap.put(key, entry.getValue());
				}
				else {
					tempMap = responsegenderAgemap.get(entry.getKey());
					tempMap.put(key, entry.getValue());
				}
				responsegenderAgemap.put(entry.getKey(), tempMap);
			}

			for (Entry<String, CumulativeROIHealthModel> entry : regionMapResp.entrySet()) {
				Map<String, CumulativeROIHealthModel> tempMap = new HashMap<String, CumulativeROIHealthModel>();
				if(!responseRegionAgemap.containsKey(entry.getKey())) {
					tempMap.put(key, entry.getValue());
				}
				else {
					tempMap = responseRegionAgemap.get(entry.getKey());
					tempMap.put(key, entry.getValue());
				}
				responseRegionAgemap.put(entry.getKey(), tempMap);
			}

			for (Entry<String, CumulativeROIHealthModel> entry : ethnicityMapResp.entrySet()) {
				Map<String, CumulativeROIHealthModel> tempMap = new HashMap<String, CumulativeROIHealthModel>();
				if(!responseEthnicityAgemap.containsKey(entry.getKey())) {
					tempMap.put(key, entry.getValue());
				}
				else {
					tempMap = responseEthnicityAgemap.get(entry.getKey());
					tempMap.put(key, entry.getValue());
				}
				responseEthnicityAgemap.put(entry.getKey(), tempMap);
			}
		}
		responseMap.put("Total", responseTotalAgemap);
		responseMap.put("Gender", responsegenderAgemap);
		responseMap.put("Ethnicity", responseEthnicityAgemap);
		responseMap.put("Region", responseRegionAgemap);

		return responseMap;
	}

	private CumulativeROIHealthModel getTotalsDtos(List<HealthTotalData> totals, ROICalculatorRequest request, int age, String total, String gender, String region, String ethnicity) {

		//Total cases will be total number of cases for that particular age for given gender, ethnicity and region
		Gson gson = new Gson();
		Map<String, Double> ageMap = gson.fromJson(request.getSizeOfGroup().toString(), Map.class);

		double utilityLoss = 0.0;
		double totalCases = 0.0;
		if(!StringUtils.isEmpty(total)) {
			utilityLoss = totals.stream().filter(o -> o.getAge() == age).mapToDouble(o -> o.getAverageHealthyUtility() - o.getAverageUtility()).sum();
			totalCases = totals.stream().filter(o -> o.getAge() == age).mapToDouble(o -> o.getCases()).sum();
		}else if(!StringUtils.isEmpty(gender)) {
			utilityLoss = totals.stream().filter(o -> o.getAge() == age && gender.equals(o.getSex())).mapToDouble(o -> o.getAverageHealthyUtility() - o.getAverageUtility()).sum();
			totalCases = totals.stream().filter(o -> o.getAge() == age && gender.equals(o.getSex())).mapToDouble(o -> o.getCases()).sum();
		}else if(!StringUtils.isEmpty(region)) {
			utilityLoss = totals.stream().filter(o -> o.getAge() == age && region.equals(o.getRegion())).mapToDouble(o -> o.getAverageHealthyUtility() - o.getAverageUtility()).sum();
			totalCases = totals.stream().filter(o -> o.getAge() == age && region.equals(o.getRegion())).mapToDouble(o -> o.getCases()).sum();
		}else if(!StringUtils.isEmpty(ethnicity)) {
			utilityLoss = totals.stream().filter(o -> o.getAge() == age && ethnicity.equals(o.getEthnicity())).mapToDouble(o -> o.getAverageHealthyUtility() - o.getAverageUtility()).sum();
			totalCases = totals.stream().filter(o -> o.getAge() == age && ethnicity.equals(o.getEthnicity())).mapToDouble(o -> o.getCases()).sum();

		}
		double costPerCase = totals.get(0).getCostPerCase();
		Map<Integer, Long> populationMapWithProgram = populationCalculatorWithProgram(ageMap.get(Integer.toString(age)).longValue(),request.getNumberOfFollowUpYears(), 
				request.getReductionInRateWithProgram(), request.getReductionInRateAfterYearsWithProgram(), request.getPercentIncreateInCasePerYear());

		Map<Integer, Long> populationMapWithoutProgram = populationCalculatorWithoutProgram(ageMap.get(Integer.toString(age)).longValue(),request.getNumberOfFollowUpYears(), 
				request.getPercentIncreateInCasePerYear());

		List<ROIHealthModelPerYear> roiHealthModelPerYears = new ArrayList<ROIHealthModelPerYear>();
		CumulativeROIHealthModel cumulativeROIHealthModel = new CumulativeROIHealthModel();
		DecimalFormat df = new DecimalFormat("#");
		df.setMaximumFractionDigits(3);
		int iter = 0;
		long startAge = age;
		while(iter < request.getNumberOfFollowUpYears()) {
			iter++;
			ROIHealthModelPerYear healthModelPerYear = new ROIHealthModelPerYear();
			healthModelPerYear.setAge((int)startAge);
			//healthModelPerYear.setTotalCases(Math.round(totalCases));
			healthModelPerYear.setNumberOfPeopleWithProgram(populationMapWithProgram.get(iter));
			healthModelPerYear.setNumberOfPeopleWithOutProgram(populationMapWithoutProgram.get(iter));
			healthModelPerYear.setDifference(healthModelPerYear.getNumberOfPeopleWithOutProgram() - healthModelPerYear.getNumberOfPeopleWithProgram());
			healthModelPerYear.setInvestmentPerPerson(request.getInvestmentPerPerson());
			double totalCostSaving = healthModelPerYear.getInvestmentPerPerson() * healthModelPerYear.getDifference();
			healthModelPerYear.setCostSavingPerYear(df.format(totalCostSaving));
			healthModelPerYear.setDiscountedCostSavingsPerYear(df.format(generateDiscountedCostSaving(request.getDiscountedfactor() , totalCostSaving)));
			healthModelPerYear.setTotalCost(df.format(healthModelPerYear.getNumberOfPeopleWithOutProgram() * request.getInvestmentPerPerson()));
			if(iter != age)
				utilityLoss = utilityLoss + utilityLossRate;
			healthModelPerYear.setUtilityCost(df.format(healthModelPerYear.getNumberOfPeopleWithOutProgram() * utilityLoss * costPerCase));
			roiHealthModelPerYears.add(healthModelPerYear);
			startAge++;
		}
		
		double totalUtilityLoss = roiHealthModelPerYears.stream().mapToDouble(o -> Double.valueOf(o.getUtilityCost())).sum();
		double totalSaving = roiHealthModelPerYears.stream().mapToDouble(o -> Double.valueOf(o.getCostSavingPerYear())).sum();
		double totalDiscountedSavings = roiHealthModelPerYears.stream().mapToDouble(o -> Double.valueOf(o.getDiscountedCostSavingsPerYear())).sum();
		double totalInvestmest = roiHealthModelPerYears.stream().mapToDouble(o -> Double.valueOf(o.getInvestmentPerPerson())).sum();
		cumulativeROIHealthModel.setROINonDiscounted(df.format((totalSaving + totalUtilityLoss) / totalInvestmest));
		cumulativeROIHealthModel.setROIDiscounted(df.format((totalDiscountedSavings + totalUtilityLoss) / totalInvestmest));
		cumulativeROIHealthModel.setDiscountedCostSaving(df.format(totalDiscountedSavings / totalInvestmest));
		cumulativeROIHealthModel.setTotalcost(df.format(roiHealthModelPerYears.stream().mapToDouble(o -> o.getInvestmentPerPerson()).sum()));
		cumulativeROIHealthModel.setTotalUtilityCost(df.format(roiHealthModelPerYears.stream().mapToDouble(o -> Double.valueOf(o.getDiscountedCostSavingsPerYear())).sum()));
		cumulativeROIHealthModel.setTotalCostSaving(df.format(totalSaving));
		cumulativeROIHealthModel.setDiscountedCostSaving(df.format(totalDiscountedSavings));
		cumulativeROIHealthModel.setAgeRange(roiHealthModelPerYears);

		return cumulativeROIHealthModel; 
	}

	public Map<Integer, Long> populationCalculatorWithoutProgram(long initialPopulation, int numnerOfFollowupYears, float percentIncreateInCasePerYear){
		Map<Integer, Long> agePopulationMap = new HashMap<Integer, Long>();
		long prevPopulation = 0;
		int iterator = 0;
		while(iterator < numnerOfFollowupYears) {
			iterator++;
			if(agePopulationMap.size() == 0) {
				prevPopulation = initialPopulation;
				agePopulationMap.put(iterator, prevPopulation);
			}
			else {
				agePopulationMap.put(iterator, (long)  Math.round(prevPopulation + (prevPopulation * (percentIncreateInCasePerYear/100))));
				prevPopulation = Math.round(prevPopulation + (prevPopulation * (percentIncreateInCasePerYear/100)));
			}
		}
		return agePopulationMap;
	}

	public Map<Integer, Long> populationCalculatorWithProgram(long initialPopulation, int numnerOfFollowupYears, float reductionRate, int reductionRateAfteryear, float percentIncreateInCasePerYear){
		Map<Integer, Long> agePopulationMap = new HashMap<Integer, Long>();
		long prevPopulation = 0;
		int i = 0;
		int iterator = 0;
		while(iterator < numnerOfFollowupYears) {
			i++;
			iterator++;
			if(agePopulationMap.size() == 0) {
				prevPopulation = initialPopulation;
				agePopulationMap.put(iterator, prevPopulation);
				continue;
			}
			if(i > reductionRateAfteryear) {
				agePopulationMap.put(iterator, (long)  Math.round(prevPopulation + (prevPopulation * ((percentIncreateInCasePerYear- reductionRate)/100))));
				prevPopulation = Math.round(prevPopulation + (prevPopulation * (percentIncreateInCasePerYear/100)));
			}else {
				agePopulationMap.put(iterator, (long)  Math.round(prevPopulation + (prevPopulation * ((percentIncreateInCasePerYear)/100))));
				prevPopulation = Math.round(prevPopulation + (prevPopulation * (percentIncreateInCasePerYear/100)));
			}
		}
		return agePopulationMap;
	}

	public double generateDiscountedCostSaving(float discountFacor, double costSavingPerYear) {
		return costSavingPerYear/discountFacor; 
	}

	public float calculateRoi(float netSaving, float investment) {
		return (netSaving / investment) ;
	}

	public static Map<String, List<HealthTotalData>> getGenderMap(List<HealthTotalData> totalDataList) {
		return totalDataList.stream()
				.collect(Collectors.groupingBy(HealthTotalData::getSex));
	}
	public static Map<String, List<HealthTotalData>> getEthnicityMap(List<HealthTotalData> totalDataList) {
		return totalDataList.stream()
				.collect(Collectors.groupingBy(HealthTotalData::getEthnicity));
	}
	public static Map<String, List<HealthTotalData>> getRegionMap(List<HealthTotalData> totalDataList) {
		return totalDataList.stream()
				.collect(Collectors.groupingBy(HealthTotalData::getRegion));
	}

}

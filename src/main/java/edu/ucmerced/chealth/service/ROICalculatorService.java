package edu.ucmerced.chealth.service;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

	public Map<String, CumulativeROIHealthModel> getROIData(ROICalculatorRequest request) {

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
		Map<String, CumulativeROIHealthModel> responsemap = new HashMap<String, CumulativeROIHealthModel>();

		int startAge = ageList.get(0);
		int endAge = 0;
		for(int i = startAge ; i<= ageList.get(1) ; i++) {

			endAge = i + (request.getNumberOfFollowUpYears() - 1);
			String key = i + "-" + endAge;
			List<HealthTotalData> healthTotalDataList =  healthTotalRepository.retrieveHealthData(i, endAge, countyList, ethnicityList, diseaseList, genderList, regionList);
			CumulativeROIHealthModel response = getTotalsDtos(healthTotalDataList, request);
			responsemap.put(key,response);
		}

		return responsemap;
	}

	private CumulativeROIHealthModel getTotalsDtos(List<HealthTotalData> totals, ROICalculatorRequest request) {
		/*
		 * Map<Integer, Float> ageRateMap = totals.stream().collect(
		 * Collectors.toMap(HealthTotalData::getAge,
		 * HealthTotalData::getPrevalenceRate));
		 * 
		 * System.out.println(ageRateMap);
		 */

		Map<Integer, Long> populationMapWithProgram = populationCalculatorWithProgram(totals.get(0).getCases(),request.getNumberOfFollowUpYears(), request.getReductionInRateWithProgram(), request.getReductionInRateAfterYearsWithProgram(), request.getPercentIncreateInCasePerYear());

		System.out.println(populationMapWithProgram);

		Map<Integer, Long> populationMapWithoutProgram = populationCalculatorWithoutProgram(totals.get(0).getCases(),request.getNumberOfFollowUpYears(), request.getPercentIncreateInCasePerYear());

		System.out.println(populationMapWithoutProgram);
		List<ROIHealthModelPerYear> roiHealthModelPerYears = new ArrayList<ROIHealthModelPerYear>();
		CumulativeROIHealthModel cumulativeROIHealthModel = new CumulativeROIHealthModel();
		Map<Integer, List<ROIHealthModelPerYear>> map = new HashMap<Integer, List<ROIHealthModelPerYear>>();
		DecimalFormat df = new DecimalFormat("#");
		df.setMaximumFractionDigits(2);
		int iter = 0;
		for(HealthTotalData data : totals) {
			iter++;
			ROIHealthModelPerYear healthModelPerYear = new ROIHealthModelPerYear();
			healthModelPerYear.setAge(data.getAge());
			healthModelPerYear.setCountyName(data.getCounty());
			healthModelPerYear.setDiseaseName(data.getDisease());
			healthModelPerYear.setEthnicity(data.getEthnicity());
			healthModelPerYear.setPopulation(data.getPopulation());
			healthModelPerYear.setRegionName(data.getRegion());
			healthModelPerYear.setSex(data.getSex());
			System.out.println(iter);
			healthModelPerYear.setNumberOfPeopleWithProgram(populationMapWithProgram.get(iter));
			healthModelPerYear.setNumberOfPeopleWithOutProgram(populationMapWithoutProgram.get(iter));
			healthModelPerYear.setDifference(healthModelPerYear.getNumberOfPeopleWithOutProgram() - healthModelPerYear.getNumberOfPeopleWithProgram());
			healthModelPerYear.setInvestmentPerPerson(request.getInvestmentPerPerson());
			double totalCostSaving = healthModelPerYear.getInvestmentPerPerson() * healthModelPerYear.getDifference();
			healthModelPerYear.setCostSavingPerYear(df.format(totalCostSaving));
			healthModelPerYear.setDiscountedCostSavingsPerYear(df.format(generateDiscountedCostSaving(request.getDiscountedfactor() , totalCostSaving)));
			healthModelPerYear.setTotalCost(df.format(healthModelPerYear.getNumberOfPeopleWithOutProgram() * request.getInvestmentPerPerson()));
			healthModelPerYear.setUtilityCost(df.format(healthModelPerYear.getNumberOfPeopleWithOutProgram() * data.getUtilityLoss()));
			roiHealthModelPerYears.add(healthModelPerYear);

		}

		map.put(totals.get(0).getAge(), roiHealthModelPerYears);
		cumulativeROIHealthModel.setAgemap(map);
		double totalSaving = roiHealthModelPerYears.stream().mapToDouble(o -> Double.valueOf(o.getCostSavingPerYear())).sum();
		double totalDiscountedSavings = roiHealthModelPerYears.stream().mapToDouble(o -> Double.valueOf(o.getDiscountedCostSavingsPerYear())).sum();
		double totalInvestmest = roiHealthModelPerYears.stream().mapToDouble(o -> Double.valueOf(o.getInvestmentPerPerson())).sum();
		cumulativeROIHealthModel.setROINonDiscounted(df.format(totalSaving / totalInvestmest));
		cumulativeROIHealthModel.setROIDiscounted(df.format(totalDiscountedSavings / totalInvestmest));
		cumulativeROIHealthModel.setDiscountedCostSaving(df.format(totalDiscountedSavings / totalInvestmest));
		cumulativeROIHealthModel.setTotalcost(df.format(roiHealthModelPerYears.stream().mapToDouble(o -> o.getInvestmentPerPerson()).sum()));
		cumulativeROIHealthModel.setTotalUtilityCost(df.format(roiHealthModelPerYears.stream().mapToDouble(o -> Double.valueOf(o.getDiscountedCostSavingsPerYear())).sum()));
		cumulativeROIHealthModel.setTotalCostSaving(df.format(totalSaving));
		cumulativeROIHealthModel.setDiscountedCostSaving(df.format(totalDiscountedSavings));


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



}

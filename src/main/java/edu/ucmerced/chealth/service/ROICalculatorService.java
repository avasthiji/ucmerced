package edu.ucmerced.chealth.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;

import edu.ucmerced.chealth.datasource.health.domain.CumulativeROIHealthModel;
import edu.ucmerced.chealth.datasource.health.domain.HealthTotalData;
import edu.ucmerced.chealth.datasource.health.domain.ROICalculatorRequest;
import edu.ucmerced.chealth.datasource.health.domain.ROIHealthModelPerYear;
import edu.ucmerced.chealth.datasource.health.repository.HealthTotalRepository;

@Service
public class ROICalculatorService {

	@Autowired
	private HealthTotalRepository healthTotalRepository;

	public CumulativeROIHealthModel getROIData(ROICalculatorRequest request) {

		/*
		 * ROICalculatorRequest calculatorRequest = new ROICalculatorRequest();
		 * calculatorRequest.setCountyName("Alameda");
		 * calculatorRequest.setDiscountedfactor(1.2f);
		 * calculatorRequest.setDiseaseName("Arthritis");
		 * calculatorRequest.setEthnicity("African American");
		 * calculatorRequest.setInvestmentPerPerson(5000);
		 * calculatorRequest.setNumberOfFollowUpYears(5);
		 * calculatorRequest.setReductionInRateAfterYearsWithProgram(5);
		 * calculatorRequest.setReductionInRateWithProgram(2);
		 * calculatorRequest.setRegionName("BAY AREA COUNTIES");
		 * calculatorRequest.setSex("Female"); calculatorRequest.setSizeOfGroup(0);
		 * calculatorRequest.setStartAge(1); calculatorRequest.setUnDouscounted(false);
		 */

		List<String> countyList = new ArrayList<String>(Arrays.asList(request.getCountyName().split(",")));
		List<String> ethnicityList = new ArrayList<String>(Arrays.asList(request.getEthnicity().split(","))); 
		List<String> diseaseList = new ArrayList<String>(Arrays.asList(request.getCountyName().split(","))); 
		List<String> genderList = new ArrayList<String>(Arrays.asList(request.getSex().split(","))); 
		List<String> regionList = new ArrayList<String>(Arrays.asList(request.getRegionName().split(","))); 

		List<HealthTotalData> healthTotalDataList =  healthTotalRepository.retrieveHealthData(25, 35, countyList, ethnicityList, diseaseList, genderList, regionList);

		CumulativeROIHealthModel response = getTotalsDtos(healthTotalDataList, request);
		Gson gson = new Gson();
		System.out.println(gson.toJson(response));
		return response;
	}

	private CumulativeROIHealthModel getTotalsDtos(List<HealthTotalData> totals, ROICalculatorRequest request) {
		//SQL will return age from 25-35 
		Map<Integer, Float> ageRateMap = totals.stream().collect(
				Collectors.toMap(HealthTotalData::getAge, HealthTotalData::getPrevalenceRate));

		System.out.println(ageRateMap);

		Map<Integer, Double> populationMapWithProgram = populationCalculatorWithProgram(totals.get(0).getPopulation(), ageRateMap,
				request.getReductionInRateWithProgram(), request.getReductionInRateAfterYearsWithProgram());

		System.out.println(populationMapWithProgram);

		Map<Integer, Double> populationMapWithoutProgram = populationCalculatorWithoutProgram(totals.get(0).getPopulation(), ageRateMap);

		System.out.println(populationMapWithoutProgram);
		List<ROIHealthModelPerYear> roiHealthModelPerYears = new ArrayList<ROIHealthModelPerYear>();
		CumulativeROIHealthModel cumulativeROIHealthModel = new CumulativeROIHealthModel();
		Map<Integer, List<ROIHealthModelPerYear>> map = new HashMap<Integer, List<ROIHealthModelPerYear>>();
		for(HealthTotalData data : totals) {
			ROIHealthModelPerYear healthModelPerYear = new ROIHealthModelPerYear();
			healthModelPerYear.setAge(data.getAge());
			healthModelPerYear.setCountyName(data.getCounty());
			healthModelPerYear.setDiseaseName(data.getDisease());
			healthModelPerYear.setEthnicity(data.getEthnicity());
			healthModelPerYear.setPopulation(data.getPopulation());
			healthModelPerYear.setRegionName(data.getRegion());
			healthModelPerYear.setSex(data.getSex());
			healthModelPerYear.setNumberOfPeopleWithProgram(populationMapWithProgram.get(data.getAge()));
			healthModelPerYear.setNumberOfPeopleWithOutProgram(populationMapWithoutProgram.get(data.getAge()));
			healthModelPerYear.setDifference(healthModelPerYear.getNumberOfPeopleWithOutProgram() - healthModelPerYear.getNumberOfPeopleWithProgram());
			healthModelPerYear.setInvestmentPerPerson(request.getInvestmentPerPerson());
			healthModelPerYear.setCostSavingPerYear(healthModelPerYear.getInvestmentPerPerson() * healthModelPerYear.getDifference());
			healthModelPerYear.setDiscountedCostSavingsPerYear(generateDiscountedCostSaving(request.getDiscountedfactor() , healthModelPerYear.getCostSavingPerYear()));
			healthModelPerYear.setTotalCost(healthModelPerYear.getNumberOfPeopleWithOutProgram() * request.getInvestmentPerPerson());
			healthModelPerYear.setUtilityCost(healthModelPerYear.getNumberOfPeopleWithOutProgram() * data.getUtilityLoss());
			roiHealthModelPerYears.add(healthModelPerYear);

		}
		map.put(totals.get(0).getAge(), roiHealthModelPerYears);
		cumulativeROIHealthModel.setAgemap(map);
		double totalSaving = roiHealthModelPerYears.stream().mapToDouble(o -> o.getCostSavingPerYear()).sum();
		double totalDiscountedSavings = roiHealthModelPerYears.stream().mapToDouble(o -> o.getDiscountedCostSavingsPerYear()).sum();
		double totalInvestmest = roiHealthModelPerYears.stream().mapToDouble(o -> o.getDiscountedCostSavingsPerYear()).sum();
		cumulativeROIHealthModel.setROINonDiscounted(totalSaving / totalInvestmest);
		cumulativeROIHealthModel.setDiscountedCostSaving(totalDiscountedSavings / totalInvestmest);
		cumulativeROIHealthModel.setTotalcost(roiHealthModelPerYears.stream().mapToDouble(o -> o.getInvestmentPerPerson()).sum());
		cumulativeROIHealthModel.setTotalUtilityCost(roiHealthModelPerYears.stream().mapToDouble(o -> o.getDiscountedCostSavingsPerYear()).sum());
		cumulativeROIHealthModel.setTotalCostSaving(totalSaving);
		cumulativeROIHealthModel.setDiscountedCostSaving(totalDiscountedSavings);


		return cumulativeROIHealthModel; 
	}

	public Map<Integer, Double> populationCalculatorWithoutProgram(float initailPopulation , Map<Integer, Float> ageRateMap){
		Map<Integer, Double> agePopulationMap = new HashMap<Integer, Double>();
		for (Map.Entry<Integer, Float> map : ageRateMap.entrySet()) {
			agePopulationMap.put(map.getKey(), (double) (initailPopulation * map.getValue()));
		}
		return agePopulationMap;
	}

	public Map<Integer, Double> populationCalculatorWithProgram(float initailPopulation , Map<Integer, Float> ageRateMap, float reductionRate, int reductionRateAfteryear){
		Map<Integer, Double> agePopulationMap = new HashMap<Integer, Double>();
		int i = 0;
		for (Map.Entry<Integer, Float> map : ageRateMap.entrySet()) {
			i++;
			if(i > reductionRateAfteryear) {
				agePopulationMap.put(map.getKey(), (double) (initailPopulation * (map.getValue() - reductionRate)));
			}else {
				agePopulationMap.put(map.getKey(), (double) (initailPopulation * map.getValue()));
			}
		}
		return agePopulationMap;
	}

	public double generateDiscountedCostSaving(float discountFacor, double costSavingPerYear) {
		float discountRate = (1 / discountFacor); 
		return costSavingPerYear * discountRate; 
	}

	public float calculateRoi(float netSaving, float investment) {
		return (netSaving / investment) ;
	}



}

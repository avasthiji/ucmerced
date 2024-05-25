package edu.ucmerced.chealth.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
public class ROICalculatorServiceImpl {

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


	@Value("${percentIncreaseInCasePerYear}")
	private float percentIncreaseInCasePerYear;

	public Map<String, Object> getROIData(ROICalculatorRequest request) {

		List<String> countyList =countyRepository.findByIdIn(Stream.of(request.getCounty().split(","))
				.map(Long::parseLong)
				.collect(Collectors.toList()));
		List<String> ethnicityList =ethnicityRepository.findByIdIn(Stream.of(request.getEthnicity().split(","))
				.map(Long::parseLong)
				.collect(Collectors.toList()));
		List<String> diseaseList =diseaseRepository.findByIdIn(Stream.of(request.getDisease().split(","))
				.map(Long::parseLong)
				.collect(Collectors.toList()));
		List<String> genderList = new
				ArrayList<String>(Arrays.asList(request.getSex().split(",")));
		List<String> regionList =regionRepository.findByIdIn(Stream.of(request.getRegion().split(","))
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

		int startAge = ageList.get(0);
		int endAge = 0;
		for(int i = startAge ; i<= ageList.get(1) ; i++) {

			endAge = i + (request.getNumberOfFollowUpYears() - 1);
			String key = i + "-" + endAge;
			List<HealthTotalData> healthTotalDataList =  healthTotalRepository.retrieveHealthData(i, endAge, countyList, ethnicityList, diseaseList, genderList, regionList);

			CumulativeROIHealthModel response = getTotalsDtos(healthTotalDataList, request, startAge, countyList, ethnicityList, diseaseList, genderList, regionList );
			responseTotalAgemap.put(key,response);

		}
		responseMap.put("Total", responseTotalAgemap);

		return responseMap;
	}

	@SuppressWarnings("removal")
	private CumulativeROIHealthModel getTotalsDtos(List<HealthTotalData> totals, ROICalculatorRequest request, int age, List<String> countyList , List<String> ethnicityList, 
			List<String> diseaseList, List<String> genderList, List<String> regionList) {

		List<ROIHealthModelPerYear> roiHealthModelPerYears = new ArrayList<ROIHealthModelPerYear>();
		CumulativeROIHealthModel cumulativeROIHealthModel = new CumulativeROIHealthModel();
		DecimalFormat df = new DecimalFormat("#");
		df.setMaximumFractionDigits(2);
		int iter = 1;
		long startAge = age;
		double population = 0;
		double cases = 0.0;

		double discountRate = 0.0f;
		float investment = 0.0f;
		while(iter <= request.getNumberOfFollowUpYears()) {

			final Integer newStartAge =  startAge < 80 ?  new Integer((int) startAge) : 80;
			
			if(request.getSizeOfGroup() == 0) {
				population = totals.stream().filter(o -> o.getAge() == Integer.valueOf(newStartAge)).mapToDouble(o -> o.getPopulation()).sum();
			}
			else {
				population = request.getSizeOfGroup();
			}
			//discountRate = (iter == 1) ? 1.0f : BigDecimal.valueOf(discountRate -  (discountRate * request.getDiscountRate()/100)).setScale(2,RoundingMode.DOWN).doubleValue();			
			discountRate = BigDecimal.valueOf(getDiscount(iter)).setScale(2,RoundingMode.DOWN).doubleValue();
			investment = (iter == 1) ? (request.getInitialProgramCost() + request.getOngoingProgramCost()) : request.getOngoingProgramCost();
			ROIHealthModelPerYear healthModelPerYear = new ROIHealthModelPerYear();
			healthModelPerYear.setConditions(String.join(",", diseaseList));
			healthModelPerYear.setRegion(String.join(",", regionList));
			healthModelPerYear.setCounty(String.join(",", countyList));
			healthModelPerYear.setEthnicity(String.join(",", ethnicityList));
			healthModelPerYear.setGender(String.join(",", genderList));
			healthModelPerYear.setAge((int)startAge);
			healthModelPerYear.setYear(iter);
			healthModelPerYear.setDiscount (discountRate);

			//double prevRate = totals.stream().filter(o -> o.getAge() == newStartAge).mapToDouble(o -> o.getPrevalenceRate()).average().orElse(0.0)/100;
			double prevRate = getAveragePrevAge(totals, newStartAge);
			double anticipatedPrevRate = prevRate;
			if(iter > request.getAnticipatedTimeForEffectivenessOfProgram()) {
				anticipatedPrevRate = prevRate - (prevRate * request.getAnticipatedEffectivenessOfProgram())/100;
				}
			healthModelPerYear.setPrevRateAfter(anticipatedPrevRate);
			healthModelPerYear.setPrevRateInitial(prevRate);
			
			healthModelPerYear.setCostPerCase(getCostPerCase(totals, newStartAge)); 
			
			healthModelPerYear.setPopulation(population);
			healthModelPerYear.setCasesBeforeProgram(population * prevRate);
			healthModelPerYear.setCasesAfterProgram(population * anticipatedPrevRate);
			
			double utilityDiff = getAverageUtilityDiff(totals, newStartAge);
			healthModelPerYear.setUtilityDiffAfterWithDiscount(utilityDiff * discountRate);
			healthModelPerYear.setUtilityDiffInitialWithoutDiscount(utilityDiff);
			
			healthModelPerYear.setUtilityLossDiscountedInitial(healthModelPerYear.getUtilityDiffAfterWithDiscount() * healthModelPerYear.getCasesBeforeProgram());
			healthModelPerYear.setUtilityLossDiscountedAfter(healthModelPerYear.getUtilityDiffAfterWithDiscount() * healthModelPerYear.getCasesAfterProgram());
			healthModelPerYear.setUtilityLossDiscountedDiff(healthModelPerYear.getUtilityLossDiscountedInitial() - healthModelPerYear.getUtilityLossDiscountedAfter());
			
			healthModelPerYear.setUtilityLossAfter(healthModelPerYear.getUtilityDiffInitialWithoutDiscount() * healthModelPerYear.getCasesAfterProgram());
			healthModelPerYear.setUtilityLossInitial(healthModelPerYear.getUtilityDiffInitialWithoutDiscount() * healthModelPerYear.getCasesBeforeProgram());
			healthModelPerYear.setUtilityLossDiff(healthModelPerYear.getUtilityLossInitial() - healthModelPerYear.getUtilityLossAfter());
			
			healthModelPerYear.setHealthcareCostInitial(healthModelPerYear.getCasesBeforeProgram() * totals.get(0).getCostPerCase());
			healthModelPerYear.setHealthcareCostAfter(healthModelPerYear.getCasesAfterProgram() * totals.get(0).getCostPerCase());
			healthModelPerYear.setHealthcareCostDiff(healthModelPerYear.getHealthcareCostInitial() - healthModelPerYear.getHealthcareCostAfter());
			
			double totalInitialCost = Double.valueOf(df.format((healthModelPerYear.getCasesBeforeProgram() * totals.get(0).getCostPerCase()) + 
					(healthModelPerYear.getCasesBeforeProgram() * request.getValueOfQaly() * utilityDiff)));
			double totalAfterCost = Double.valueOf(df.format((healthModelPerYear.getCasesAfterProgram() * totals.get(0).getCostPerCase()) + 
					(healthModelPerYear.getCasesAfterProgram() * request.getValueOfQaly() * utilityDiff)));
			
			healthModelPerYear.setTotalCostInitial(totalInitialCost);
			healthModelPerYear.setTotalCostAfter(totalAfterCost);
			healthModelPerYear.setTotalCostDiff(totalInitialCost - totalAfterCost);
			healthModelPerYear.setTotalCostDiffDiscounted(healthModelPerYear.getTotalCostDiff() * discountRate);

			healthModelPerYear.setInvestment(investment);
			healthModelPerYear.setDiscountedInvestment(investment * discountRate);
			roiHealthModelPerYears.add(healthModelPerYear);
			startAge++;
			iter++;
		}

		//table 2
		
		cumulativeROIHealthModel.setCounty(String.join(",", countyList));
		double costPerCase = roiHealthModelPerYears.stream().mapToDouble(o -> o.getCostPerCase()).sum();
		cumulativeROIHealthModel.setCostPerCaseInitial(costPerCase);
		cumulativeROIHealthModel.setCostPerCaseAfterProgram(costPerCase);
		cumulativeROIHealthModel.setCostPerCaseDiff(0);
		
		double utilityLoss = BigDecimal.valueOf(roiHealthModelPerYears.stream().mapToDouble(o -> o.getUtilityDiffInitialWithoutDiscount()).average().orElse(0)).setScale(2, RoundingMode.DOWN).doubleValue();
		cumulativeROIHealthModel.setUtilityLossPerCaseInitial(utilityLoss);
		cumulativeROIHealthModel.setUtilityLossPerCaseAfterProgram(utilityLoss);
		cumulativeROIHealthModel.setUtilityLossPerCaseDiff(0);
		
		cumulativeROIHealthModel.setRatesInitial(BigDecimal.valueOf(roiHealthModelPerYears.stream().mapToDouble(o -> o.getPrevRateInitial()).average().orElse(0)).setScale(2, RoundingMode.DOWN).doubleValue());
		cumulativeROIHealthModel.setRatesAfterProgram(BigDecimal.valueOf(roiHealthModelPerYears.stream().mapToDouble(o -> o.getPrevRateAfter()).average().orElse(0)).setScale(2, RoundingMode.DOWN).doubleValue());
		cumulativeROIHealthModel.setRatesDiff(BigDecimal.valueOf(cumulativeROIHealthModel.getRatesInitial() - cumulativeROIHealthModel.getRatesAfterProgram()).setScale(2, RoundingMode.DOWN).doubleValue());

		cumulativeROIHealthModel.setPopulationInitial((long) roiHealthModelPerYears.stream().mapToDouble(o -> o.getPopulation()).sum());
		cumulativeROIHealthModel.setPopulationAfterProgram((long) roiHealthModelPerYears.stream().mapToDouble(o -> o.getPopulation()).sum());
		cumulativeROIHealthModel.setPopulationDiff(0);
		
		cumulativeROIHealthModel.setCasesInitial(Math.round(roiHealthModelPerYears.stream().mapToDouble(o -> o.getCasesBeforeProgram()).sum()));
		cumulativeROIHealthModel.setCasesAfterProgram(Math.round(roiHealthModelPerYears.stream().mapToDouble(o -> o.getCasesAfterProgram()).sum()));
		cumulativeROIHealthModel.setCasesDiff(cumulativeROIHealthModel.getCasesInitial() - cumulativeROIHealthModel.getCasesAfterProgram());
		
		cumulativeROIHealthModel.setUtilityLossInitial(Double.valueOf(df.format(roiHealthModelPerYears.stream().mapToDouble(o -> o.getUtilityLossInitial()).sum())));
		cumulativeROIHealthModel.setUtilityLossAfterProgram(Double.valueOf(df.format(roiHealthModelPerYears.stream().mapToDouble(o -> o.getUtilityLossAfter()).sum())));
		cumulativeROIHealthModel.setUtilityLossDiff(Double.valueOf(df.format(roiHealthModelPerYears.stream().mapToDouble(o -> o.getUtilityLossDiff()).sum())));
		
		cumulativeROIHealthModel.setHealthCareCostInitial(Double.valueOf(df.format(roiHealthModelPerYears.stream().mapToDouble(o -> o.getHealthcareCostInitial()).sum())));
		cumulativeROIHealthModel.setHealthCareCostAfterProgram(Double.valueOf(df.format(roiHealthModelPerYears.stream().mapToDouble(o -> o.getHealthcareCostAfter()).sum())));
		cumulativeROIHealthModel.setHealthCareCostDiff(Double.valueOf(df.format(roiHealthModelPerYears.stream().mapToDouble(o -> o.getHealthcareCostDiff()).sum())));
		
		cumulativeROIHealthModel.setTotalCostInitial(df.format(roiHealthModelPerYears.stream().mapToDouble(o -> o.getTotalCostInitial()).sum()));
		cumulativeROIHealthModel.setTotalCostAfterProgram(df.format(roiHealthModelPerYears.stream().mapToDouble(o -> o.getTotalCostAfter()).sum()));
		cumulativeROIHealthModel.setTotalCostDiff(Double.valueOf(df.format(roiHealthModelPerYears.stream().mapToDouble(o -> o.getTotalCostDiff()).sum())));
		
		
		
		//Cases in ‘X’ years 	
		cumulativeROIHealthModel.setTotalCasesWithoutProgram(Math.round(roiHealthModelPerYears.stream().mapToDouble(o -> Double.valueOf(o.getCasesBeforeProgram())).sum()));
		cumulativeROIHealthModel.setTotalCasesWithProgram(Math.round(roiHealthModelPerYears.stream().mapToDouble(o -> Double.valueOf(o.getCasesAfterProgram())).sum()));
		cumulativeROIHealthModel.setTotalCasesDiff(cumulativeROIHealthModel.getTotalCasesWithoutProgram() - cumulativeROIHealthModel.getTotalCasesWithProgram());

		//Total costs over ‘X’ years (without QALYs)	
		cumulativeROIHealthModel.setTotalCostWithoutQalyWithoutProgram(Double.valueOf(df.format(cumulativeROIHealthModel.getTotalCasesWithoutProgram() * totals.get(0).getCostPerCase())));
		cumulativeROIHealthModel.setTotalCostWithoutQalyWithProgram(Double.valueOf(df.format(cumulativeROIHealthModel.getTotalCasesWithProgram() * totals.get(0).getCostPerCase())));
		cumulativeROIHealthModel.setTotalCostWithoutQalyDiff(Double.valueOf(df.format(cumulativeROIHealthModel.getTotalCostWithoutQalyWithoutProgram() - cumulativeROIHealthModel.getTotalCostWithoutQalyWithProgram())));

		//Total costs over ‘X’ years (with QALYs)	
		cumulativeROIHealthModel.setTotalCostWithQalyWithoutProgram(df.format(roiHealthModelPerYears.stream().mapToDouble(o -> Double.valueOf(o.getTotalCostInitial())).sum()));
		cumulativeROIHealthModel.setTotalCostWithQalyWithProgram(df.format(roiHealthModelPerYears.stream().mapToDouble(o -> Double.valueOf(o.getTotalCostAfter())).sum()));
		cumulativeROIHealthModel.setTotalCostWithQalyDiff(Double.valueOf(df.format(Double.valueOf(cumulativeROIHealthModel.getTotalCostWithQalyWithoutProgram()) -  Double.valueOf(cumulativeROIHealthModel.getTotalCostWithQalyWithProgram()))));
		
		//Total QALYs over ‘X’ years
		cumulativeROIHealthModel.setTotalQalyWithProgram(Double.valueOf(df.format(roiHealthModelPerYears.stream().mapToDouble( o -> o.getUtilityLossAfter()).sum())));
		cumulativeROIHealthModel.setTotalQaLYWithoutProgram(Double.valueOf(df.format(roiHealthModelPerYears.stream().mapToDouble( o -> o.getUtilityLossInitial()).sum())));
		cumulativeROIHealthModel.setTotalQalyDiff(Double.parseDouble(df.format(cumulativeROIHealthModel.getTotalQaLYWithoutProgram() - cumulativeROIHealthModel.getTotalQalyWithProgram())));

		//Total investment	
		cumulativeROIHealthModel.setInvestmentWithoutProgram(0.0);
		cumulativeROIHealthModel.setInvestmentWithProgram(Double.parseDouble(df.format(roiHealthModelPerYears.stream().mapToDouble(o -> Double.valueOf(o.getInvestment())).sum())));
		cumulativeROIHealthModel.setInvestmentDiff(Double.parseDouble(df.format(roiHealthModelPerYears.stream().mapToDouble(o -> Double.valueOf(o.getInvestment())).sum())));

		//Return on investment (without QALYs)		
		cumulativeROIHealthModel.setROIWithoutQaly(Double.parseDouble(df.format((cumulativeROIHealthModel.getTotalCostWithoutQalyDiff() - 
				cumulativeROIHealthModel.getInvestmentDiff())/cumulativeROIHealthModel.getInvestmentDiff())));
		//Return on investment (without QALYs)	
		cumulativeROIHealthModel.setROIWithQaly(Double.parseDouble(df.format((cumulativeROIHealthModel.getTotalCostWithQalyDiff() - cumulativeROIHealthModel.getInvestmentDiff())/cumulativeROIHealthModel.getInvestmentDiff())));
		//Return on investment (without QALYs)	
		
		double discountedInvestment = roiHealthModelPerYears.stream().mapToDouble(o -> Double.valueOf(o.getDiscountedInvestment())).sum();
		double roiDiscounted = BigDecimal.valueOf(((roiHealthModelPerYears.stream().mapToDouble(o -> Double.valueOf(o.getTotalCostDiffDiscounted())).sum() - discountedInvestment)/discountedInvestment)).setScale(2, RoundingMode.DOWN).doubleValue();
		cumulativeROIHealthModel.setROIDiscounted(roiDiscounted);


		cumulativeROIHealthModel.setAgeRange(roiHealthModelPerYears);

		return cumulativeROIHealthModel; 
	}
	
	private double getAveragePrevAge(List<HealthTotalData> totals, int age) {
		double sum = 0;
		int itr = 0;
		for(HealthTotalData total : totals) {
			if(total.getAge() == age) {
				sum = sum + total.getPrevalenceRate();
				itr++;
			}
		}
		return sum/(itr);
	}
	
	private double getAverageUtilityDiff(List<HealthTotalData> totals, int age) {
		double sum = 0;
		int itr = 0;
		for(HealthTotalData total : totals) {
			if(total.getAge() == age) {
				sum = sum + total.getUtilityLoss();
				itr++;
			}
		}
		return sum/(itr);
	}
	
	private double getDiscount(int age) {
		return 1/Math.pow(1.03,age-1);
	}

	private double getCostPerCase(List<HealthTotalData> totals, int age) {
		double sum = 0;
		int itr = 0;
		for(HealthTotalData total : totals) {
			if(total.getAge() == age) {
				sum = sum + total.getCostPerCase();
				itr++;
			}
		}
		return sum/(itr);
	}

}

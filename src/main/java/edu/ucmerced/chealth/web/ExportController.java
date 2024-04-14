package edu.ucmerced.chealth.web;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import edu.ucmerced.chealth.datasource.health.domain.HealthTotalData;
import edu.ucmerced.chealth.datasource.health.domain.Totals;
import edu.ucmerced.chealth.datasource.health.repository.CountyRepository;
import edu.ucmerced.chealth.datasource.health.repository.DiseaseRepository;
import edu.ucmerced.chealth.datasource.health.repository.EthnicityRepository;
import edu.ucmerced.chealth.datasource.health.repository.HealthTotalRepository;
import edu.ucmerced.chealth.datasource.health.repository.RegionRepository;
import edu.ucmerced.chealth.search.HealthDataDTO;
import edu.ucmerced.chealth.search.SearchCriteria;
import edu.ucmerced.chealth.search.TotalsDTO;
import edu.ucmerced.chealth.service.SearchService;

@Controller
public class ExportController {
    @Autowired
    private SearchService searchService;
    
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

    @GetMapping("/export")
    public ResponseEntity<Resource> export(
            @RequestParam(value = "region", required = false) List<Long> regions,
            @RequestParam("county") List<Long> counties,
            @RequestParam("disease") List<Long> diseases,
            @RequestParam("ethnicity") List<Long> ethnicity,
            @RequestParam("ageGroup") List<Long> ageGroups,
            @RequestParam("sex") List<String> sexes) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        final CSVPrinter printer = new CSVPrinter(new PrintWriter(out), CSVFormat.DEFAULT);
        printer.printRecord("Disease", "Region", "County", "Ethnicity", "SEX", "Age_Group",
                "Case", "Cost");
        for (Totals totals: searchService.search(new SearchCriteria(regions, counties, diseases,
                        ethnicity, ageGroups, sexes))) {
            TotalsDTO dto = new TotalsDTO(totals);
            printer.printRecord(dto.getDiseaseName(), dto.getRegionName(), dto.getCountyName(),
                    dto.getEthnicity(), dto.getSex(), dto.getGroupName(),
                    String.format("%.2f", dto.getCases()),
                    String.format("%.2f", dto.getCosts()));
        }
        printer.flush();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=CBCD.csv")
                .contentType(MediaType.TEXT_PLAIN)
                .body(new InputStreamResource(new ByteArrayInputStream(out.toByteArray())));
    }
    
    @GetMapping("/exportData")
    public ResponseEntity<Resource> exportCostData(
    		@RequestParam(value = "region", required = false) String regions,
			@RequestParam("county") String counties,
			@RequestParam("disease") String diseases,
			@RequestParam("ethnicity") String ethnicity,
			@RequestParam("ageGroup") String ageGroups,
			@RequestParam("sex") String sexes) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        final CSVPrinter printer = new CSVPrinter(new PrintWriter(out), CSVFormat.DEFAULT);
         	List<String> countyList =countyRepository.findByIdIn(Stream.of(counties.split(","))
				.map(Long::parseLong)
				.collect(Collectors.toList()));
		List<String> ethnicityList =ethnicityRepository.findByIdIn(Stream.of(ethnicity.split(","))
				.map(Long::parseLong)
				.collect(Collectors.toList()));
		List<String> diseaseList =diseaseRepository.findByIdIn(Stream.of(diseases.split(","))
				.map(Long::parseLong)
				.collect(Collectors.toList()));
		List<String> genderList = new
				 ArrayList<String>(Arrays.asList(sexes.split(",")));
		List<String> regionList =regionRepository.findByIdIn(Stream.of(regions.split(","))
				.map(Long::parseLong)
				.collect(Collectors.toList()));
		List<Integer> ageList = Stream.of(ageGroups.split("-"))
				.map(Integer::parseInt)
				.collect(Collectors.toList());
		
		List<HealthTotalData> countiesHealthDataList =  healthTotalRepository.retrieveHealthData(ageList.get(0), ageList.get(1), countyList, 
				ethnicityList, diseaseList, genderList, regionList);
		   printer.printRecord("Disease", "Region", "County", "Ethnicity","Gender", "Age","Total_Pop", "Prev_Rate(%)","Case", "Cost Per Case",
	                 "Total Healthcare Cost", "Avg Utility", "Avg Healthy Utility","Utility loss", "Total Utility Loss", "Total cost of Utility","Total Cost(Healthcare and Utility)");
	                 

        for (HealthTotalData totals: countiesHealthDataList) {
        	HealthDataDTO dto = new HealthDataDTO(totals);
            printer.printRecord(dto.getDiseaseName(), dto.getRegionName(), dto.getCountyName(),dto.getEthnicity(), 
            		dto.getSex(), dto.getGroupName(), dto.getPopulation(), dto.getPrevalenceRate(),dto.getCases(), dto.getCostPerCase(),
                    dto.gethealthcareCost(), dto.getAverageUtility(), dto.getAverageHealthyUtility(),
                    dto.getutilityLoss(),dto.getTotalUtilityLoss(), dto.getTotalCostOfUtility() ,dto.gettotalCost());
        }
        
        DecimalFormat df = new DecimalFormat("#");
		df.setMaximumFractionDigits(2);
        printer.printRecord("", "", "",
                "", "", "Average", 
                df.format(countiesHealthDataList.stream().mapToDouble(o->o.getPopulation()).average().orElse(0.0)), 
                df.format(countiesHealthDataList.stream().mapToDouble(o->o.getPrevalenceRate()).average().orElse(0.0)), 
                df.format(countiesHealthDataList.stream().mapToDouble(o->o.getCases()).average().orElse(0.0)), 
                df.format(countiesHealthDataList.stream().mapToDouble(o->o.getCostPerCase()).average().orElse(0.0)), 
                df.format(countiesHealthDataList.stream().mapToDouble(o->o.getTotalHCCost()).average().orElse(0.0)), 
                df.format(countiesHealthDataList.stream().mapToDouble(o->o.getAverageUtility()).average().orElse(0.0)), 
                df.format(countiesHealthDataList.stream().mapToDouble(o->o.getAverageHealthyUtility()).average().orElse(0.0)),
                df.format(countiesHealthDataList.stream().mapToDouble(o->o.getUtilityLoss()).average().orElse(0.0)), 
                df.format(countiesHealthDataList.stream().mapToDouble(o->o.getTotalUtilityLoss()).average().orElse(0.0)),
                df.format(countiesHealthDataList.stream().mapToDouble(o->o.getCostOfUtility()).average().orElse(0.0)),
                df.format(countiesHealthDataList.stream().mapToDouble(o->o.getTotalTotalCost()).average().orElse(0.0)));
        
        printer.printRecord("", "", "",
                "", "", "Total", 
                df.format(countiesHealthDataList.stream().mapToDouble(o->o.getPopulation()).sum()), 
                df.format(countiesHealthDataList.stream().mapToDouble(o->o.getPrevalenceRate()).sum()), 
                df.format(countiesHealthDataList.stream().mapToDouble(o->o.getCases()).sum()), 
                df.format(countiesHealthDataList.stream().mapToDouble(o->o.getCostPerCase()).sum()), 
                df.format(countiesHealthDataList.stream().mapToDouble(o->o.getTotalHCCost()).sum()), 
                df.format(countiesHealthDataList.stream().mapToDouble(o->o.getAverageUtility()).sum()), 
                df.format(countiesHealthDataList.stream().mapToDouble(o->o.getAverageHealthyUtility()).sum()),
                df.format(countiesHealthDataList.stream().mapToDouble(o->o.getUtilityLoss()).sum()), 
                df.format(countiesHealthDataList.stream().mapToDouble(o->o.getTotalUtilityLoss()).sum()),
                df.format(countiesHealthDataList.stream().mapToDouble(o->o.getCostOfUtility()).sum()),
                df.format(countiesHealthDataList.stream().mapToDouble(o->o.getTotalTotalCost()).sum()));
        
        
        printer.flush();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=CBCD.csv")
                .contentType(MediaType.TEXT_PLAIN)
                .body(new InputStreamResource(new ByteArrayInputStream(out.toByteArray())));
    }
}

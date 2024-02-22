package edu.ucmerced.chealth.service;

import edu.ucmerced.chealth.datasource.health.domain.*;
import edu.ucmerced.chealth.datasource.health.repository.*;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/*
 * This is used to create the embedded Chronic Health database. See README.database.
 */

@Service
@CommonsLog
public class DataLoader {
    @Value("${loadData:true}")
    private boolean loadData;

    @Autowired
    private AgeGroupRepository ageGroupRepository;

    @Autowired
    private CountyRepository countyRepository;

    @Autowired
    private DiseaseCategoryRepository diseaseCategoryRepository;

    @Autowired
    private DiseaseRepository diseaseRepository;

    @Autowired
    private EthnicityRepository ethnicityRepository;

    @Autowired
    private RegionRepository regionRepository;

    @Autowired
    private TotalsRepository totalsRepository;
    
    @Autowired
    private HealthTotalRepository healthTotalRepository;

    @PostConstruct
    private void init() {
        if (loadData) {
            Path dataPath = Path.of("").toAbsolutePath().resolve("data");
            if (!Files.exists(dataPath)) {
                throw new IllegalArgumentException(
                        "Unable to load data; data path doesn't exist: " + dataPath);
            }
            try {
                log.info("Inserting data from CSV files found in " + dataPath);
                loadData(dataPath);
            } catch (IOException e) {
                log.error("Data load failed: " + e);
            }
        }
    }

    /**
     * Load data from the CSV files into the database.
     *
     * @param dataPath  The location of the CSV files
     */
    public void loadData(Path dataPath) throws IOException {
        totalsRepository.deleteAll();
        diseaseRepository.deleteAll();
        countyRepository.deleteAll();
        regionRepository.deleteAll();
        ethnicityRepository.deleteAll();
        diseaseCategoryRepository.deleteAll();
        ageGroupRepository.deleteAll();

        log.info("Inserting age groups");
        Map<String, AgeGroup> ageGroups = new HashMap<>();
        Path ageGroupPath = dataPath.resolve("ageGroup.csv");
        for (CSVRecord csvRecord : CSVFormat.DEFAULT.parse(
                new FileReader(ageGroupPath.toString()))) {
            AgeGroup ageGroup = new AgeGroup();
            ageGroup.setGroupName(csvRecord.get(0));
            AgeGroup savedAgeGroup = ageGroupRepository.save(ageGroup);
            log.info(" - Saved: " + savedAgeGroup);
            ageGroups.put(savedAgeGroup.getGroupName(), savedAgeGroup);
        }

        log.info("Inserting disease categories");
        Map<String, DiseaseCategory> diseaseCategories = new HashMap<>();
        Path dcPath = dataPath.resolve("diseaseCategories.csv");
        for (CSVRecord csvRecord : CSVFormat.DEFAULT.parse(
                new FileReader(dcPath.toString()))) {
            DiseaseCategory diseaseCategory = new DiseaseCategory();
            diseaseCategory.setName(csvRecord.get(0));
            DiseaseCategory savedDc = diseaseCategoryRepository.save(diseaseCategory);
            log.info(" - Saved: " + savedDc);
            diseaseCategories.put(savedDc.getName(), savedDc);
        }

        log.info("Inserting ethnicities");
        Map<String, Ethnicity> ethnicities = new HashMap<>();
        Path ethnicityPath = dataPath.resolve("ethnicity.csv");
        for (CSVRecord csvRecord : CSVFormat.DEFAULT.parse(
                new FileReader(ethnicityPath.toString()))) {
            if (!StringUtils.isBlank(csvRecord.get(0))) {
                Ethnicity ethnicity = new Ethnicity();
                ethnicity.setEthnicity(csvRecord.get(0));
                Ethnicity savedEthnicity = ethnicityRepository.save(ethnicity);
                log.info(" - Saved: " + savedEthnicity);
                ethnicities.put(savedEthnicity.getEthnicity(), savedEthnicity);
            }
        }

        log.info("Inserting regions");
        Map<String, Region> regions = new HashMap<>();
        Path regionPath = dataPath.resolve("regions.csv");
        for (CSVRecord csvRecord : CSVFormat.DEFAULT.parse(
                new FileReader(regionPath.toString()))) {
            if (!StringUtils.isBlank(csvRecord.get(0))) {
                Region region = new Region();
                region.setRegion(csvRecord.get(0));
                Region savedRegion = regionRepository.save(region);
                log.info(" - Saved: " + savedRegion);
                regions.put(savedRegion.getRegion(), savedRegion);
            }
        }

        log.info("Inserting counties...");
        Map<String, County> counties = new HashMap<>();
        Path countyPath = dataPath.resolve("counties.csv");
        for (CSVRecord csvRecord : CSVFormat.DEFAULT.parse(
                new FileReader(countyPath.toString()))) {
            String regionName = csvRecord.get(0);
            String countyName = csvRecord.get(1);
            if (!StringUtils.isBlank(regionName) && !StringUtils.isBlank(countyName)) {
                if (regions.containsKey(regionName)) {
                    County county = new County();
                    county.setCounty(countyName);
                    county.setRegion(regions.get(regionName));
                    County savedCounty = countyRepository.save(county);
                    counties.put(savedCounty.getCounty(), savedCounty);
                } else {
                    log.error("County has unknown region: " + regionName);
                }
            }
        }
        log.info(" - " + counties.size() + " counties were inserted");

        log.info("Inserting diseases");
        Map<String, Disease> diseases = new HashMap<>();
        Path diseasePath = dataPath.resolve("diseases.csv");
        for (CSVRecord csvRecord : CSVFormat.DEFAULT.parse(
                new FileReader(diseasePath.toString()))) {
            String dcName = csvRecord.get(0);
            String diseaseName = csvRecord.get(1);
            if (!StringUtils.isBlank(dcName) && !StringUtils.isBlank(diseaseName)) {
                if (diseaseCategories.containsKey(dcName)) {
                    Disease disease = new Disease();
                    disease.setDisease(diseaseName);
                    disease.setCategory(diseaseCategories.get(dcName));
                    Disease savedDisease = diseaseRepository.save(disease);
                    log.info("Saved: " + savedDisease);
                    diseases.put(savedDisease.getDisease(), savedDisease);
                } else {
                    log.error("Disease has unknown category: " + dcName);
                }
            }
        }

        log.info("Inserting totals...");
        Path totalsPath = dataPath.resolve("data.csv");
        int numTotals = 0;
		/*
		 * for (CSVRecord csvRecord : CSVFormat.DEFAULT.parse( new
		 * FileReader(totalsPath.toString()))) { if (csvRecord.size() == 10) { Totals
		 * totals = new Totals(); totals.setDisease(diseases.get(csvRecord.get(0)));
		 * totals.setRegion(regions.get(csvRecord.get(1)));
		 * totals.setCounty(counties.get(csvRecord.get(2)));
		 * totals.setEthnicity(ethnicities.get(csvRecord.get(3)));
		 * totals.setSex(csvRecord.get(4));
		 * totals.setAgeGroup(ageGroups.get(csvRecord.get(5)));
		 * totals.setCases(Double.valueOf(csvRecord.get(6)));
		 * totals.setCosts(Double.valueOf(csvRecord.get(7)));
		 * totals.setPopulation(Long.valueOf(csvRecord.get(8)));
		 * totals.setPrevalenceRate(Float.valueOf(csvRecord.get(9)));
		 * totalsRepository.save(totals); numTotals++; } else if (csvRecord.size() != 0)
		 * { log.error("Bad line in data.csv? had " + csvRecord.size() + " fields"); } }
		 */
        log.info(" - " + numTotals + " totals were inserted");
        
        log.info("Inserting Health Data ROI...");
        Path HealthDataPath = dataPath.resolve("Data_ToolROI-1.csv");
        int data = 0;
        for (CSVRecord csvRecord : CSVFormat.DEFAULT.parse(
                new FileReader(HealthDataPath.toString()))) {
        	if (csvRecord.size() == 17) {
        		HealthTotalData healthTotalData = new HealthTotalData();
        		healthTotalData.setDisease(csvRecord.get(0));
        		healthTotalData.setRegion(csvRecord.get(1));
        		healthTotalData.setCounty(csvRecord.get(2));
        		healthTotalData.setEthnicity(csvRecord.get(3));
        		healthTotalData.setSex(csvRecord.get(4));
        		healthTotalData.setAge(Integer.parseInt(csvRecord.get(5)));
        		healthTotalData.setCases(Double.valueOf(csvRecord.get(6)));
        		healthTotalData.setCosts(Double.valueOf(csvRecord.get(7)));
        		healthTotalData.setPopulation(Float.valueOf(csvRecord.get(8)));
        		healthTotalData.setPrevalenceRate(Float.valueOf(csvRecord.get(9)));
        		healthTotalData.setAverageUtility(Float.valueOf(csvRecord.get(10)));
        		healthTotalData.setAverageHealthyUtility(Double.valueOf(csvRecord.get(11)));
        		healthTotalData.setCostPerCase(Double.valueOf(csvRecord.get(12)));
        		healthTotalData.setTotalHCCost(Double.valueOf(csvRecord.get(13)));
        		healthTotalData.setUtilityLoss(Double.valueOf(csvRecord.get(14)));
        		healthTotalData.setTotalCostOfUtility(Double.valueOf(csvRecord.get(15)));
        		healthTotalData.setTotalTotalCost(Double.valueOf(csvRecord.get(16)));
                healthTotalRepository.save(healthTotalData);
                data++;
            } else if (csvRecord.size() != 0) {
                log.error("Bad line in data.csv? had " + csvRecord.size() + " fields");
            }
        }
        log.info(" - " + data + " HealthData were inserted");
    }
}

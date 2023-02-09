package edu.ucmerced.chealth.web;

import edu.ucmerced.chealth.datasource.health.domain.*;
import edu.ucmerced.chealth.datasource.health.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api")
public class HealthController {
    @Autowired
    private AgeGroupRepository ageGroupRepository;

    @Autowired
    private DiseaseRepository diseaseRepository;

    @Autowired
    private CountyRepository countyRepository;

    @Autowired
    private EthnicityRepository ethnicityRepository;

    @Autowired
    private RegionRepository regionRepository;

    @GetMapping("/agegroup")
    @ResponseBody
    public List<AgeGroup> getAgeGroups() {
        List<AgeGroup> ageGroups = new ArrayList<>();
        ageGroupRepository.findAll().forEach(ageGroups::add);
        return ageGroups;
    }

    @GetMapping("/counties")
    @ResponseBody
    public List<County> getCounties() {
        List<County> counties = new ArrayList<>();
        countyRepository.findAll().forEach(counties::add);
        return counties;
    }

    @GetMapping("/diseases")
    @ResponseBody
    public List<Disease> getDiseases() {
        List<Disease> diseases = new ArrayList<>();
        diseaseRepository.findAll().forEach(diseases::add);
        return diseases;
    }

    @GetMapping("/ethnicity")
    @ResponseBody
    public List<Ethnicity> getEthnicities() {
        List<Ethnicity> ethnicities = new ArrayList<>();
        ethnicityRepository.findAll().forEach(ethnicities::add);
        return ethnicities;
    }


    @GetMapping("/regions")
    @ResponseBody
    public List<Region> getRegions() {
        List<Region> regions = new ArrayList<>();
        regionRepository.findAll().forEach(regions::add);
        return regions;
    }
}

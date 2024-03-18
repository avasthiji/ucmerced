package edu.ucmerced.chealth.web;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.ucmerced.chealth.datasource.health.domain.ROICalculatorRequest;
import edu.ucmerced.chealth.service.CostCalculatorService;
import edu.ucmerced.chealth.service.ROICalculatorService;

@CrossOrigin
@RestController
@RequestMapping("/api")
public class CostCalculatorController {    
	@Autowired
	private CostCalculatorService costCalculatorService;

	@Autowired
	private ROICalculatorService calculatorService;

	@GetMapping("/utilityCost")
	@ResponseBody
	public ResponseEntity<ObjectNode> getData( 
			@RequestParam(value = "region", required = false) String regions,
			@RequestParam("county") String counties,
			@RequestParam("disease") String diseases,
			@RequestParam("ethnicity") String ethnicity,
			@RequestParam("ageGroup") String ageGroups,
			@RequestParam("sex") String sexes) {

		return ResponseEntity
				.ok()
				.body(costCalculatorService.getHealthData(counties, diseases, ethnicity, ageGroups, sexes, regions));
	}

	@PostMapping("/roiCalculator")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> getRoiData(@RequestBody ROICalculatorRequest request) 
	{
		return ResponseEntity
				.ok()
				.body(calculatorService.getROIData(request));
	}
}
